package com.sugarplumstudios.coquette.block;

import com.sugarplumstudios.coquette.config.CoquetteConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Mirrors pay compliments; the vanity also pampers you. */
public final class Pampering {
    /** Game time until which each player's makeup is "still perfect". */
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();
    private static final int COMPLIMENTS = 8;

    private Pampering() {
    }

    public static void compliment(ServerLevel level, Player player, Vec3 at) {
        int index = level.random.nextInt(COMPLIMENTS);
        player.sendOverlayMessage(Component.translatable("message.coquettefurniture.compliment." + index)
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        level.playSound(null, at.x, at.y, at.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F,
                1.1F + level.random.nextFloat() * 0.3F);
        sparkle(level, at, 6);
    }

    public static void pamper(ServerLevel level, Player player, Vec3 at) {
        if (!CoquetteConfig.COMMON.vanityEffect.get()) {
            compliment(level, player, at);
            return;
        }
        long now = level.getGameTime();
        if (now < COOLDOWNS.getOrDefault(player.getUUID(), Long.MIN_VALUE)) {
            player.sendOverlayMessage(Component.translatable("message.coquettefurniture.vanity_cooldown")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            sparkle(level, at, 3);
            return;
        }
        COOLDOWNS.put(player.getUUID(), now + CoquetteConfig.COMMON.vanityCooldown.get() * 20L);
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
        player.sendOverlayMessage(Component.translatable("message.coquettefurniture.pampered")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        level.playSound(null, at.x, at.y, at.z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, 1.4F);
        sparkle(level, at, 12);
    }

    public static void sparkle(ServerLevel level, Vec3 at, int count) {
        level.sendParticles(ParticleTypes.HEART, at.x, at.y, at.z, Math.max(1, count / 3), 0.3D, 0.2D, 0.3D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y, at.z, count, 0.35D, 0.3D, 0.35D, 0.01D);
    }
}
