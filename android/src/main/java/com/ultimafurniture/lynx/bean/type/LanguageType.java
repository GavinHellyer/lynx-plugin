package com.ultimafurniture.lynx.bean.type;

/**
 * @author naz
 *         Date 2020/4/14
 */
public enum LanguageType {
    /**
     * 中文
     */
    CHINESE("ch"),
    /**
     * 英文
     */
    ENGLISH("en");

    private String language;

    LanguageType(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language == null ? "" : language;
    }
}
