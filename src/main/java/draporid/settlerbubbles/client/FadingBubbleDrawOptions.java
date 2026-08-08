package draporid.settlerbubbles.client;

import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawOptions.texture.SharedTextureDrawOptions;
import necesse.gfx.gameTexture.GameTexture;

final class FadingBubbleDrawOptions {
    private static final int SPRITE_SIZE = 32;
    private static final int PADDING = 4;

    private FadingBubbleDrawOptions() {
    }

    static DrawOptions create(GameTexture texture, int width, int height, int indicatorX,
                              int x, int y, float alpha) {
        SharedTextureDrawOptions options = new SharedTextureDrawOptions(texture);
        int totalWidth = width + PADDING * 2;
        int totalHeight = height + PADDING * 2;
        int drawX = x - PADDING;
        int drawY = y - PADDING;
        int left = Math.min(SPRITE_SIZE, totalWidth / 2);
        int right = Math.min(SPRITE_SIZE, totalWidth - left);
        int top = Math.min(SPRITE_SIZE, totalHeight / 2);
        int bottom = Math.min(SPRITE_SIZE, totalHeight - top);

        options.addSpriteSection(0, 0, SPRITE_SIZE, 0, left, 0, top).pos(drawX, drawY);
        options.addSpriteSection(0, 2, SPRITE_SIZE, 0, left, SPRITE_SIZE - bottom, SPRITE_SIZE)
                .pos(drawX, drawY + totalHeight - bottom);
        options.addSpriteSection(2, 0, SPRITE_SIZE, SPRITE_SIZE - right, SPRITE_SIZE, 0, top)
                .pos(drawX + totalWidth - right, drawY);
        options.addSpriteSection(2, 2, SPRITE_SIZE, SPRITE_SIZE - right, SPRITE_SIZE,
                        SPRITE_SIZE - bottom, SPRITE_SIZE)
                .pos(drawX + totalWidth - right, drawY + totalHeight - bottom);

        int middleWidth = totalWidth - SPRITE_SIZE * 2;
        for (int offsetX = 0; offsetX < middleWidth; offsetX += SPRITE_SIZE) {
            int sectionWidth = Math.min(SPRITE_SIZE, middleWidth - offsetX);
            options.addSpriteSection(1, 0, SPRITE_SIZE, 0, sectionWidth, 0, top)
                    .pos(drawX + SPRITE_SIZE + offsetX, drawY);
            options.addSpriteSection(1, 2, SPRITE_SIZE, 0, sectionWidth,
                            SPRITE_SIZE - bottom, SPRITE_SIZE)
                    .pos(drawX + SPRITE_SIZE + offsetX, drawY + totalHeight - bottom);
        }

        int middleHeight = totalHeight - SPRITE_SIZE * 2;
        for (int offsetY = 0; offsetY < middleHeight; offsetY += SPRITE_SIZE) {
            int sectionHeight = Math.min(SPRITE_SIZE, middleHeight - offsetY);
            options.addSpriteSection(0, 1, SPRITE_SIZE, 0, left, 0, sectionHeight)
                    .pos(drawX, drawY + SPRITE_SIZE + offsetY);
            options.addSpriteSection(2, 1, SPRITE_SIZE, SPRITE_SIZE - right, SPRITE_SIZE,
                            0, sectionHeight)
                    .pos(drawX + totalWidth - right, drawY + SPRITE_SIZE + offsetY);
        }

        for (int offsetX = 0; offsetX < middleWidth; offsetX += SPRITE_SIZE) {
            int sectionWidth = Math.min(SPRITE_SIZE, middleWidth - offsetX);
            for (int offsetY = 0; offsetY < middleHeight; offsetY += SPRITE_SIZE) {
                int sectionHeight = Math.min(SPRITE_SIZE, middleHeight - offsetY);
                options.addSpriteSection(1, 1, SPRITE_SIZE, 0, sectionWidth, 0, sectionHeight)
                        .pos(drawX + SPRITE_SIZE + offsetX, drawY + SPRITE_SIZE + offsetY);
            }
        }

        options.addSprite(3, 1, SPRITE_SIZE)
                .pos(drawX + indicatorX - 16, drawY + totalHeight + PADDING - 16);
        options.forEachDraw(wrapper -> wrapper.alpha(alpha));
        return options::draw;
    }
}
