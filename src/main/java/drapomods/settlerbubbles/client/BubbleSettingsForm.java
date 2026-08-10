package drapomods.settlerbubbles.client;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import drapomods.settlerbubbles.BubbleFontMode;
import drapomods.settlerbubbles.BubbleFrequency;
import drapomods.settlerbubbles.BubbleLanguage;
import drapomods.settlerbubbles.SettlerBubblesSettings;
import necesse.engine.Settings;
import necesse.engine.gameLoop.tickManager.TickManager;
import necesse.engine.input.InputEvent;
import necesse.engine.input.InputID;
import necesse.engine.localization.Language;
import necesse.engine.localization.Localization;
import necesse.engine.localization.message.GameMessage;
import necesse.engine.localization.message.LocalMessage;
import necesse.engine.localization.message.StaticMessage;
import necesse.engine.network.client.Client;
import necesse.engine.window.GameWindow;
import necesse.entity.mobs.PlayerMob;
import necesse.gfx.forms.Form;
import necesse.gfx.forms.components.FormCheckBox;
import necesse.gfx.forms.components.FormDropdownSelectionButton;
import necesse.gfx.forms.components.FormInputSize;
import necesse.gfx.forms.components.FormLabel;
import necesse.gfx.forms.components.FormSlider;
import necesse.gfx.forms.components.localComponents.FormLocalCheckBox;
import necesse.gfx.forms.components.localComponents.FormLocalLabel;
import necesse.gfx.forms.components.localComponents.FormLocalTextButton;
import necesse.gfx.gameFont.FontOptions;
import necesse.gfx.ui.ButtonColor;

public class BubbleSettingsForm extends Form {
    private static final int FORM_WIDTH = 660;
    private static final int FORM_HEIGHT = 600;
    private static final int COLUMN_WIDTH = 292;
    private static final int LEFT_X = 24;
    private static final int RIGHT_X = 344;
    private static final int[] FONT_SIZES = {14, 16, 18, 20, 22};
    private static final Set<String> SUPPORTED_LANGUAGES = new HashSet<>(Arrays.asList(
            "en", "pt-BR", "de", "es", "fr", "nl", "pl", "ru", "zh-CN", "ja", "kr"
    ));

    private final Client client;
    private final GameWindow window;
    private final Runnable closeAction;
    private final FormCheckBox enabled;
    private final FormDropdownSelectionButton<BubbleFrequency> frequency;
    private final FormSlider duration;
    private final FormSlider distance;
    private final FormDropdownSelectionButton<BubbleFontMode> fontMode;
    private final FormDropdownSelectionButton<Integer> fontSize;
    private final FormSlider density;
    private final FormDropdownSelectionButton<BubbleLanguage> bubbleLanguage;
    private final FormCheckBox social;
    private final FormCheckBox work;
    private final FormCheckBox needs;
    private final FormCheckBox mood;
    private final FormCheckBox combat;

    public BubbleSettingsForm(Client client, GameWindow window, Runnable closeAction) {
        super("settlerbubblessettings", FORM_WIDTH, FORM_HEIGHT);
        this.client = client;
        this.window = window;
        this.closeAction = closeAction;

        addComponent(new FormLocalLabel(local("settings_title"), new FontOptions(20),
                FormLabel.ALIGN_MID, FORM_WIDTH / 2, 12));

        Language language = Localization.getCurrentLang();
        boolean supported = language != null && SUPPORTED_LANGUAGES.contains(language.stringID);
        String languageName = language == null ? "English" : language.localDisplayName;
        GameMessage languageStatus = local(supported ? "language_available" : "language_fallback");
        addComponent(new FormLocalLabel(new LocalMessage("settlerbubbles", "settings_language",
                "language", languageName, "status", languageStatus), new FontOptions(14),
                FormLabel.ALIGN_MID, FORM_WIDTH / 2, 44, FORM_WIDTH - 48));

        addSectionLabel("section_general", LEFT_X, 78);
        enabled = addComponent(new FormLocalCheckBox(local("enabled_session"),
                LEFT_X, 108, SettlerBubblesSettings.enabled, COLUMN_WIDTH));

        addSmallLabel("frequency", LEFT_X, 143);
        frequency = addComponent(new FormDropdownSelectionButton<BubbleFrequency>(
                LEFT_X, 164, FormInputSize.SIZE_32, ButtonColor.BASE, COLUMN_WIDTH));
        addFrequencyOption(BubbleFrequency.LOW, "frequency_low");
        addFrequencyOption(BubbleFrequency.NORMAL, "frequency_normal");
        addFrequencyOption(BubbleFrequency.HIGH, "frequency_high");
        selectFrequency(SettlerBubblesSettings.frequency);

        duration = addComponent(new FormSlider(translate("duration"), LEFT_X, 216,
                Math.round(SettlerBubblesSettings.durationScale * 100.0F), 50, 200, COLUMN_WIDTH) {
            @Override
            public String getValueText() {
                return getValue() + "%";
            }
        });

        distance = addComponent(new FormSlider(translate("distance"), LEFT_X, 267,
                SettlerBubblesSettings.maxDistance, 200, 1600, COLUMN_WIDTH));
        distance.drawValueInPercent = false;

        addSectionLabel("section_visuals", LEFT_X, 320);
        addSmallLabel("font_type", LEFT_X, 354);
        fontMode = addComponent(new FormDropdownSelectionButton<BubbleFontMode>(
                LEFT_X, 375, FormInputSize.SIZE_32, ButtonColor.BASE, COLUMN_WIDTH));
        addFontModeOption(BubbleFontMode.GAME_DEFAULT, "font_game_default");
        addFontModeOption(BubbleFontMode.PIXEL, "font_pixel");
        addFontModeOption(BubbleFontMode.SMOOTH, "font_smooth");
        selectFontMode(SettlerBubblesSettings.fontMode);

        addSmallLabel("font_size", LEFT_X, 421);
        fontSize = addComponent(new FormDropdownSelectionButton<Integer>(
                LEFT_X, 442, FormInputSize.SIZE_32, ButtonColor.BASE, COLUMN_WIDTH));
        for (int size : FONT_SIZES) {
            fontSize.options.add(size, fontSizeMessage(size));
        }
        setFontSize(SettlerBubblesSettings.fontSize);

        FormLocalTextButton autoButton = addComponent(new FormLocalTextButton(
                local("auto_detect"), LEFT_X, 483, COLUMN_WIDTH,
                FormInputSize.SIZE_32, ButtonColor.BASE));
        autoButton.onClicked(event -> setFontSize(SettlerBubblesSettings.suggestFontSize(window)));

        addSectionLabel("section_categories", RIGHT_X, 78);
        social = addComponent(categoryCheckBox("category_social", 108,
                SettlerBubblesSettings.social));
        work = addComponent(categoryCheckBox("category_work", 138,
                SettlerBubblesSettings.work));
        needs = addComponent(categoryCheckBox("category_needs", 168,
                SettlerBubblesSettings.needs));
        mood = addComponent(categoryCheckBox("category_mood", 198,
                SettlerBubblesSettings.mood));
        combat = addComponent(categoryCheckBox("category_combat", 228,
                SettlerBubblesSettings.combat));

        addSectionLabel("section_density", RIGHT_X, 281);
        density = addComponent(new FormSlider(translate("max_visible"), RIGHT_X, 313,
                SettlerBubblesSettings.maxVisibleBubbles, 3, 10, COLUMN_WIDTH));
        density.drawValueInPercent = false;

        addComponent(new FormLocalLabel(local("density_help"), new FontOptions(13),
                FormLabel.ALIGN_LEFT, RIGHT_X, 365, COLUMN_WIDTH));
        addComponent(new FormLocalLabel(local("settings_help"), new FontOptions(13),
                FormLabel.ALIGN_LEFT, RIGHT_X, 422, COLUMN_WIDTH));

        addSmallLabel("bubble_language", RIGHT_X, 478);
        bubbleLanguage = addComponent(new FormDropdownSelectionButton<BubbleLanguage>(
                RIGHT_X, 499, FormInputSize.SIZE_32, ButtonColor.BASE, COLUMN_WIDTH));
        bubbleLanguage.options.add(BubbleLanguage.GAME_LANGUAGE, local("language_game"));
        for (BubbleLanguage option : BubbleLanguage.values()) {
            if (option == BubbleLanguage.GAME_LANGUAGE) {
                continue;
            }
            Language optionLanguage = option.getLanguage();
            String name = optionLanguage == null
                    ? option.name()
                    : optionLanguage.localDisplayName;
            bubbleLanguage.options.add(option, new StaticMessage(name));
        }
        selectBubbleLanguage(SettlerBubblesSettings.language);

        FormLocalTextButton resetButton = addComponent(new FormLocalTextButton(
                local("reset_defaults"), 24, 552, 202,
                FormInputSize.SIZE_32, ButtonColor.BASE));
        resetButton.onClicked(event -> resetDefaults());

        FormLocalTextButton cancelButton = addComponent(new FormLocalTextButton(
                local("cancel"), 370, 552, 126,
                FormInputSize.SIZE_32, ButtonColor.BASE));
        cancelButton.onClicked(event -> closeAction.run());

        FormLocalTextButton saveButton = addComponent(new FormLocalTextButton(
                local("save"), 510, 552, 126,
                FormInputSize.SIZE_32, ButtonColor.GREEN));
        saveButton.onClicked(event -> saveAndClose());
    }

    @Override
    public void handleInputEvent(InputEvent event, TickManager tickManager, PlayerMob perspective) {
        if (!event.isUsed() && event.state && event.getID() == InputID.KEY_ESCAPE) {
            event.use();
            closeAction.run();
            return;
        }
        super.handleInputEvent(event, tickManager, perspective);
    }

    private void saveAndClose() {
        SettlerBubblesSettings.enabled = enabled.checked;
        SettlerBubblesSettings.frequency = valueOr(frequency.getSelected(), BubbleFrequency.NORMAL);
        SettlerBubblesSettings.durationScale = duration.getValue() / 100.0F;
        SettlerBubblesSettings.maxDistance = distance.getValue();
        SettlerBubblesSettings.fontMode = valueOr(fontMode.getSelected(), BubbleFontMode.GAME_DEFAULT);
        SettlerBubblesSettings.fontSize = valueOr(fontSize.getSelected(),
                SettlerBubblesSettings.suggestFontSize(window));
        SettlerBubblesSettings.maxVisibleBubbles = density.getValue();
        SettlerBubblesSettings.language = valueOr(
                bubbleLanguage.getSelected(), BubbleLanguage.GAME_LANGUAGE);
        SettlerBubblesSettings.social = social.checked;
        SettlerBubblesSettings.work = work.checked;
        SettlerBubblesSettings.needs = needs.checked;
        SettlerBubblesSettings.mood = mood.checked;
        SettlerBubblesSettings.combat = combat.checked;
        Settings.saveClientSettings();
        BubbleClientManager.trim(client.getLevel(), client.getPlayer());
        closeAction.run();
    }

    private void resetDefaults() {
        enabled.checked = true;
        selectFrequency(BubbleFrequency.NORMAL);
        duration.setValue(100);
        distance.setValue(700);
        selectFontMode(BubbleFontMode.GAME_DEFAULT);
        setFontSize(SettlerBubblesSettings.suggestFontSize(window));
        density.setValue(SettlerBubblesSettings.DEFAULT_MAX_VISIBLE_BUBBLES);
        selectBubbleLanguage(BubbleLanguage.GAME_LANGUAGE);
        social.checked = true;
        work.checked = true;
        needs.checked = true;
        mood.checked = true;
        combat.checked = true;
    }

    private FormLocalCheckBox categoryCheckBox(String key, int y, boolean checked) {
        return new FormLocalCheckBox(local(key), RIGHT_X, y, checked, COLUMN_WIDTH);
    }

    private void addSectionLabel(String key, int x, int y) {
        addComponent(new FormLocalLabel(local(key), new FontOptions(17),
                FormLabel.ALIGN_LEFT, x, y, COLUMN_WIDTH));
    }

    private void addSmallLabel(String key, int x, int y) {
        addComponent(new FormLocalLabel(local(key), new FontOptions(14),
                FormLabel.ALIGN_LEFT, x, y, COLUMN_WIDTH));
    }

    private void addFrequencyOption(BubbleFrequency value, String key) {
        frequency.options.add(value, local(key));
    }

    private void selectFrequency(BubbleFrequency value) {
        BubbleFrequency selected = valueOr(value, BubbleFrequency.NORMAL);
        frequency.setSelected(selected, local("frequency_" + selected.name().toLowerCase()));
    }

    private void addFontModeOption(BubbleFontMode value, String key) {
        fontMode.options.add(value, local(key));
    }

    private void selectFontMode(BubbleFontMode value) {
        BubbleFontMode selected = valueOr(value, BubbleFontMode.GAME_DEFAULT);
        String key;
        switch (selected) {
            case PIXEL:
                key = "font_pixel";
                break;
            case SMOOTH:
                key = "font_smooth";
                break;
            case GAME_DEFAULT:
            default:
                key = "font_game_default";
                break;
        }
        fontMode.setSelected(selected, local(key));
    }

    private void setFontSize(int value) {
        int normalized = SettlerBubblesSettings.normalizeFontSize(value);
        if (normalized == 0) {
            normalized = SettlerBubblesSettings.suggestFontSize(window);
        }
        fontSize.setSelected(normalized, fontSizeMessage(normalized));
    }

    private void selectBubbleLanguage(BubbleLanguage value) {
        BubbleLanguage selected = valueOr(value, BubbleLanguage.GAME_LANGUAGE);
        if (selected == BubbleLanguage.GAME_LANGUAGE) {
            bubbleLanguage.setSelected(selected, local("language_game"));
            return;
        }
        Language selectedLanguage = selected.getLanguage();
        String name = selectedLanguage == null
                ? selected.name()
                : selectedLanguage.localDisplayName;
        bubbleLanguage.setSelected(selected, new StaticMessage(name));
    }

    private static GameMessage fontSizeMessage(int size) {
        return new LocalMessage("settlerbubbles", "font_size_value",
                "size", Integer.toString(size));
    }

    private static LocalMessage local(String key) {
        return new LocalMessage("settlerbubbles", key);
    }

    private static String translate(String key) {
        return local(key).translate();
    }

    private static <T> T valueOr(T value, T fallback) {
        return value == null ? fallback : value;
    }
}
