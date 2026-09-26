package ru.eblan.visuals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import ru.eblan.visuals.EblanVisuals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Читает и записывает конфигурацию мода в папку config.
 */
public final class JsonConfigStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path VISUAL_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("eblan_visuals.json");
    private static final Path DRPC_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("eblan_visuals_drp.json");

    public static EblanConfig loadVisualConfig() {
        return read(VISUAL_PATH, EblanConfig.class, new EblanConfig());
    }

    public static DrpcConfig loadDrpcConfig() {
        return read(DRPC_PATH, DrpcConfig.class, new DrpcConfig());
    }

    public static void saveVisualConfig(EblanConfig config) {
        write(VISUAL_PATH, config);
    }

    public static void saveDrpcConfig(DrpcConfig config) {
        write(DRPC_PATH, config);
    }

    private static <T> T read(Path path, Class<T> type, T fallback) {
        if (!Files.exists(path)) {
            write(path, fallback);
            return fallback;
        }
        try {
            T value = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), type);
            return value == null ? fallback : value;
        } catch (Exception exception) {
            EblanVisuals.LOGGER.warn("Не удалось прочитать {}: {}", path, exception.getMessage());
            return fallback;
        }
    }

    private static void write(Path path, Object value) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(value), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            EblanVisuals.LOGGER.warn("Не удалось сохранить {}: {}", path, exception.getMessage());
        }
    }

    private JsonConfigStore() {
    }
}