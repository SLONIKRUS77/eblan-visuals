package ru.eblan.visuals.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import ru.eblan.visuals.EblanVisuals;
import ru.eblan.visuals.config.DrpcConfig;
import ru.eblan.visuals.config.JsonConfigStore;

/**
 * Экран редактирования текста Discord Rich Presence.
 */
public final class DrpcConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 398;
    private static final int PANEL_HEIGHT = 438;
    private final Screen parent;
    private final DrpcConfig config;
    private TextFieldWidget gameName;
    private TextFieldWidget details;
    private TextFieldWidget state;
    private TextFieldWidget largeImageKey;
    private TextFieldWidget largeImageText;
    private TextFieldWidget smallImageKey;
    private TextFieldWidget smallImageText;
    private TextFieldWidget button1Name;
    private TextFieldWidget button1Url;
    private TextFieldWidget button2Name;
    private TextFieldWidget button2Url;
    private ButtonWidget modeButton;

    public DrpcConfigScreen(Screen parent) {
        super(Text.literal("Настройка DRPC"));
        this.parent = parent;
        this.config = EblanVisuals.discord().getConfig();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        modeButton = addDrawableChild(ButtonWidget.builder(modeText(),
                button -> {
                    config.presenceMode = isCustomMode()
                            ? "NORMAL_MINECRAFT" : "CUSTOM_GAME";
                    button.setMessage(modeText());
                }).dimensions(left, top + 20, PANEL_WIDTH, 20).build());

        gameName = field(left, top, 0, "Название игры", config.gameName);
        details = field(left, top, 1, "Заголовок статуса", config.details);
        state = field(left, top, 2, "Описание статуса", config.state);
        largeImageKey = field(left, top, 3, "Large Image — ключ иконки", config.largeImageKey);
        largeImageText = field(left, top, 4, "Large Image — подсказка", config.largeImageText);
        smallImageKey = field(left, top, 5, "Small Image — ключ иконки", config.smallImageKey);
        smallImageText = field(left, top, 6, "Small Image — подсказка", config.smallImageText);
        button1Name = field(left, top, 7, "Кнопка 1 — название", config.button1Name);
        button1Url = field(left, top, 8, "Кнопка 1 — URL", config.button1Url);
        button2Name = field(left, top, 9, "Кнопка 2 — название", config.button2Name);
        button2Url = field(left, top, 10, "Кнопка 2 — URL", config.button2Url);

        int buttonsY = top + 43 + 11 * 29;
        addDrawableChild(ButtonWidget.builder(Text.literal("Применить и Сохранить"),
                button -> apply()).dimensions(left, buttonsY, PANEL_WIDTH, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Отмена"),
                button -> closeScreen()).dimensions(left, buttonsY + 25, 194, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Назад"),
                button -> MinecraftClient.getInstance().setScreen(parent))
                .dimensions(left + 204, buttonsY + 25, 194, 20).build());
    }

    private TextFieldWidget field(int x, int top, int index, String label, String value) {
        TextFieldWidget widget = new TextFieldWidget(textRenderer, x, top + 43 + index * 29,
                PANEL_WIDTH, 20, Text.literal(label));
        widget.setMaxLength(256);
        widget.setText(value == null ? "" : value);
        addDrawableChild(widget);
        return widget;
    }

    private void apply() {
        config.gameName = gameName.getText();
        config.details = details.getText();
        config.state = state.getText();
        config.largeImageKey = largeImageKey.getText();
        config.largeImageText = largeImageText.getText();
        config.smallImageKey = smallImageKey.getText();
        config.smallImageText = smallImageText.getText();
        config.button1Name = button1Name.getText();
        config.button1Url = button1Url.getText();
        config.button2Name = button2Name.getText();
        config.button2Url = button2Url.getText();
        JsonConfigStore.saveDrpcConfig(config);
        EblanVisuals.discord().applyConfig(config);
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xB0000000);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        context.fill(left - 16, top - 16, left + PANEL_WIDTH + 16,
                top + PANEL_HEIGHT + 16, 0xF00D1020);
        context.fill(left - 12, top - 12, left + PANEL_WIDTH + 12, top - 11, 0xFF4DD9FF);
        context.drawText(textRenderer, Text.literal("Настройка DRPC"), left, top - 2,
                0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal(
                        "Переменные: {server} {username} {ping} {coords} {time}"),
                left, top + 8, 0xFF8A9AB5, false);
        String[] labels = {
                "Название игры",
                "Заголовок статуса",
                "Описание статуса",
                "Large Image — ключ иконки",
                "Large Image — подсказка",
                "Small Image — ключ иконки",
                "Small Image — подсказка",
                "Кнопка 1 — название",
                "Кнопка 1 — URL",
                "Кнопка 2 — название",
                "Кнопка 2 — URL"
        };
        for (int index = 0; index < labels.length; index++) {
            context.drawText(textRenderer, Text.literal(labels[index]), left,
                    top + 32 + index * 29, 0xFFB8C7E5, false);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private boolean isCustomMode() {
        return "CUSTOM_GAME".equals(config.presenceMode);
    }

    private Text modeText() {
        return Text.literal(isCustomMode()
                ? "Режим: Кастомный игровой статус"
                : "Режим: Обычный Minecraft");
    }

    private void closeScreen() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}