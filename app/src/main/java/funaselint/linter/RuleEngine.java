package funaselint.linter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import funaselint.rules.AutoFixable;
import funaselint.rules.Rule;

public class RuleEngine {
    private final List<Rule> rules;
    private boolean fix;
    private boolean verbose;

    public RuleEngine(List<Rule> rules, boolean fix, boolean verbose) {
        this.rules = rules;
        this.fix = fix;
        this.verbose = verbose;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public boolean isFix() {
        return fix;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void applyRules(File baseDirectory) throws IOException, ParserConfigurationException {
        DocumentBuilder dBuilder = createDocumentBuilder();

        for (Rule rule : rules) {
            processRule(baseDirectory, rule, dBuilder);
        }
    }

    private DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        return dbFactory.newDocumentBuilder();
    }

    private void processRule(File baseDirectory, Rule rule, DocumentBuilder dBuilder) {
        for (String relativePath : rule.applicableFilesOrFolders()) {
            File fileOrDirectory = new File(baseDirectory, relativePath);
            if (fileOrDirectory.isDirectory()) {
                applyRuleToDirectory(rule, fileOrDirectory, dBuilder);
            } else if (fileOrDirectory.exists()) {
                applyRuleToFile(rule, fileOrDirectory, dBuilder);
            }
        }
    }

    private void applyRuleToDirectory(Rule rule, File directory, DocumentBuilder dBuilder) {
        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            paths.filter(Files::isRegularFile)
                    .forEach(file -> applyRuleToFile(rule, file.toFile(), dBuilder));
        } catch (IOException e) {
            System.err.println("Error processing directory: " + directory.getPath());
            e.printStackTrace();
        }
    }

    private void applyRuleToFile(Rule rule, File file, DocumentBuilder dBuilder) {
        try {
            Document doc = dBuilder.parse(file);
            if (verbose) {
                System.out.println("Applying rule " + rule + " to file: " + file.getPath());
            }

            if (fix && rule instanceof AutoFixable && rule.checkCondition(doc, file)) {
                ((AutoFixable) rule).autoFix(doc, file);
                saveDocumentToFile(doc, file);
            }
        } catch (Exception e) {
            System.err.println("Error applying rule to file: " + file.getPath());
            e.printStackTrace();
        }
    }

    private void saveDocumentToFile(Document doc, File file) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }
}
