package drapomods.settlerbubbles;

import necesse.gfx.gameFont.FontOptions;

public enum BubbleFontMode {
    GAME_DEFAULT,
    PIXEL,
    SMOOTH;

    public FontOptions apply(FontOptions options) {
        switch (this) {
            case PIXEL:
                return options.forcePixelFont();
            case SMOOTH:
                return options.forceNonPixelFont();
            case GAME_DEFAULT:
            default:
                return options;
        }
    }
}
