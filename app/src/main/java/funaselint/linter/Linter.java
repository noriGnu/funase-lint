package funaselint.linter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import funaselint.model.Presentation;
import funaselint.utils.TempDirUtils;
import funaselint.utils.ZipUtils;

public class Linter {
    private RuleEngine ruleEngine;

    public Linter(LinterOption linterOption) {
        this.ruleEngine = new RuleEngine(linterOption.getRules(), linterOption.isFix(), linterOption.isVerbose());
    }

    public void lint(Path inputPath) {
        if (Files.isDirectory(inputPath)) {
            new IgnoreProcessor(inputPath).findFilesToLint().parallelStream().forEach(this::lintPresentation);
        } else {
            lintPresentation(inputPath);
        }
    }

    public void lintPresentation(Path pptx) {
        Presentation presentation = new Presentation(pptx);

        try {
            Path tempDir = TempDirUtils.createTempDirectory("unzippedPptx");
            ZipUtils.unzip(presentation.getFilePath(), tempDir);

            System.out.println("Linting " + presentation.getFilePath() + "...");
            ruleEngine.applyRules(tempDir.toFile());

            if (ruleEngine.isFix()) {
                Path newPptxFilePath = createLintedFilePath(presentation.getFilePath());
                ZipUtils.zip(tempDir, newPptxFilePath);
            }

            TempDirUtils.deleteDirectory(tempDir);
        } catch (IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private Path createLintedFilePath(Path originalPath) {
        String originalName = originalPath.getFileName().toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = dateFormat.format(new Date());
        String newName = originalName.replaceFirst("(?i)(\\.pptx)$", "_linted_" + timestamp + ".pptx");
        return originalPath.getParent().resolve(newName);
    }
}
