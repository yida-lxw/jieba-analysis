package com.huaban.analysis.jieba.util;

import java.util.regex.Pattern;

/**
 * 字符操作工具类
 */
public class CharacterUtils {
    public static Pattern reSkip = Pattern.compile("(\\d+\\.\\d+|[a-zA-Z0-9]+)");
    private static final char[] connectors = new char[] { '+', '#', '&', '.', '_', '-' };
    /**标点符号*/
    private final static String PUNCTION = ".。,，、:：·\"“'‘!！;；?？";
    /**空白字符*/
    private final static String SPACES = " 　\t\r\n";

    public static boolean isChineseLetter(char ch) {
        if (ch >= 0x4E00 && ch <= 0x9FA5) {
            return true;
        }
        return false;
    }


    public static boolean isEnglishLetter(char ch) {
        if ((ch >= 0x0041 && ch <= 0x005A) || (ch >= 0x0061 && ch <= 0x007A))
            return true;
        return false;
    }


    public static boolean isDigit(char ch) {
        if (ch >= 0x0030 && ch <= 0x0039)
            return true;
        return false;
    }


    public static boolean isConnector(char ch) {
        for (char connector : connectors)
            if (ch == connector)
                return true;
        return false;
    }


    public static boolean ccFind(char ch) {
        if (isChineseLetter(ch)) {
            return true;
        }
        if (isEnglishLetter(ch)) {
            return true;
        }
        if (isDigit(ch)) {
            return true;
        }
        if (isConnector(ch)) {
            return true;
        }
        return false;
    }


    /**
     * 全角 to 半角,大写 to 小写
     * 
     * @param input
     *            输入字符
     * @return 转换后的字符
     */
    public static char regularize(char input) {
        if (input == 12288) {
            return 32;
        }
        //全角 to 半角
        else if (input > 65280 && input < 65375) {
            return (char) (input - 65248);
        }
        //大写 to 小写
        else if (input >= 'A' && input <= 'Z') {
            return (input += 32);
        }
        return input;
    }

    /**
     * 判断是否为标点符号
     * @param ch
     * @return
     */
    public static boolean isPunction(char ch) {
        return PUNCTION.indexOf(ch) != -1;
    }

    /**
     * 判断是否为空白字符
     * @param ch
     * @return
     */
    public static boolean isSpace(char ch) {
        return SPACES.indexOf(ch) != -1;
    }

    /**
     * 判断是否为双空白字符
     * @param ch
     * @param pch
     * @return
     */
    public static boolean isTwoSpace(char ch,char pch) {
        return SPACES.indexOf(ch) != -1 && SPACES.indexOf(pch) != -1;
    }
}
