package funaselint.model;

import java.nio.file.Path;

public class Presentation {
    private Path filePath;

    public Presentation(Path filePath) {
        this.filePath = filePath;
    }

    public Path getFilePath() {
        return this.filePath;
    }

    public String getName() {
        return this.filePath.getFileName().toString();
    }

    public Path getLintedFile(String newFilename) {
        return this.filePath.getParent().resolve(newFilename);
    }
}
