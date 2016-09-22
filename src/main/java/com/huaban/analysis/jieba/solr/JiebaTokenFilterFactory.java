package com.huaban.analysis.jieba.solr;

import com.huaban.analysis.jieba.lucene.tokenfilter.JiebaTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.util.Map;

/**
 * Created by Lanxiaowei
 * JiebaTokenFilter的工厂类  兼容Solr5.x
 */
public class JiebaTokenFilterFactory extends TokenFilterFactory {
    /**分词模式：default、index、search、query*/
    private String mode;

    protected JiebaTokenFilterFactory(Map<String, String> args) {
        super(args);
        this.mode = get(args,"mode");
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new JiebaTokenFilter(mode,tokenStream);
    }
}
