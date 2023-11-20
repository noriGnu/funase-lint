package funaselint.rules;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PunctuationMarkRule extends Rule implements AutoFixable {

    @Override
    public List<String> applicableFilesOrFolders() {
        return List.of("ppt/slides/"); // relsも含んでしまう問題あり
    }

    @Override
    public boolean checkCondition(Document doc, File file) {
        NodeList textNodes = doc.getElementsByTagName("a:t"); // <a:t> タグに含まれるテキストを取得
        for (int i = 0; i < textNodes.getLength(); i++) {
            Node textNode = textNodes.item(i);
            String textContent = textNode.getTextContent();
            if (textContent.contains(".") || textContent.contains(",") || textContent.contains("、") || textContent.contains("。")) {
                // 文中に句読点が含まれている場合は修正が必要
                return true;
            }
        }
        return false;
    }

    @Override
    public void autoFix(Document doc, File file) {
        NodeList textNodes = doc.getElementsByTagName("a:t");
        for (int i = 0; i < textNodes.getLength(); i++) {
            Node textNode = textNodes.item(i);
            String textContent = textNode.getTextContent();
            
            // 句読点を削除
            textContent = textContent.replace(".", "").replace(",", "").replace("、", "").replace("。", "");

            // 修正したテキストを設定
            textNode.setTextContent(textContent);
        }
    }
}