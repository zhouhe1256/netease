package netease.zh.com.neteasemaven.netease.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    private StringUtil() {
    }

    /**
     * isNotEmpty
     *
     * @param string
     * @return
     */
    public static boolean isNotEmpty(String string) {
        return string != null && !"".equals(string.trim()) && !"null".equals(string.trim());
    }

    /**
     * 判断字符串是否为空
     *
     * @param string
     * @return 空返回true, 非空返回false
     */
    public static boolean isEmpty(String string) {
        return !isNotEmpty(string);
    }

    /**
     * trim
     *
     * @param string
     * @return
     */
    public static String trim(String string) {
        if (string == null || string.equals("null"))
            return "";
        else
            return string.trim();
    }

    /**
     * @param o
     * @return
     */
    public static String toString(Object o) {
        return o == null ? "" : o.toString();
    }

    /**
     * 字符串模糊处理，*号代替
     *
     * @param string 原字符
     * @param start  模糊开始下标
     * @param end    模糊结束下标
     * @return 138****5678
     */
    public static String formatStringVague(String string, int start, int end) {
        if (StringUtil.isEmpty(string)) {
            return string;
        }

        if (start < 0 || end > (string.length() - 1)) {
            return string;
        }

        StringBuilder sb = new StringBuilder();
        char[] c = string.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (i >= start && i <= end) {
                sb.append("*");
            } else {
                sb.append(c[i]);
            }
        }
        return sb.toString();
    }

    /**
     * 将一个字符串转换成整型值，如果 number 不能转换成整型则返回 defaultValue
     *
     * @param number 一个数字字符串
     * @return 转换后的整型值。
     */
    public static int StringToInt(String number, int defaultValue) {
        if (StringUtil.isEmpty(number)) {
            return defaultValue;
        } else {
            try {
                return Integer.valueOf(number);
            } catch (Exception e) {
                return defaultValue;
            }
        }
    }

    /**
     * 将一个字符串转换成浮点型值，如果 number 不能转换成整型则返回 defaultValue
     *
     * @param number 一个数字字符串
     * @return 转换后的整型值。
     */
    public static Float StringToFloat(String number, float defaultValue) {
        if (StringUtil.isEmpty(number)) {
            return defaultValue;
        } else {
            try {
                return Float.valueOf(number);
            } catch (Exception e) {
                return defaultValue;
            }
        }
    }

    /**
     * 。是否是纯数字字符串
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }


    /**
     * 判定输入汉字
     *
     * @param c
     * @return
     */
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    /**
     * 检测String是否全是中文
     *
     * @param name
     * @return
     */
    public static boolean checkNameChese(String name) {
        boolean res = true;
        char[] cTemp = name.toCharArray();
        for (int i = 0; i < name.length(); i++) {
            if (!isChinese(cTemp[i])) {
                res = false;
                break;
            }
        }
        return res;
    }
}
