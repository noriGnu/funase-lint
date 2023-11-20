package funaselint.linter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import funaselint.rules.PunctuationMarkRule;
import funaselint.rules.RuleEngine;
import funaselint.rules.SlideAspectRule;

public class Linter {
    public void lint(File presentation) {
        try {
            // 一時ディレクトリの作成
            File tempDir = Files.createTempDirectory("unzippedPptx").toFile();

            // .pptx ファイルを解凍
            unzipFile(presentation, tempDir);

            // PowerPointファイルに対するリント処理の実装
            System.out.println("Linting PowerPoint file: " + presentation.getName());
            // ルールベースのチェックや修正処理

            RuleEngine engine = new RuleEngine();
            engine.addRule(new SlideAspectRule());
            engine.addRule(new PunctuationMarkRule());

            try {
                engine.applyRules(tempDir);
            } catch (IOException e) {
                e.printStackTrace();
            }

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
