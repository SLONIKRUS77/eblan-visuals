package ru.eblan.visuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.eblan.visuals.config.EblanConfig;
import ru.eblan.visuals.config.JsonConfigStore;
import ru.eblan.visuals.drpc.DiscordRichPresenceManager;
import ru.eblan.visuals.hud.WatermarkHud;
import ru.eblan.visuals.visual.VisualEffects;

/**
 * Главный клиентский вход мода Eblan Visuals.
 */
public final class EblanVisuals implements ClientModInitializer {
    public static final String MOD_ID = "eblan_visuals";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static EblanConfig config;
    private static DiscordRichPresenceManager discord;

    @Override
    public void onInitializeClient() {
        config = JsonConfigStore.loadVisualConfig();
        discord = new DiscordRichPresenceManager();

        HudRenderCallback.EVENT.register(WatermarkHud::render);
        HudRenderCallback.EVENT.register(VisualEffects::renderHealthBar);
        ClientTickEvents.END_CLIENT_TICK.register(VisualEffects::tick);
        ClientTickEvents.END_CLIENT_TICK.register(client -> discord.tick(client));
        WorldRenderEvents.AFTER_ENTITIES.register(VisualEffects::afterEntities);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            discord.close();
            JsonConfigStore.saveVisualConfig(config);
        });

        discord.start();
        LOGGER.info("Eblan Visuals загружен.");
    }

    public static EblanConfig config() {
        return config;
    }

    public static DiscordRichPresenceManager discord() {
        return discord;
    }

    private EblanVisuals() {
    }
}