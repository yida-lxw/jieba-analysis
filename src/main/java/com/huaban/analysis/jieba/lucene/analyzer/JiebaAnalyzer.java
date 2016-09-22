package com.huaban.analysis.jieba.lucene.analyzer;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.conf.Configuration;
import com.huaban.analysis.jieba.conf.DefaultConfiguration;
import com.huaban.analysis.jieba.lucene.tokenfilter.JiebaTokenFilter;
import com.huaban.analysis.jieba.lucene.tokenizer.SentenceTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

/**
 * Created by Lanxiaowei
 */
public class JiebaAnalyzer extends Analyzer {
    /**分词模式：default、index、search、query*/
    private String mode;

    /**分词器配置对象*/
    private static Configuration config;

    static {
        config = DefaultConfiguration.getInstance();
    }

    public JiebaAnalyzer() {
        this.mode = JiebaSegmenter.SegMode.DEFAULT.getValue();
    }

    public JiebaAnalyzer(String mode) {
        this.mode = mode;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new SentenceTokenizer();
        TokenStream tokenFilter = new JiebaTokenFilter(mode, tokenizer);
        return new TokenStreamComponents(tokenizer, tokenFilter);
    }
}
