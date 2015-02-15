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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

public class IpadicFilter extends TokenFilter {

    private AttributeSource.State current;
    private Tagger tagger;
    private StringBuffer  stringBuffer;
    private TermAttribute termAttr;
    private TypeAttribute typeAttr;
    private OffsetAttribute offsetAttr;
    private PositionIncrementAttribute positionIncrementAttribute;
    private TokenStream tokenStream;
    private int lastOffset;
    private int lastPositionIncrement;

    public IpadicFilter(TokenStream in, Tagger tagger) {
        super(in);

        this.tagger = tagger;
        tokenStream = null;
        stringBuffer = new StringBuffer();
        this.termAttr = addAttribute(TermAttribute.class);
        this.typeAttr = addAttribute(TypeAttribute.class);
        this.offsetAttr = addAttribute(OffsetAttribute.class);
        this.positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);
        lastOffset = 0;
        lastPositionIncrement = -1;
    }

    public  boolean incrementToken() throws IOException {

        final boolean next;

        if(tokenStream != null) {
            if(!tokenStream.incrementToken()) {
                if(current != null) restoreState(current);
           //     System.out.println("Next Term: " + termAttr.term());
           //     if(current != null) input.incrementToken();
                tokenStream = null;
                lastOffset = this.offsetAttr.endOffset();
                return (current != null);
            }
            TermAttribute ip_termAttr = tokenStream.addAttribute(TermAttribute.class);
            TypeAttribute ip_typeAttr = tokenStream.addAttribute(TypeAttribute.class);
            OffsetAttribute ip_offsetAttr = tokenStream.addAttribute(OffsetAttribute.class);
            PositionIncrementAttribute ip_positionIncrementAttr = tokenStream.addAttribute(PositionIncrementAttribute.class);
            this.termAttr.setTermBuffer( ip_termAttr.term());
            this.typeAttr.setType(ip_typeAttr.type());
            this.offsetAttr.setOffset(lastOffset+ip_offsetAttr.startOffset(), lastOffset+ip_offsetAttr.endOffset());
            if(lastPositionIncrement < 0)
                this.positionIncrementAttribute.setPositionIncrement(ip_positionIncrementAttr.getPositionIncrement());
            else {
                this.positionIncrementAttribute.setPositionIncrement(lastPositionIncrement);
                lastPositionIncrement = -1;
            }
//            System.out.println("Term: " + this.termAttr.term());
//            System.out.println("Offset- start: " + ip_offsetAttr.startOffset() + " end: " + ip_offsetAttr.endOffset());

            return true;
        }

        if(!(next = input.incrementToken()) || typeAttr.type() != "<CJ>")
        if(stringBuffer.length() < 1)  return next;
        else {
            current = next ? captureState() : null;
            tokenStream = new IpadicTokenizer(tagger, new StringReader(stringBuffer.toString()));
            stringBuffer.setLength(0);

            return this.incrementToken();
        }

        if(stringBuffer.length() < 1) { lastOffset = this.offsetAttr.startOffset(); lastPositionIncrement = this.positionIncrementAttribute.getPositionIncrement(); }
        stringBuffer.append(this.termAttr.term());


        return this.incrementToken();
    }
}
