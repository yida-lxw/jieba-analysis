package com.huaban.analysis.jieba.lucene.tokenfilter;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.huaban.analysis.jieba.util.CharacterUtils;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Lanxiaowei
 */
public class JiebaTokenFilter extends TokenFilter {
    private JiebaSegmenter segmenter;

    private Iterator<SegToken> tokenIter;
    private List<SegToken> array;
    /**分词模式：default、index、search、query*/
    private String mode;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

    public JiebaTokenFilter(String mode, TokenStream input) {
        super(input);
        this.mode = mode;
        segmenter = new JiebaSegmenter(this.mode);
    }

    public JiebaTokenFilter(JiebaSegmenter.SegMode mode, TokenStream input) {
        super(input);
        if(null != mode) {
            this.mode = mode.getValue();
        }
        segmenter = new JiebaSegmenter(this.mode);
    }

    public JiebaTokenFilter(TokenStream input) {
        super(input);
        segmenter = new JiebaSegmenter(mode);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (tokenIter == null || !tokenIter.hasNext()) {
            if (input.incrementToken()) {
                if (null == this.mode || "".equals(this.mode) ||
                        this.mode.equalsIgnoreCase("index") ||
                        this.mode.equalsIgnoreCase("index")) {
                    array = segmenter.process(termAtt.toString(),
                            JiebaSegmenter.SegMode.INDEX);
                } else if (this.mode.equalsIgnoreCase("query") ||
                        this.mode.equalsIgnoreCase("search")) {
                    array = new ArrayList<SegToken>();
                    String token = termAtt.toString();
                    char[] ctoken = token.toCharArray();
                    for (int i = 0; i < ctoken.length; i++) {
                        ctoken[i] = CharacterUtils.regularize(ctoken[i]);
                    }
                    token = String.valueOf(ctoken);
                    array.add(new SegToken(token, 0, token.length()));
                } else {
                    array = segmenter.process(termAtt.toString(),
                            JiebaSegmenter.SegMode.DEFAULT);
                }
                tokenIter = array.iterator();
                if (!tokenIter.hasNext()) {
                    return false;
                }
            } else {
                return false;
            }
        }
        clearAttributes();
        SegToken token = tokenIter.next();
        offsetAtt.setOffset(token.startOffset, token.endOffset);
        String tokenString = token.word;
        termAtt.copyBuffer(tokenString.toCharArray(), 0, tokenString.length());
        //词性标注暂未实现
        typeAtt.setType("word");
        return true;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        tokenIter = null;
    }
}
