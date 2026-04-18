package com.example.androiddemo.tools;

import java.util.HashSet;
import java.util.Set;

/**
 * 敏感词数据库
 */
public class SensitiveWordDb {

    private static final Set<String> SENSITIVE_WORDS = new HashSet<>();

    static {
        // 政治敏感词
        SENSITIVE_WORDS.add("分裂");
        SENSITIVE_WORDS.add("反动");
        SENSITIVE_WORDS.add("颠覆");
        SENSITIVE_WORDS.add("暴动");
        SENSITIVE_WORDS.add("政变");

        // 暴力相关
        SENSITIVE_WORDS.add("杀人");
        SENSITIVE_WORDS.add("殴打");
        SENSITIVE_WORDS.add("砍伤");
        SENSITIVE_WORDS.add("炸药");
        SENSITIVE_WORDS.add("武器");

        // 色情相关
        SENSITIVE_WORDS.add("色情");
        SENSITIVE_WORDS.add("淫秽");
        SENSITIVE_WORDS.add("裸体");
        SENSITIVE_WORDS.add("性交易");
        SENSITIVE_WORDS.add("裸聊");

        // 赌博相关
        SENSITIVE_WORDS.add("赌博");
        SENSITIVE_WORDS.add("赌场");
        SENSITIVE_WORDS.add("赌注");
        SENSITIVE_WORDS.add("博彩");
        SENSITIVE_WORDS.add("投注");

        // 毒品相关
        SENSITIVE_WORDS.add("毒品");
        SENSITIVE_WORDS.add("吸毒");
        SENSITIVE_WORDS.add("贩毒");
        SENSITIVE_WORDS.add("海洛因");
        SENSITIVE_WORDS.add("冰毒");

        // 诈骗相关
        SENSITIVE_WORDS.add("诈骗");
        SENSITIVE_WORDS.add("欺诈");
        SENSITIVE_WORDS.add("骗子");
        SENSITIVE_WORDS.add("钓鱼");
        SENSITIVE_WORDS.add("盗刷");

        // 谣言虚假信息
        SENSITIVE_WORDS.add("谣言");
        SENSITIVE_WORDS.add("造谣");
        SENSITIVE_WORDS.add("传谣");
        SENSITIVE_WORDS.add("虚假");
        SENSITIVE_WORDS.add("伪造");

        // 违禁品买卖
        SENSITIVE_WORDS.add("假证");
        SENSITIVE_WORDS.add("代办证件");
        SENSITIVE_WORDS.add("非法集资");
        SENSITIVE_WORDS.add("传销");
        SENSITIVE_WORDS.add("走私");
    }

    /**
     * 检测文本中是否包含敏感词
     * @param text 待检测的文本
     * @return 包含的敏感词集合，如果无敏感词则返回空集合
     */
    public static Set<String> detect(String text) {
        Set<String> found = new HashSet<>();
        if (text == null || text.isEmpty()) {
            return found;
        }

        String lowerText = text.toLowerCase();

        for (String word : SENSITIVE_WORDS) {
            if (lowerText.contains(word.toLowerCase())) {
                found.add(word);
            }
        }

        return found;
    }

    /**
     * 检测文本中是否包含敏感词
     * @param text 待检测的文本
     * @return true表示包含敏感词，false表示不包含
     */
    public static boolean contains(String text) {
        return !detect(text).isEmpty();
    }

    /**
     * 获取所有敏感词
     * @return 敏感词集合
     */
    public static Set<String> getAllWords() {
        return new HashSet<>(SENSITIVE_WORDS);
    }

    /**
     * 获取敏感词数量
     * @return 敏感词数量
     */
    public static int getWordCount() {
        return SENSITIVE_WORDS.size();
    }
}