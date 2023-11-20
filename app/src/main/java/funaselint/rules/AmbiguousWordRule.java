package funaselint.rules;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AmbiguousWordRule extends Rule {

    private static final List<String> ambiguousWords = Arrays.asList("かなり", "非常に");

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

            // "?"が含まれているかどうかを確認
            if (textContent.contains("?")) {
                System.out.println("Warning: \"?\"が使用されています。 File: " + file.getPath() + ", Text: " + textContent);
                System.out.println("?とか使うのやめようよ。子どもの文章じゃあるまいし。");
                return true;
            }

            // 曖昧な言葉が含まれているかどうかを確認
            for (String ambiguousWord : ambiguousWords) {
                if (textContent.contains(ambiguousWord)) {
                    System.out.println("Warning: 曖昧な言葉が使用されています。 File: " + file.getPath() + ", Text: " + textContent);
                    System.out.println("可能な限り主観的な評価を取り除くことが工学の文章の考え方です。");
                    return true;
                }
            }
        }
        return false;
    }
}