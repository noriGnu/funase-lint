package funaselint.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static void unzip(Path zipFilePath, Path outputDirPath) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFilePath))) {
            extractEntries(zis, outputDirPath);
        }
    }

    public static void zip(Path directoryPath, Path zipFilePath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            addDirectoryToZip(zos, directoryPath);
        }
    }

    private static void extractEntries(ZipInputStream zis, Path outputDirPath) throws IOException {
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
            Path newFilePath = outputDirPath.resolve(zipEntry.getName());
            if (zipEntry.isDirectory()) {
                Files.createDirectories(newFilePath);
            } else {
                Files.createDirectories(newFilePath.getParent());
                Files.copy(zis, newFilePath);
            }
        }
    }

    private static void addDirectoryToZip(ZipOutputStream zos, Path directoryPath) throws IOException {
        try (Stream<Path> stream = Files.walk(directoryPath)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                ZipEntry zipEntry = new ZipEntry(directoryPath.relativize(path).toString());
                try {
                    zos.putNextEntry(zipEntry);
                    Files.copy(path, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }
}
