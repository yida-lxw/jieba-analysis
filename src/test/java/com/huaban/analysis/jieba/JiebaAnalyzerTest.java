package com.huaban.analysis.jieba;

import com.huaban.analysis.jieba.lucene.analyzer.JiebaAnalyzer;
import org.apache.lucene.analysis.Analyzer;

import java.io.IOException;

import static com.huaban.analysis.jieba.AnalyzerUtils.displayTokens;

/**
 * Created by Lanxiaowei
 * Jieba分词器简单测试
 */
public class JiebaAnalyzerTest {
    public static void main(String[] args) throws IOException {
        String text = "结巴分词器简单测试";
        Analyzer analyzer = new JiebaAnalyzer(JiebaSegmenter.SegMode.DEFAULT.getValue());
        displayTokens(analyzer, text);
    }
}
