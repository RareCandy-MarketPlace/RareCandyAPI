package com.rarecandy.rarecandyapi.util;

import me.clip.placeholderapi.PlaceholderAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])([A-Fa-f0-9])>");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("</?gradient(:#[A-Fa-f0-9]{6})*>");

    public static Component format(String text, ServerPlayer player) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }

        String parsedText = text;

        if (player != null) {
            try {
                if (Bukkit.getServer() != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                    Player bukkitPlayer = Bukkit.getPlayer(player.getUUID());
                    if (bukkitPlayer != null) {
                        parsedText = PlaceholderAPI.setPlaceholders(bukkitPlayer, text);
                    }
                }
            } catch (Throwable ignored) {}
        }

        parsedText = GRADIENT_PATTERN.matcher(parsedText).replaceAll("");

        Matcher matcher = HEX_PATTERN.matcher(parsedText);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "§x§" + matcher.group(1) + "§" + matcher.group(2) + "§" + matcher.group(3) + "§" + matcher.group(4) + "§" + matcher.group(5) + "§" + matcher.group(6));
        }
        matcher.appendTail(sb);

        return Component.literal(sb.toString().replace("&", "§"));
    }
}
