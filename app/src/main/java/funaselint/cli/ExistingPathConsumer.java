package funaselint.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Stack;

import picocli.CommandLine;
import picocli.CommandLine.IParameterConsumer;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;

public class ExistingPathConsumer implements IParameterConsumer {
    @Override
    public void consumeParameters(Stack<String> args, ArgSpec argSpec, CommandSpec commandSpec) {
        // Stackからパス文字列を取得し、Pathに変換
        String pathStr = args.pop();
        Path path = Path.of(pathStr).normalize().toAbsolutePath();

        // パスが存在するか確認
        if (!Files.exists(path)) {
            throw new CommandLine.ParameterException(commandSpec.commandLine(), "Path does not exist: " + path);
        }

        // ArgSpecに変換されたPathをセット
        argSpec.setValue(path);
    }
}
