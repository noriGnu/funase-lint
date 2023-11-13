package funaselint.linter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

            try {
                engine.applyRules(tempDir);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // ZIPアーカイブのファイル名を設定（元のファイル名に基づく）
            File newPptxFile = new File(presentation.getParent(), createLintedFilename(presentation));

            // 一時ディレクトリをZIPアーカイブにまとめる
            zipDirectory(tempDir, newPptxFile);

            // 一時ディレクトリの削除
            deleteDirectory(tempDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void unzipFile(File zipFile, File outputDir) throws IOException {
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(outputDir, zipEntry);
            if (zipEntry.isDirectory()) {
                newFile.mkdirs();
            } else {
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    private void zipDirectory(File directory, File zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zos = new ZipOutputStream(fos)) {
            Path sourcePath = Paths.get(directory.getAbsolutePath());

            Files.walk(sourcePath).filter(path -> !Files.isDirectory(path)).forEach(path -> {
                ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
                try {
                    zos.putNextEntry(zipEntry);
                    Files.copy(path, zos);
                    zos.closeEntry();
                } catch (IOException e) {
                    System.err.println("Error creating ZIP entry for: " + path);
                    e.printStackTrace();
                }
            });
        }
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

    private String createLintedFilename(File originalFile) {
        String originalName = originalFile.getName();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = dateFormat.format(new Date());
        if (originalName.endsWith(".pptx")) {
            return originalName.replace(".pptx", "_linted_" + timestamp + ".pptx");
        } else {
            return originalName + "_linted_" + timestamp + ".pptx";
        }
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}