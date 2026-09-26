package ru.eblan.visuals.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.eblan.visuals.visual.VisualEffects;

/**
 * Добавляет частицы в момент атаки, не меняя урон и порядок ванильной атаки.
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Inject(method = "attackEntity", at = @At("TAIL"))
    private void eblanVisuals$hitParticles(MinecraftClient client, Entity target, CallbackInfo callbackInfo) {
        VisualEffects.spawnHitParticles(client, target);
    }
}