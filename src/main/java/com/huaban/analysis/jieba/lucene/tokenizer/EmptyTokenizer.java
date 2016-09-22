package com.huaban.analysis.jieba.lucene.tokenizer;

import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeFactory;

/**
 * Created by Lanxiaowei
 * CharTokenizer空实现
 */
public class EmptyTokenizer extends CharTokenizer {
    public EmptyTokenizer() {
        super();
    }

    public EmptyTokenizer(AttributeFactory factory) {
        super(factory);
    }

    /**
     * 是否为Token字符，若返回false，则会被过滤掉
     * @param i
     * @return
     */
    @Override
    protected boolean isTokenChar(int i) {
        return true;
    }
}
