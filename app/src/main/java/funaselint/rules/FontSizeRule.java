package funaselint.rules;

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FontSizeRule extends Rule {

    @Override
    public List<String> applicableFilesOrFolders() {
        return List.of("ppt/slides/");
    }

    @Override
    public boolean checkCondition(Document doc, File file) {
        NodeList titleNodes = doc.getElementsByTagName("p:title"); // スライドタイトルを表すノード
        NodeList textNodes = doc.getElementsByTagName("a:t"); // <a:t> タグに含まれるテキストを取得

        int countFontSizeAbove40 = 0; // 40pt以上のフォントサイズの数
        int slideTitleFontSize = 0; // スライドタイトルのフォントサイズ
        boolean hasFontSizeLessThan22 = false; // 22pt未満のフォントサイズが存在するかどうか

        // スライドタイトルのフォントサイズをチェック
        for (int i = 0; i < titleNodes.getLength(); i++) {
            Element titleNode = (Element) titleNodes.item(i);
            String fontSizeString = titleNode.getAttribute("sz");
            
            // フォントサイズを整数に変換
            try {
                int fontSize = Integer.parseInt(fontSizeString);
                if (fontSize >= 40) {
                    countFontSizeAbove40++;  // 40pt以上のものをカウント
                }
                if (fontSize > slideTitleFontSize) {
                    slideTitleFontSize = fontSize;
                }
            } catch (NumberFormatException e) {
                // 数値変換エラーが発生した場合はスキップ
            }
        }

        // その他のテキストのフォントサイズをチェック
        for (int i = 0; i < textNodes.getLength(); i++) {
            Element textNode = (Element) textNodes.item(i);
            String fontSizeString = textNode.getAttribute("sz");

            // フォントサイズを整数に変換
            try {
                int fontSize = Integer.parseInt(fontSizeString);
                if (fontSize >= 40) {
                    // フォントサイズが40pt以上のものがあれば警告
                    System.out.println("Warning: フォントサイズが40pt以上のテキストが存在します。 File: " + file.getPath() + ", Font Size: " + fontSize);
                    return true;
                } else if (fontSize < 22) {
                    // フォントサイズが22pt未満のものがあれば警告
                    hasFontSizeLessThan22 = true;
                }
            } catch (NumberFormatException e) {
                // 数値変換エラーが発生した場合はスキップ
            }
        }

        // 警告条件を確認し、警告を表示

        // スライドタイトルが40pt以上ではない、または40pt以上のフォントが複数個存在するとき
        if (countFontSizeAbove40 != 1)  {
            System.out.println("Warning: スライドタイトルがそのスライドで一番大きい文字じゃないといけないのよ。 File: " + file.getPath());
            return true;
            
            // フォントサイズが22pt未満のものがあるとき
        } else if (hasFontSizeLessThan22) {
            System.out.println("Warning: 本文の文字の大きさは22~38ptぐらいまでにしなさいな。読めないでしょ。 File: " + file.getPath());
            return true;
        }

        return false; // 警告がない場合は false を返す
    }
}