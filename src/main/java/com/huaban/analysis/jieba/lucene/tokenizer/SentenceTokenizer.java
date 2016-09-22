package com.huaban.analysis.jieba.lucene.tokenizer;

import com.huaban.analysis.jieba.util.CharacterUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;

/**
 * Created by Lanxiaowei
 */
public class SentenceTokenizer extends Tokenizer {
    private final StringBuilder buffer = new StringBuilder();
    private int tokenStart = 0;
    private int tokenEnd = 0;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

    public SentenceTokenizer() {
        super();
    }

    public SentenceTokenizer(AttributeFactory factory) {
        super(factory);
    }

    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();
        buffer.setLength(0);
        int ci;
        char ch, pch;
        boolean atBegin = true;
        tokenStart = tokenEnd;
        ci = input.read();
        ch = (char) ci;

        while (true) {
            if (ci == -1) {
                break;
            } else if (CharacterUtils.isPunction(ch)) {
                buffer.append(ch);
                tokenEnd++;
                break;
            } else if (atBegin && CharacterUtils.isSpace(ch)) {
                tokenStart++;
                tokenEnd++;
                ci = input.read();
                ch = (char) ci;
            } else {
                buffer.append(ch);
                atBegin = false;
                tokenEnd++;
                pch = ch;
                ci = input.read();
                ch = (char) ci;
                if (CharacterUtils.isTwoSpace(ch,pch)) {
                    tokenEnd++;
                    break;
                }
            }
        }
        if (buffer.length() == 0) {
            return false;
        }
        termAtt.setEmpty().append(buffer);
        offsetAtt.setOffset(correctOffset(tokenStart),
                correctOffset(tokenEnd));
        typeAtt.setType("sentence");
        return true;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        tokenStart = tokenEnd = 0;
    }

    @Override
    public void end() {
        final int finalOffset = correctOffset(tokenEnd);
        offsetAtt.setOffset(finalOffset, finalOffset);
    }
}
