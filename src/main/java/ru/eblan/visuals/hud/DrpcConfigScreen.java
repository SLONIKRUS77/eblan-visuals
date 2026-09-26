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
    private final Screen parent;
    private final DrpcConfig config;
    private TextFieldWidget details;
    private TextFieldWidget state;
    private TextFieldWidget button1Name;
    private TextFieldWidget button1Url;
    private TextFieldWidget button2Name;
    private TextFieldWidget button2Url;

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
        int left = width / 2 - 160;
        int top = height / 2 - 108;
        details = field(left, top + 26, "Заголовок статуса", config.details);
        state = field(left, top + 54, "Описание статуса", config.state);
        button1Name = field(left, top + 82, "Кнопка 1 — название", config.button1Name);
        button1Url = field(left, top + 110, "Кнопка 1 — URL", config.button1Url);
        button2Name = field(left, top + 138, "Кнопка 2 — название", config.button2Name);
        button2Url = field(left, top + 166, "Кнопка 2 — URL", config.button2Url);

        addDrawableChild(ButtonWidget.builder(Text.literal("Применить и Сохранить"),
                button -> apply()).dimensions(left, top + 200, 320, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Отмена"),
                button -> closeScreen()).dimensions(left, top + 224, 156, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Назад"),
                button -> MinecraftClient.getInstance().setScreen(parent))
                .dimensions(left + 164, top + 224, 156, 20).build());
    }

    private TextFieldWidget field(int x, int y, String hint, String value) {
        TextFieldWidget widget = new TextFieldWidget(textRenderer, x, y, 320, 20,
                Text.literal(hint));
        widget.setMaxLength(256);
        widget.setText(value == null ? "" : value);
        addDrawableChild(widget);
        return widget;
    }

    private void apply() {
        config.details = details.getText();
        config.state = state.getText();
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
        int left = width / 2 - 160;
        int top = height / 2 - 108;
        context.fill(left - 16, top - 16, left + 336, top + 258, 0xF00D1020);
        context.fill(left - 12, top - 12, left + 332, top - 11, 0xFF4DD9FF);
        context.drawText(textRenderer, Text.literal("Настройка DRPC"), left, top - 2,
                0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal("Переменные: {server} {username} {ping} {coords}"),
                left, top + 10, 0xFF8A9AB5, false);
        super.render(context, mouseX, mouseY, delta);
    }

    private void closeScreen() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}