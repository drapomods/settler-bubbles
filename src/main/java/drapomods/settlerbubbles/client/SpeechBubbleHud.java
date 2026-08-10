package drapomods.settlerbubbles.client;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import drapomods.settlerbubbles.BubbleCategory;
import drapomods.settlerbubbles.BubbleStyle;
import drapomods.settlerbubbles.SettlerBubblesSettings;
import necesse.engine.Settings;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.gfx.GameColor;
import necesse.gfx.camera.GameCamera;
import necesse.gfx.drawOptions.DrawOptions;
import necesse.gfx.drawables.SortedDrawable;
import necesse.gfx.fairType.FairType;
import necesse.gfx.fairType.FairTypeDrawOptions;
import necesse.gfx.forms.components.chat.ChatMessage;
import necesse.gfx.gameFont.FontOptions;
import necesse.entity.mobs.Mob;
import necesse.entity.mobs.PlayerMob;
import necesse.level.maps.hudManager.HudDrawElement;
import necesse.level.maps.hudManager.HudManager;

public class SpeechBubbleHud extends HudDrawElement {
    private static final int MAX_TEXT_WIDTH = 200;
    private static final int MAX_THOUGHT_TEXT_WIDTH = 168;
    private static final int FADE_TIME = 350;

    private final Mob mob;
    private final BubbleCategory category;
    private final BubbleStyle style;
    private final String message;
    private final int duration;
    private final long createdAt = System.nanoTime();
    private long startTime;
    private FairTypeDrawOptions textDraw;
    private int textWidth;
    private int textHeight;
    private Rectangle lastWorldBounds;

    public SpeechBubbleHud(Mob mob, BubbleCategory category, BubbleStyle style,
                           String message, int duration) {
        this.mob = mob;
        this.category = category;
        this.style = style;
        this.message = GameColor.stripCodes(message);
        this.duration = Math.max(1500, Math.min(12000, duration));
    }

    @Override
    public void init(HudManager manager) {
        startTime = getTime();
        int fontSize = SettlerBubblesSettings.fontSize == 0
                ? 16
                : SettlerBubblesSettings.fontSize;
        FontOptions font = SettlerBubblesSettings.fontMode.apply(new FontOptions(fontSize));
        FairType type = new FairType().append(font, message);
        type.applyParsers(ChatMessage.getParsers(font));
        int baseMaxTextWidth = style == BubbleStyle.THOUGHT
                ? MAX_THOUGHT_TEXT_WIDTH
                : MAX_TEXT_WIDTH;
        int maxTextWidth = Math.round(baseMaxTextWidth * (fontSize / 14.0F));
        textDraw = type.getDrawOptions(FairType.TextAlign.LEFT, maxTextWidth, true, true);
        Rectangle bounds = textDraw.getBoundingBox();
        textWidth = Math.max(28, bounds.width);
        textHeight = Math.max(fontSize, bounds.height);
    }

    @Override
    public void addDrawables(List<SortedDrawable> list, GameCamera camera, PlayerMob perspective) {
        long elapsed = getTime() - startTime;
        if (elapsed >= duration || mob.removed()) {
            remove();
            return;
        }
        if (!SettlerBubblesSettings.enabled
                || !SettlerBubblesSettings.isCategoryEnabled(category)
                || perspective == null
                || !mob.isVisible()
                || perspective.getDistance(mob) > SettlerBubblesSettings.maxDistance) {
            return;
        }

        Point mobPos = mob.getDrawPos();
        int bubbleWidth = textWidth + 16;
        int bubbleHeight = textHeight + 12;
        int totalWidth = bubbleWidth + 8;
        int totalHeight = bubbleHeight + 18;
        Rectangle cameraBounds = camera.getBounds();

        int worldX = mobPos.x - totalWidth / 2;
        int minX = cameraBounds.x + 4;
        int maxX = cameraBounds.x + cameraBounds.width - totalWidth - 4;
        worldX = Math.max(minX, Math.min(maxX, worldX));
        int worldY = mobPos.y - 46 - totalHeight;

        Rectangle candidate = new Rectangle(worldX, worldY, totalWidth, totalHeight);
        candidate = resolveOverlap(candidate);
        candidate.y = Math.max(cameraBounds.y + 4, candidate.y);
        lastWorldBounds = candidate;

        int drawX = camera.getDrawX(candidate.x);
        int drawY = camera.getDrawY(candidate.y);
        int anchorX = mobPos.x - candidate.x;
        anchorX = Math.max(18, Math.min(bubbleWidth - 8, anchorX));

        float fade = elapsed <= duration - FADE_TIME
                ? 1.0F
                : Math.max(0.0F, (duration - elapsed) / (float)FADE_TIME);
        final DrawOptions background = FadingBubbleDrawOptions.create(
                style == BubbleStyle.THOUGHT ? ThoughtBubbleTexture.get() : Settings.UI.chatbubble,
                bubbleWidth, bubbleHeight, anchorX, drawX + 4, drawY + 4, fade);
        Color base = style == BubbleStyle.SHOUT
                ? new Color(112, 25, 20)
                : Settings.UI.chatBubbleActiveTextColor;
        final Color textColor = new Color(base.getRed(), base.getGreen(), base.getBlue(),
                Math.max(0, Math.min(255, (int)(255 * fade))));
        final int textX = drawX + 12;
        final int textY = drawY + 10;

        list.add(new SortedDrawable() {
            @Override
            public int getPriority() {
                return 1200;
            }

            @Override
            public void draw(TickManager tickManager) {
                background.draw();
                textDraw.draw(textX, textY, textColor);
            }
        });
    }

    private Rectangle resolveOverlap(Rectangle initial) {
        Rectangle result = new Rectangle(initial);
        List<Rectangle> occupied = new ArrayList<>();
        for (HudDrawElement element : getLevel().hudManager.getElements()) {
            if (element instanceof SpeechBubbleHud && element != this && !element.isRemoved()) {
                SpeechBubbleHud bubble = (SpeechBubbleHud)element;
                if (bubble.createdAt <= createdAt && bubble.lastWorldBounds != null) {
                    occupied.add(bubble.lastWorldBounds);
                }
            }
        }
        boolean moved;
        int attempts = 0;
        do {
            moved = false;
            for (Rectangle other : occupied) {
                if (result.intersects(other)) {
                    result.y = other.y - result.height - 4;
                    moved = true;
                }
            }
        } while (moved && ++attempts < 8);
        return result;
    }

    public boolean isFor(Mob other) {
        return mob == other || mob.getUniqueID() == other.getUniqueID();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public BubbleCategory getCategory() {
        return category;
    }

    public Mob getMob() {
        return mob;
    }
}
