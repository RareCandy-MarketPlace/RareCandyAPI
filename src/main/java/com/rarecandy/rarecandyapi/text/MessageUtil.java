package com.rarecandy.rarecandyapi.text;

import com.rarecandy.rarecandyapi.util.ColorUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class MessageUtil {

    public static void send(ServerPlayer player, String message) {
        if (player != null && message != null) {
            player.sendSystemMessage(ColorUtils.format(message, player));
        }
    }

    public static void send(ServerPlayer player, Component component) {
        if (player != null && component != null) {
            player.sendSystemMessage(component);
        }
    }

    public static void sendActionBar(ServerPlayer player, String message) {
        if (player != null && message != null) {
            player.sendSystemMessage(ColorUtils.format(message, player), true);
        }
    }

    public static void sendActionBar(ServerPlayer player, Component component) {
        if (player != null && component != null) {
            player.sendSystemMessage(component, true);
        }
    }

    public static void sendTitle(ServerPlayer player, String title, String subtitle) {
        sendTitle(player, title, subtitle, 10, 50, 10);
    }

    public static void sendTitle(ServerPlayer player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) return;
        player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
        if (title != null) {
            player.connection.send(new ClientboundSetTitleTextPacket(ColorUtils.format(title, player)));
        }
        if (subtitle != null) {
            player.connection.send(new ClientboundSetSubtitleTextPacket(ColorUtils.format(subtitle, player)));
        }
    }

    public static void broadcast(String message) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || message == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(ColorUtils.format(message, player));
        }
    }

    public static void broadcastActionBar(String message) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || message == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(ColorUtils.format(message, player), true);
        }
    }

    public static void playSound(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        if (player == null || sound == null) return;
        player.playNotifySound(sound, SoundSource.PLAYERS, volume, pitch);
    }
}

