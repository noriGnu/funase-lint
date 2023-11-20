package funaselint.rules;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AmbiguousWordRule extends Rule {

    // 警告メッセージを出力する曖昧な言葉を設定
    private static final List<String> ambiguousWords = Arrays.asList("かなり", "非常に", "とても");

    @Override
    public List<String> applicableFilesOrFolders() {
        return List.of("ppt/slides/");
    }

    @Override
    public boolean checkCondition(Document doc, File file) {
        NodeList textNodes = doc.getElementsByTagName("a:t"); // <a:t> タグに含まれるテキストを取得
        for (int i = 0; i < textNodes.getLength(); i++) {
            Node textNode = textNodes.item(i);
            String textContent = textNode.getTextContent();

            // 曖昧な言葉が含まれているかどうかを確認
            for (String ambiguousWord : ambiguousWords) {
                if (textContent.contains(ambiguousWord)) {
                    System.out.println("Warning: 主観的評価は取り除きなさいな。 File: " + file.getPath() + ", Text: " + textContent);
                    return true;
                }
            }
        }
        return false;
    }
}