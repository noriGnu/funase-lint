package funaselint;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import funaselint.cli.ExistingPathConsumer;
import funaselint.linter.Linter;
import funaselint.linter.LinterOption;
import funaselint.rules.Rule;
import funaselint.rules.SlideAspectRule;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "funase-lint", mixinStandardHelpOptions = true, version = "funase-lint 1.0", description = "Lints and fixes PowerPoint presentations according to specified rules.")
public class App implements Callable<Integer> {

    @Option(names = { "--rule", "-r" }, //
            description = "Specify rules to check.")
    private String rule;

    @Option(names = { "--fix", "-f" }, //
            description = "Automatically fix problems.")
    private boolean fix;

    @Option(names = { "--style", "-s" }, //
            description = "Specify output style.")
    private String style;

    @Option(names = { "--list-rules", "-l" }, //
            description = "List all available rules.")
    private boolean listRules;

    @Option(names = { "--verbose", "-v" }, //
            description = "Enable verbose output for more detailed information.")
    private boolean verbose;

    @Parameters(index = "0", //
            description = "The PowerPoint file or directory to lint.", //
            parameterConsumer = ExistingPathConsumer.class)
    private Path inputPath;

    @Override
    public Integer call() throws Exception {
        LinterOption linterOption = new LinterOption();
        
        List<Rule> rules = new ArrayList<>();
        rules.add(new SlideAspectRule());
        linterOption.setRules(rules);

        if (rule != null) {
            // 特定のルールに基づくリントの実行
        }

        if (fix) {
            linterOption.setFix(true);
        }

        if (listRules) {
            // listAvailableRules();
            return 0;
        }

        Linter linter = new Linter(linterOption);
        linter.lint(inputPath);

        return 0; // 成功した場合は0を返します
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }
}
