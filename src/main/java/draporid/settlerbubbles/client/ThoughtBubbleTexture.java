package draporid.settlerbubbles.client;

import java.awt.Color;

import necesse.gfx.gameTexture.GameTexture;

/**
 * Generates a scalable, pixel-art thought bubble atlas in the same 4x3 layout
 * used by Necesse chat bubbles. Keeping it generated lets the shape stay crisp
 * at every text size without shipping a large texture for a handful of pixels.
 */
final class ThoughtBubbleTexture {
    private static final int TILE_SIZE = 32;
    private static final int OUTLINE_RADIUS = 2;
    private static final Color FILL = new Color(250, 252, 255, 255);
    private static final Color INNER_EDGE = new Color(190, 205, 231, 255);
    private static final Color OUTLINE = new Color(59, 72, 105, 255);

    private static GameTexture texture;

    private ThoughtBubbleTexture() {
    }

    static synchronized GameTexture get() {
        if (texture == null) {
            texture = createTexture();
        }
        return texture;
    }

    private static GameTexture createTexture() {
        GameTexture result = new GameTexture("settlerbubbles_thoughtbubble",
                TILE_SIZE * 4, TILE_SIZE * 3);
        for (int tileY = 0; tileY < 3; tileY++) {
            for (int tileX = 0; tileX < 3; tileX++) {
                drawFrameTile(result, tileX, tileY);
            }
        }
        drawThoughtTrail(result, 3 * TILE_SIZE, TILE_SIZE);
        return result.makeFinal();
    }

    private static void drawFrameTile(GameTexture texture, int tileX, int tileY) {
        int baseX = tileX * TILE_SIZE;
        int baseY = tileY * TILE_SIZE;
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                if (insideFrame(tileX, tileY, x, y)) {
                    texture.setPixel(baseX + x, baseY + y,
                            touchesOutside(tileX, tileY, x, y, 1) ? INNER_EDGE : FILL);
                } else if (touchesInside(tileX, tileY, x, y, OUTLINE_RADIUS)) {
                    texture.setPixel(baseX + x, baseY + y, OUTLINE);
                }
            }
        }
    }

    private static boolean insideFrame(int tileX, int tileY, int x, int y) {
        boolean horizontal = true;
        boolean vertical = true;
        if (tileX == 0) {
            horizontal = x >= cloudBoundary(y);
        } else if (tileX == 2) {
            horizontal = x < TILE_SIZE - cloudBoundary(y);
        }
        if (tileY == 0) {
            vertical = y >= cloudBoundary(x);
        } else if (tileY == 2) {
            vertical = y < TILE_SIZE - cloudBoundary(x);
        }
        return horizontal && vertical;
    }

    private static int cloudBoundary(int position) {
        int value = Math.floorMod(position, 16);
        int distance = Math.abs(value - 7);
        if (distance <= 2) {
            return 2;
        }
        if (distance <= 4) {
            return 3;
        }
        if (distance <= 6) {
            return 5;
        }
        return 7;
    }

    private static boolean touchesInside(int tileX, int tileY, int x, int y, int radius) {
        for (int offsetY = -radius; offsetY <= radius; offsetY++) {
            for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                if (Math.abs(offsetX) + Math.abs(offsetY) <= radius
                        && insideFrame(tileX, tileY, x + offsetX, y + offsetY)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean touchesOutside(int tileX, int tileY, int x, int y, int radius) {
        for (int offsetY = -radius; offsetY <= radius; offsetY++) {
            for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                if (Math.abs(offsetX) + Math.abs(offsetY) <= radius
                        && !insideFrame(tileX, tileY, x + offsetX, y + offsetY)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void drawThoughtTrail(GameTexture texture, int baseX, int baseY) {
        drawPixelCircle(texture, baseX + 15, baseY + 8, 6);
        drawPixelCircle(texture, baseX + 18, baseY + 18, 4);
        drawPixelCircle(texture, baseX + 20, baseY + 27, 2);
    }

    private static void drawPixelCircle(GameTexture texture, int centerX, int centerY, int radius) {
        int outerRadius = radius + OUTLINE_RADIUS;
        for (int y = -outerRadius; y <= outerRadius; y++) {
            for (int x = -outerRadius; x <= outerRadius; x++) {
                int distance = x * x + y * y;
                if (distance <= radius * radius) {
                    texture.setPixel(centerX + x, centerY + y,
                            distance >= (radius - 1) * (radius - 1) ? INNER_EDGE : FILL);
                } else if (distance <= outerRadius * outerRadius) {
                    texture.setPixel(centerX + x, centerY + y, OUTLINE);
                }
            }
        }
    }
}
