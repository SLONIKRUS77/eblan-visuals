package ru.eblan.visuals.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import ru.eblan.visuals.EblanVisuals;
import ru.eblan.visuals.config.EblanConfig;
import ru.eblan.visuals.config.JsonConfigStore;

/**
 * Контекстное меню ватермарки без остановки игрового мира.
 */
public final class WatermarkMenuScreen extends Screen {
    private static final int WIDTH = 230;
    private static final int HEIGHT = 220;
    private final EblanConfig config = EblanVisuals.config();

    public WatermarkMenuScreen() {
        super(Text.literal("Eblan Visuals"));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void init() {
        // Окно рисует собственные переключатели, чтобы анимация оставалась цельной.
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int progress = Math.round(WatermarkHud.menuAnimationProgress() * 100);
        int panelWidth = WIDTH;
        int panelHeight = HEIGHT;
        int x = (width - panelWidth) / 2;
        int y = height / 2 - panelHeight / 2 + (100 - progress) / 2;

        context.fill(0, 0, width, height, 0x42000000);
        context.fill(x + 4, y, x + panelWidth - 4, y + panelHeight, 0xF00D1020);
        context.fill(x, y + 4, x + panelWidth, y + panelHeight - 4, 0xF00D1020);
        context.fill(x + 12, y + 12, x + panelWidth - 12, y + 13, 0xFF4DD9FF);
        context.drawText(textRenderer, Text.literal("Eblan Visuals"), x + 16, y + 20,
                0xFFFFFFFF, true);
        context.drawText(textRenderer, Text.literal("ПКМ по ватермарке закрывает меню"),
                x + 16, y + 35, 0xFF8A9AB5, false);

        String[] labels = {
                "IP сервера",
                "Пинг игрока",
                "Ник игрока",
                "Локальное время",
                "Координаты"
        };
        boolean[] values = {
                config.showServer, config.showPing, config.showUsername,
                config.showTime, config.showCoordinates
        };
        for (int index = 0; index < labels.length; index++) {
            int rowY = y + 55 + index * 22;
            context.fill(x + 16, rowY - 2, x + panelWidth - 16, rowY + 16, 0x401E2A42);
            context.drawText(textRenderer, Text.literal(labels[index]), x + 24, rowY + 2,
                    0xFFE4ECFF, false);
            context.drawText(textRenderer, Text.literal(values[index] ? "ON" : "OFF"),
                    x + panelWidth - 48, rowY + 2,
                    values[index] ? 0xFF62E6A8 : 0xFFFF6F91, true);
        }
        context.fill(x + 16, y + 172, x + panelWidth - 16, y + 201, 0xFF243552);
        context.drawText(textRenderer, Text.literal("Настройка DRPC..."), x + 30, y + 182,
                0xFFBDEEFF, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int x = (width - WIDTH) / 2;
        int y = height / 2 - HEIGHT / 2;
        if (mouseX < x || mouseX > x + WIDTH || mouseY < y || mouseY > y + HEIGHT) {
            closeMenu();
            return true;
        }
        int row = (int) ((mouseY - (y + 53)) / 22);
        if (row >= 0 && row < 5) {
            switch (row) {
                case 0 -> config.showServer = !config.showServer;
                case 1 -> config.showPing = !config.showPing;
                case 2 -> config.showUsername = !config.showUsername;
                case 3 -> config.showTime = !config.showTime;
                case 4 -> config.showCoordinates = !config.showCoordinates;
            }
            JsonConfigStore.saveVisualConfig(config);
            return true;
        }
        if (mouseY >= y + 168 && mouseY <= y + 208) {
            MinecraftClient.getInstance().setScreen(new DrpcConfigScreen(this));
            return true;
        }
        return true;
    }

    private void closeMenu() {
        MinecraftClient.getInstance().setScreen(null);
    }
}