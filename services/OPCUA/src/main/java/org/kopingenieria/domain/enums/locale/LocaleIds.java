package org.kopingenieria.domain.enums.locale;

public enum LocaleIds {
    ENGLISH("en"),
    SPANISH("es"),
    FRENCH("fr"),
    GERMAN("de"),
    CHINESE("zh"),
    JAPANESE("ja"),
    KOREAN("ko"),
    ITALIAN("it"),
    PORTUGUESE("pt"),
    RUSSIAN("ru"),
    ARABIC("ar"),
    HINDI("hi"),
    DUTCH("nl"),
    SWEDISH("sv"),
    TURKISH("tr"),
    POLISH("pl"),
    GREEK("el"),
    HEBREW("he");

    private final String localeId;

    LocaleIds(String localeId) {
        this.localeId = localeId;
    }

    public String getLocaleId() {
        return localeId;
    }
}
