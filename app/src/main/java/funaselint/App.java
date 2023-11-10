package funaselint;

import java.io.File;
import java.util.concurrent.Callable;

import funaselint.linter.Linter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "funase-lint", mixinStandardHelpOptions = true, version = "funase-lint 1.0", description = "Lints and fixes PowerPoint presentations according to specified rules.")
public class App implements Callable<Integer> {

    @Option(names = { "--config", "-c" }, description = "Path to configuration file.")
    private File configFile;

    @Option(names = { "--rule", "-r" }, description = "Specify rules to check.")
    private String rule;

    @Option(names = { "--fix", "-f" }, description = "Automatically fix problems.")
    private boolean fix;

    @Option(names = { "--style", "-s" }, description = "Specify output style.")
    private String style;

    @Option(names = { "--ignore", "-i" }, description = "Specify patterns to ignore.")
    private String ignore;

    @Option(names = { "--list-rules", "-l" }, description = "List all available rules.")
    private boolean listRules;

    @Option(names = { "--verbose", "-v" }, description = "Enable verbose output for more detailed information.")
    private boolean verbose;

    @Parameters(index = "0", description = "The PowerPoint file or directory to lint.")
    private File inputPath;

    private Linter linter = new Linter();

    @Override
    public Integer call() throws Exception {
        // ここにリンターのロジックを実装します。

        if (configFile != null) {
            // コンフィグファイルの読み込みと処理
        }

        if (rule != null) {
            // 特定のルールに基づくリントの実行
        }

        if (fix) {
            // 修正可能な問題の自動修正
        }

        if (style != null) {
            // 指定された形式での出力
        }

        if (ignore != null) {
            // 無視するパターンの処理
        }

        if (listRules) {
            // listAvailableRules();
            return 0;
        }

        if (verbose) {
            // Verbose 処理をするためのロジック
            System.out.println("Verbose mode enabled.");
        }

        if (inputPath.isDirectory()) {
            lintDirectory(inputPath);
        } else {
            lintPresentation(inputPath);
        }

        return 0; // 成功した場合は0を返します
    }

    public void lintPresentation(File presentation) {
        linter.lint(presentation);
    }

    public void lintDirectory(File directory) {
        // ディレクトリ内の全てのファイル（またはサブディレクトリ内のファイル）をリントする
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    lintDirectory(file); // サブディレクトリを再帰的に処理
                } else if (file.isFile() && shouldLintFile(file)) {
                    lintPresentation(file); // PowerPoint ファイルを処理
                }
            }
        }
    }

    public boolean shouldLintFile(File file) {
        // PowerPoint ファイルであるかどうかを判定するロジック
        // 例えば、拡張子が .pptx である場合に true を返す
        return file.getName().endsWith(".pptx");
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}
