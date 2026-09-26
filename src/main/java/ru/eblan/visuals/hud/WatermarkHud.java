package ru.eblan.visuals.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import ru.eblan.visuals.EblanVisuals;
import ru.eblan.visuals.config.EblanConfig;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Рендерит интерактивную стеклянную ватермарку и обрабатывает её хитбокс.
 */
public final class WatermarkHud {
    private static final int PANEL_WIDTH = 124;
    private static final int PANEL_HEIGHT = 24;
    private static final int MARGIN = 8;
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static long menuOpenedAt;

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden || client.currentScreen instanceof WatermarkMenuScreen
                || client.currentScreen instanceof DrpcConfigScreen) {
            return;
        }

        EblanConfig config = EblanVisuals.config();
        float scale = Math.max(0.75f, Math.min(1.5f, config.watermarkScale));
        int width = Math.round(PANEL_WIDTH * scale);
        int height = Math.round(PANEL_HEIGHT * scale);
        int[] position = calculatePosition(client.getWindow().getScaledWidth(),
                client.getWindow().getScaledHeight(), width, height, config.watermarkPosition);
        int x = position[0];
        int y = position[1];

        drawGlassPanel(context, x, y, width, height);
        TextRenderer renderer = client.textRenderer;
        String label = "Eblan Visuals";
        int textX = x + 10;
        int textY = y + 7;
        for (int index = 0; index < label.length(); index++) {
            String character = label.substring(index, index + 1);
            int color = gradientColor(index, label.length());
            context.drawText(renderer, Text.literal(character), textX, textY, color, true);
            textX += renderer.getWidth(character);
        }
    }

    public static boolean handleRightClick(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        EblanConfig config = EblanVisuals.config();
        int width = Math.round(PANEL_WIDTH * config.watermarkScale);
        int height = Math.round(PANEL_HEIGHT * config.watermarkScale);
        int[] position = calculatePosition(client.getWindow().getScaledWidth(),
                client.getWindow().getScaledHeight(), width, height, config.watermarkPosition);
        if (mouseX >= position[0] && mouseX <= position[0] + width
                && mouseY >= position[1] && mouseY <= position[1] + height) {
            menuOpenedAt = System.currentTimeMillis();
            client.setScreen(new WatermarkMenuScreen());
            return true;
        }
        return false;
    }

    public static float menuAnimationProgress() {
        return Math.min(1.0f, (System.currentTimeMillis() - menuOpenedAt) / 180.0f);
    }

    public static String currentServer() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isInSingleplayer()) {
            return "Singleplayer";
        }
        if (client.getCurrentServerEntry() == null) {
            return "Отключён";
        }
        return client.getCurrentServerEntry().address;
    }

    public static String currentPing() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) {
            return "—";
        }
        var entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        return entry == null ? "—" : String.valueOf(entry.getLatency());
    }

    public static String currentCoordinates() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return "—";
        }
        return "%d, %d, %d".formatted(client.player.getBlockX(),
                client.player.getBlockY(), client.player.getBlockZ());
    }

    public static String currentTime() {
        return LocalTime.now().format(CLOCK);
    }

    private static void drawGlassPanel(DrawContext context, int x, int y, int width, int height) {
        context.fill(x + 3, y, x + width - 3, y + height, 0xD9121727);
        context.fill(x, y + 3, x + width, y + height - 3, 0xD9121727);
        context.fill(x + 3, y + 1, x + width - 3, y + 2, 0xA84DD9FF);
        context.fill(x + 2, y + height - 2, x + width - 2, y + height - 1, 0x804A1F63);
        context.fill(x + 1, y + 4, x + 2, y + height - 4, 0x705BD7FF);
        context.fill(x + width - 2, y + 4, x + width - 1, y + height - 4, 0x704A1F63);
    }

    private static int gradientColor(int index, int length) {
        float progress = length <= 1 ? 0.0f : index / (float) (length - 1);
        int red = Math.round(80 + 100 * progress);
        int green = Math.round(220 - 80 * progress);
        int blue = 255;
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private static int[] calculatePosition(int screenWidth, int screenHeight,
                                           int width, int height, String position) {
        return switch (position) {
            case "TOP_CENTER" -> new int[]{(screenWidth - width) / 2, MARGIN};
            case "TOP_RIGHT" -> new int[]{screenWidth - width - MARGIN, MARGIN};
            case "BOTTOM_LEFT" -> new int[]{MARGIN, screenHeight - height - MARGIN};
            case "BOTTOM_RIGHT" -> new int[]{screenWidth - width - MARGIN,
                    screenHeight - height - MARGIN};
            default -> new int[]{MARGIN, MARGIN};
        };
    }

    private WatermarkHud() {
    }
}