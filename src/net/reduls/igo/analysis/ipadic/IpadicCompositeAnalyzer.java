package net.reduls.igo.analysis.ipadic;

import com.sun.istack.internal.Nullable;
import net.reduls.igo.Tagger;
import net.reduls.igo.analysis.ipadic.IpadicFilter;
import net.reduls.igo.analysis.ipadic.IpadicStopFilter;
import org.apache.lucene.analysis.*;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import net.reduls.igo.analysis.ipadic.IpadicTokenizer;

import java.io.IOException;
import java.io.Reader;

public class IpadicCompositeAnalyzer extends Analyzer {

    private final Tagger tagger;

    /**
     * {@link Tagger}インスタンスを受け取りアナライザを作成する。
     *
     * @params tagger 形態素解析クラス。辞書にはIPA辞書が指定されている必要がある
     */
    public IpadicCompositeAnalyzer(Tagger tagger) {
	  this.tagger = tagger;
    }

  public TokenStream tokenStream(String fieldName, Reader reader) {

    TokenStream result = new IpadicStopFilter(
            new IpadicFilter(
                            new StopFilter(true,
                              new LowerCaseFilter(
                                new StandardFilter(
                                        new ASCIIFoldingFilter(
                                  new StandardTokenizer(Version.LUCENE_30, reader)))),
//                                    new IpadicTokenizer(tagger, reader))),
                                    StopAnalyzer.ENGLISH_STOP_WORDS_SET), tagger), tagger);
      return result;
  }
}