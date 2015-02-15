package net.reduls.igo.analysis.ipadic;

import net.reduls.igo.Morpheme;
import net.reduls.igo.Tagger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;

import java.io.*;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

public class IpadicStopFilter extends TokenFilter {

    private Tagger tagger;
    private TypeAttribute typeAttr;
    private TermAttribute termAttr;
    private PositionIncrementAttribute positionIncrementAttribute;
    private int lastPositionIncrement;
    private String[][] typeList = { { "名詞","接尾", "形容動詞語幹" }, { "名詞","非自立","助動詞語幹" }, { "連体詞" }, { "接続詞" }, { "動詞", "自立" }, { "動詞","非自立" }, { "助詞","格助詞","一般" }, { "助詞", "接続助詞" }, {"副詞", "助詞類接続"}, { "副詞", "助詞類接続" }, { "助詞", "連体化" } };
    private String[] stopList = { "な", "ぼく", "お", "自分", "を", "う", "って", "よ", "の", "て", "で", "です", "てる", "。", "レ", "ん", "ル", "ア", "ね", "た", "だ", "は", "に", "も", "ない", "する", "いい", "いる", "なぁ", "か", "が", "れる", "ある", "と", "ちょっと", "http", "rt", "ｗｗｗ", "www", "・", "･" };

    public IpadicStopFilter(TokenStream in, Tagger tagger) {
        super(in);

        this.tagger = tagger;
        this.termAttr = addAttribute(TermAttribute.class);
        this.typeAttr = addAttribute(TypeAttribute.class);
        this.positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);
        lastPositionIncrement = -1;
    }

    public  boolean incrementToken() throws IOException {

        if(!input.incrementToken()) return false;

        if(isCheck()) {
            if(-1 < lastPositionIncrement) positionIncrementAttribute.setPositionIncrement(lastPositionIncrement);
            lastPositionIncrement = -1;
            return true;
        }

        if(lastPositionIncrement < 0)
            lastPositionIncrement = this.positionIncrementAttribute.getPositionIncrement()+1;
        else lastPositionIncrement++;

        return incrementToken();
    }

    private boolean isStopMatch() {

        String term = this.termAttr.term().trim();
        for(String item:stopList) if(item.compareTo(term) == 0) return true;

        return false;
    }
    private boolean isMatch(String[] strType) {

        if(isStopMatch()) return true;
        if(strType.length < 3 && strType[0].compareTo("名詞") == 0) return false;

     //   if(1 < termAttr.termLength()) return false;
        for(String[] item : typeList) {
            int incr = 0; boolean state = true;
            for(String attr:item) {    if(strType.length <= incr) break;
                state &= (attr.compareTo(strType[incr++]) == 0);
        //        System.out.println("attr: " + attr + " term: " + termAttr.term() + " state: " + state);

                }
            if(state) return 0 < incr;
        }

        return false;
    }

    private boolean isCheck() {
        String[] strType = this.typeAttr.type().split(",");


        return strType.length < 1 //|| Character.getType(strType[0].codePointAt(0)) != 5
                || !isMatch(strType);

    }

}
