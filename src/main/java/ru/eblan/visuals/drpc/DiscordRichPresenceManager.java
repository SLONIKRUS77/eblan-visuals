package ru.eblan.visuals.drpc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import ru.eblan.visuals.config.DrpcConfig;
import ru.eblan.visuals.config.JsonConfigStore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Минимальный Discord IPC-клиент для SET_ACTIVITY без сторонних зависимостей.
 */
public final class DiscordRichPresenceManager {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DrpcConfig config;
    private DiscordIpcConnection connection;
    private long lastUpdate;

    public DiscordRichPresenceManager() {
        config = JsonConfigStore.loadDrpcConfig();
    }

    public void start() {
        lastUpdate = 0L;
    }

    public void tick(MinecraftClient client) {
        if (!config.enabled || client.player == null || System.currentTimeMillis() - lastUpdate < 1000) {
            return;
        }
        lastUpdate = System.currentTimeMillis();
        try {
            if (connection == null || !connection.isOpen()) {
                connection = DiscordIpcConnection.connect(config.applicationId);
            }
            if (connection != null) {
                connection.setActivity(createActivity(client));
            }
        } catch (IOException exception) {
            close();
        }
    }

    public DrpcConfig getConfig() {
        return config;
    }

    public void applyConfig(DrpcConfig updated) {
        config.applicationId = updated.applicationId;
        config.enabled = updated.enabled;
        config.presenceMode = updated.presenceMode;
        config.gameName = updated.gameName;
        config.details = updated.details;
        config.state = updated.state;
        config.largeImageKey = updated.largeImageKey;
        config.largeImageText = updated.largeImageText;
        config.smallImageKey = updated.smallImageKey;
        config.smallImageText = updated.smallImageText;
        config.button1Name = updated.button1Name;
        config.button1Url = updated.button1Url;
        config.button2Name = updated.button2Name;
        config.button2Url = updated.button2Url;
        JsonConfigStore.saveDrpcConfig(config);
        close();
        lastUpdate = 0L;
    }

    public void close() {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    private JsonObject createActivity(MinecraftClient client) {
        JsonObject activity = new JsonObject();
        String gameName = expand(config.gameName, client);
        if ("NORMAL_MINECRAFT".equals(config.presenceMode) || gameName.isBlank()) {
            gameName = "Minecraft";
        }
        // Discord показывает имя приложения из Developer Portal, но принимает
        // переданное имя как часть activity. Дублируем его в details, чтобы
        // кастомное название было видно даже при ограничениях клиента Discord.
        activity.addProperty("name", gameName);
        activity.addProperty("type", 0);
        activity.addProperty("details", expand(config.details, client));
        activity.addProperty("state", expand(config.state, client));
        activity.addProperty("instance", true);

        JsonArray buttons = new JsonArray();
        addButton(buttons, config.button1Name, config.button1Url);
        addButton(buttons, config.button2Name, config.button2Url);
        if (!buttons.isEmpty()) {
            activity.add("buttons", buttons);
        }

        JsonObject assets = new JsonObject();
        addAsset(assets, "large_image", "large_text", config.largeImageKey, config.largeImageText);
        addAsset(assets, "small_image", "small_text", config.smallImageKey, config.smallImageText);
        if (!assets.isEmpty()) {
            activity.add("assets", assets);
        }
        return activity;
    }

    private static void addAsset(JsonObject assets, String imageProperty, String textProperty,
                                 String key, String hoverText) {
        if (key == null || key.isBlank()) {
            return;
        }
        assets.addProperty(imageProperty, key);
        if (hoverText != null && !hoverText.isBlank()) {
            assets.addProperty(textProperty, hoverText);
        }
    }

    private static void addButton(JsonArray buttons, String name, String url) {
        if (name == null || name.isBlank() || url == null || url.isBlank()) {
            return;
        }
        JsonObject button = new JsonObject();
        button.addProperty("label", name);
        button.addProperty("url", url);
        buttons.add(button);
    }

    private static String expand(String text, MinecraftClient client) {
        String result = text == null ? "" : text;
        String server = client.isInSingleplayer() ? "Singleplayer" : WatermarkHudValue.server(client);
        String username = client.getSession().getUsername();
        String ping = WatermarkHudValue.ping(client);
        String coords = WatermarkHudValue.coords(client);
        String time = LocalTime.now().format(TIME_FORMAT);
        return result.replace("{server}", server)
                .replace("{username}", username)
                .replace("{ping}", ping)
                .replace("{coords}", coords)
                .replace("{time}", time);
    }

    private static final class WatermarkHudValue {
        private static String server(MinecraftClient client) {
            ServerInfo info = client.getCurrentServerEntry();
            return info == null ? "Отключён" : info.address;
        }

        private static String ping(MinecraftClient client) {
            if (client.player == null || client.getNetworkHandler() == null) {
                return "—";
            }
            var entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            return entry == null ? "—" : String.valueOf(entry.getLatency());
        }

        private static String coords(MinecraftClient client) {
            return client.player == null ? "—" : "%d, %d, %d".formatted(
                    client.player.getBlockX(), client.player.getBlockY(), client.player.getBlockZ());
        }
    }

    /**
     * Реализация Discord IPC поверх Unix domain socket.
     */
    private static final class DiscordIpcConnection {
        private final SocketChannel channel;

        private DiscordIpcConnection(SocketChannel channel) {
            this.channel = channel;
        }

        private static DiscordIpcConnection connect(String applicationId) throws IOException {
            if (applicationId == null || applicationId.isBlank()
                    || applicationId.equals("123456789012345678")) {
                return null;
            }
            for (int index = 0; index < 10; index++) {
                Path path = Path.of("/tmp/discord-ipc-" + index);
                if (!Files.exists(path)) {
                    continue;
                }
                SocketChannel socket = SocketChannel.open(StandardProtocolFamily.UNIX);
                socket.connect(UnixDomainSocketAddress.of(path));
                DiscordIpcConnection result = new DiscordIpcConnection(socket);
                JsonObject handshake = new JsonObject();
                handshake.addProperty("v", 1);
                handshake.addProperty("client_id", applicationId);
                result.send(0, handshake.toString());
                return result;
            }
            return null;
        }

        private boolean isOpen() {
            return channel.isOpen();
        }

        private void setActivity(JsonObject activity) throws IOException {
            JsonObject payload = new JsonObject();
            payload.addProperty("cmd", "SET_ACTIVITY");
            JsonObject args = new JsonObject();
            args.addProperty("pid", ProcessHandle.current().pid());
            args.add("activity", activity);
            payload.add("args", args);
            payload.addProperty("nonce", UUID.randomUUID().toString());
            send(1, payload.toString());
        }

        private void send(int opcode, String payload) throws IOException {
            byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
            ByteBuffer header = ByteBuffer.allocate(8);
            header.putInt(opcode).putInt(bytes.length).flip();
            channel.write(header);
            channel.write(ByteBuffer.wrap(bytes));
        }

        private void close() {
            try {
                channel.close();
            } catch (IOException ignored) {
                // Закрытие IPC-соединения не должно ломать завершение клиента.
            }
        }
    }
}