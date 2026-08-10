package drapomods.settlerbubbles;

import necesse.engine.localization.Language;
import necesse.engine.localization.Localization;
import necesse.engine.localization.message.GameMessage;

public enum BubbleLanguage {
    GAME_LANGUAGE(null),
    ENGLISH("en"),
    BRAZILIAN_PORTUGUESE("pt-BR"),
    GERMAN("de"),
    SPANISH("es"),
    FRENCH("fr"),
    DUTCH("nl"),
    POLISH("pl"),
    RUSSIAN("ru"),
    CHINESE_SIMPLIFIED("zh-CN"),
    JAPANESE("ja"),
    KOREAN("kr");

    private final String languageID;

    BubbleLanguage(String languageID) {
        this.languageID = languageID;
    }

    public String translate(GameMessage message) {
        if (message == null) {
            return "";
        }
        Language language = getLanguage();
        return language == null ? message.translate() : message.translateDebug(language);
    }

    public Language getLanguage() {
        return languageID == null ? null : Localization.getLanguageStringID(languageID);
    }

    public String getLanguageID() {
        return languageID;
    }
}
