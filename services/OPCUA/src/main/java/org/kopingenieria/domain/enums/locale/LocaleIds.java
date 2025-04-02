package org.kopingenieria.domain.enums.locale;

public enum LocaleIds {
    ENGLISH("en-US"),
    SPANISH("es-ES"),
    FRENCH("fr-FR"),
    GERMAN("de-DE"),
    CHINESE("zh-CN"),
    JAPANESE("ja-JP"),
    KOREAN("ko-KR"),
    ITALIAN("it-IT"),
    PORTUGUESE("pt-PT"),
    RUSSIAN("ru-RU"),
    ARABIC("ar-SA"),
    HINDI("hi-IN"),
    DUTCH("nl-NL"),
    SWEDISH("sv-SE"),
    TURKISH("tr-TR"),
    POLISH("pl-PL"),
    GREEK("el-GR"),
    HEBREW("he-IL"),
    THAI("th-TH"),
    INDONESIAN("id-ID"),
    BENGALI("bn-BD"),
    TAMIL("ta-IN"),
    URDU("ur-PK"),
    MALAYALAM("ml-IN"),
    SLOVAK("sk-SK"),
    SLOVENIAN("sl-SI"),
    CZECH("cs-CZ"),
    HUNGARIAN("hu-HU"),
    VIETNAMESE("vi-VN"),
    ROMANIAN("ro-RO"),
    SLOVENE("sl-SI"),
    CROATIAN("hr-HR"),
    SERBIAN("sr-RS"),
    UKRAINIAN("uk-UA"),
    MALAY("ms-MY"),
    BULGARIAN("bg-BG"),
    KAZAKH("kk-KZ"),
    TELUGU("te-IN"),
    MARATHI("mr-IN");

    private final String localeId;

    LocaleIds(String localeId) {
        this.localeId = localeId;
    }

    public String getLocaleId() {
        return localeId;
    }
}
