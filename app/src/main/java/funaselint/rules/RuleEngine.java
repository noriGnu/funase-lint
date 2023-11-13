package funaselint.rules;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class RuleEngine {
    private final List<Rule> rules = new ArrayList<>();

    public void addRule(Rule rule) {
        rules.add(rule);
    }

    public void applyRules(File baseDirectory) throws IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (Exception e) {
            throw new IOException("Error creating DocumentBuilder", e);
        }

        for (Rule rule : rules) {
            for (String path : rule.applicableFilesOrFolders()) {
                File fileOrDirectory = new File(baseDirectory, path);
                if (fileOrDirectory.isDirectory()) {
                    // ディレクトリ内の全てのファイルにルールを適用
                    try (Stream<Path> paths = Files.walk(Paths.get(fileOrDirectory.toURI()))) {
                        paths.filter(Files::isRegularFile)
                                .forEach(file -> applyRuleToFile(rule, file.toFile(), dBuilder));
                    }
                } else if (fileOrDirectory.exists()) {
                    // 単一のファイルにルールを適用
                    applyRuleToFile(rule, fileOrDirectory, dBuilder);
                }
            }
        }
    }

    private void applyRuleToFile(Rule rule, File file, DocumentBuilder dBuilder) {
        try {
            Document doc = dBuilder.parse(file);
            System.out.println("Applying rule " + rule + " to file: " + file.getPath());

            if (rule instanceof AutoFixable && rule.checkCondition(doc, file)) {
                ((AutoFixable) rule).autoFix(doc, file);

                // 変更されたDocumentオブジェクトをXMLファイルとして保存
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(file);
                transformer.transform(source, result);
            }
        } catch (Exception e) {
            System.err.println("Error applying rule to file: " + file.getPath());
            e.printStackTrace();
        }
    }
}