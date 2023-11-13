package funaselint.rules;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;

public abstract class Rule {

    public abstract List<String> applicableFilesOrFolders();

    public abstract boolean checkCondition(Document doc, File file);

    public String toString() {
        return getClass().getSimpleName();
    }
}