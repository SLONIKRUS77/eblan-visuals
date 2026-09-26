package ru.eblan.visuals.visual;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.MathHelper;
import ru.eblan.visuals.EblanVisuals;

/**
 * Частицы и плавный индикатор здоровья, не меняющие игровую механику.
 */
public final class VisualEffects {
    private static float displayedHealth;
    private static float targetHealth;
    private static long lastAttackParticle;

    public static void tick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }
        LivingEntity target = findTarget(client);
        if (target != null) {
            targetHealth = target.getHealth() / Math.max(1.0f, target.getMaxHealth());
            if (displayedHealth == 0.0f) {
                displayedHealth = targetHealth;
            }
            displayedHealth = MathHelper.lerp(0.18f, displayedHealth, targetHealth);
        } else {
            targetHealth = 0.0f;
            displayedHealth = MathHelper.lerp(0.12f, displayedHealth, 0.0f);
        }

        if (EblanVisuals.config().trailParticles && client.player.age % 3 == 0
                && (client.player.isSprinting() || client.player.isFallFlying())) {
            client.world.addParticle(ParticleTypes.END_ROD, client.player.getX(),
                    client.player.getY() + 0.12, client.player.getZ(), 0.0, 0.01, 0.0);
        }
    }

    public static void renderHealthBar(DrawContext context, RenderTickCounter tickCounter) {
        if (!EblanVisuals.config().smoothHealthBar || displayedHealth <= 0.005f) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        int width = 128;
        int x = (client.getWindow().getScaledWidth() - width) / 2;
        int y = client.getWindow().getScaledHeight() / 2 + 26;
        context.fill(x, y, x + width, y + 6, 0xC0202333);
        context.fill(x + 1, y + 1, x + 1 + Math.round((width - 2) * displayedHealth),
                y + 5, healthColor(displayedHealth));
        context.drawText(client.textRenderer, "Цель", x, y - 10, 0xFFE7F1FF, false);
    }

    public static void afterEntities(WorldRenderContext context) {
        // Сам эффект следа создаётся в тике, а callback оставлен для расширения
        // мирового рендера без вмешательства в ванильную сцену.
    }

    public static void spawnHitParticles(MinecraftClient client, Entity entity) {
        if (!EblanVisuals.config().hitParticles || client.world == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastAttackParticle < 35) {
            return;
        }
        lastAttackParticle = now;
        for (int index = 0; index < 8; index++) {
            double offset = (index - 3.5) * 0.025;
            client.world.addParticle(ParticleTypes.CRIT, entity.getX(), entity.getBodyY(0.6f),
                    entity.getZ(), offset, 0.04, -offset);
        }
    }

    private static LivingEntity findTarget(MinecraftClient client) {
        if (client.crosshairTarget == null || client.crosshairTarget.getType()
                != net.minecraft.util.hit.HitResult.Type.ENTITY) {
            return null;
        }
        Entity entity = ((net.minecraft.util.hit.EntityHitResult) client.crosshairTarget).getEntity();
        return entity instanceof LivingEntity living && living != client.player ? living : null;
    }

    private static int healthColor(float value) {
        int red = Math.round(255 * (1.0f - value));
        int green = Math.round(130 + 110 * value);
        return 0xFF000000 | red << 16 | green << 8 | 0x6FFF;
    }

    private VisualEffects() {
    }
}