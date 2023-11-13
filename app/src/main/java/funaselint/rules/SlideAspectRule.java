package funaselint.rules;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SlideAspectRule extends Rule implements AutoFixable {

    @Override
    public List<String> applicableFilesOrFolders() {
        return List.of("ppt/presentation.xml");
    }

    @Override
    public boolean checkCondition(Document doc, File file) {
        NodeList nodeList = doc.getElementsByTagName("p:sldSz");
        if (nodeList.getLength() > 0) {
            Element sldSz = (Element) nodeList.item(0);
            String cx = sldSz.getAttribute("cx");
            String cy = sldSz.getAttribute("cy");

            // スライドサイズの比率を計算（3:4の比率に近いか）
            try {
                long width = Long.parseLong(cx);
                long height = Long.parseLong(cy);
                double ratio = (double) width / height;
                double expectedRatio = 4.0 / 3.0;

                // 実際の比率と期待される比率が十分に近いかどうかを確認
                return Math.abs(ratio - expectedRatio) > 0.01; // 1%の誤差を許容
            } catch (NumberFormatException e) {
                // 数値変換エラーの場合は条件チェック失敗とみなす
                return false;
            }
        }
        return false;
    }

    @Override
    public void autoFix(Document doc, File file) {
        NodeList nodeList = doc.getElementsByTagName("p:sldSz");
        if (nodeList.getLength() > 0) {
            Element sldSz = (Element) nodeList.item(0);
            sldSz.setAttribute("cx", "9144000"); // 幅を25.4cm（4の比率）に設定
            sldSz.setAttribute("cy", "6858000"); // 高さを19.05cm（3の比率）に設定
        }
    }
}