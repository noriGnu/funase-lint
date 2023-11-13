package funaselint.rules;

import java.io.File;

import org.w3c.dom.Document;

public interface AutoFixable {
    void autoFix(Document doc, File file);
}