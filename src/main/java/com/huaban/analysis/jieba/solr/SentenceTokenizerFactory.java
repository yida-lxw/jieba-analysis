package com.huaban.analysis.jieba.solr;

import com.huaban.analysis.jieba.lucene.tokenizer.SentenceTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

import java.util.Map;

/**
 * Created by Lanxiaowei
 * SentenceTokenizer的工厂类  兼容Solr5.x
 */
public class SentenceTokenizerFactory extends TokenizerFactory {

    protected SentenceTokenizerFactory(Map<String, String> args) {
        super(args);
    }

    @Override
    public Tokenizer create(AttributeFactory attributeFactory) {
        return new SentenceTokenizer(attributeFactory);
    }
}
