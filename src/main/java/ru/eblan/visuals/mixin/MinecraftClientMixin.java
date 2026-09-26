package ru.eblan.visuals.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.eblan.visuals.hud.WatermarkHud;

/**
 * Перехватывает стандартное использование ПКМ, чтобы ватермарка получала меню
 * без паузы игрового мира.
 */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    private Mouse mouse;

    @Inject(method = "doItemUse", at = @At("HEAD"), cancellable = true)
    private void eblanVisuals$openWatermarkMenu(CallbackInfo callbackInfo) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        double scale = client.getWindow().getScaleFactor();
        if (client.currentScreen == null
                && WatermarkHud.handleRightClick(mouse.getX() / scale, mouse.getY() / scale)) {
            callbackInfo.cancel();
        }
    }
}