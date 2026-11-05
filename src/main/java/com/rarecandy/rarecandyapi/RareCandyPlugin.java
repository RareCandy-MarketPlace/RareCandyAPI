package com.rarecandy.rarecandyapi;

import com.PixelmonRaid.RaidSaveData;
import com.PixelmonRaid.RaidSession;
import com.PixelmonRaid.RaidSpawner;
import com.rarecandy.rarecandyapi.bridge.GymBridge;
import com.rarecandy.rarecandyapi.database.DatabaseManager;
import com.rarecandy.rarecandyapi.listener.PixelmonEventInterceptor;
import com.rarecandy.rarecandyapi.registry.RareCandyExpansion;
import me.clip.placeholderapi.PlaceholderAPI;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class RareCandyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        DatabaseManager.init(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new RareCandyExpansion().register();
        }

        try {
            PixelmonEventInterceptor.register();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        DatabaseManager.close();
    }

    public static String parsePlaceholder(Player player, String placeholder) {
        if (player == null) return "0";
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            return PlaceholderAPI.setPlaceholders(player, placeholder);
        }
        return "0";
    }

    public static String parseRaidPlaceholders(ServerPlayer player, String text) {
        if (player == null || text == null || text.isEmpty()) {
            return text;
        }

        ServerLevel serverLevel = (ServerLevel) player.level();
        RaidSaveData.RaidPlayerStats stats = RaidSaveData.get(serverLevel).getPlayerStats(player.getUUID());
        RaidSession session = RaidSpawner.getSessionSafe(serverLevel);

        if (text.contains("%raidboss_fought%")) text = text.replace("%raidboss_fought%", String.valueOf(stats.raidsJoined));
        if (text.contains("%raidboss_defeated%")) text = text.replace("%raidboss_defeated%", String.valueOf(stats.kills));
        if (text.contains("%raidboss_damage_dealt%")) text = text.replace("%raidboss_damage_dealt%", String.valueOf(stats.totalDamage));
        if (text.contains("%raidboss_damage_taken%")) text = text.replace("%raidboss_damage_taken%", String.valueOf(stats.damageTaken));

        if (text.contains("%raidboss_active%")) {
            boolean active = session != null && session.isBattleActive();
            text = text.replace("%raidboss_active%", String.valueOf(active));
        }
        if (text.contains("%raidboss_current_name%")) {
            String name = session != null && session.getCurrentBossName() != null ? session.getCurrentBossName() : "None";
            text = text.replace("%raidboss_current_name%", name);
        }
        if (text.contains("%raidboss_current_hp%")) {
            int hp = session != null ? session.getTotalRaidHP() : 0;
            text = text.replace("%raidboss_current_hp%", String.valueOf(hp));
        }
        if (text.contains("%raidboss_max_hp%")) {
            int maxHp = session != null ? session.getMaxRaidHP() : 0;
            text = text.replace("%raidboss_max_hp%", String.valueOf(maxHp));
        }
        if (text.contains("%raidboss_player_damage%")) {
            int dmg = session != null ? session.getDamageTracker().getDamage(player.getUUID()) : 0;
            text = text.replace("%raidboss_player_damage%", String.valueOf(dmg));
        }
        if (text.contains("%raidboss_player_rank%")) {
            int rank = session != null ? session.getDamageTracker().getRank(player.getUUID()) : -1;
            text = text.replace("%raidboss_player_rank%", String.valueOf(rank));
        }

        return text;
    }

    public static String parseGymPlaceholders(ServerPlayer player, String text) {
        if (player == null || text == null || text.isEmpty()) {
            return text;
        }

        if (text.contains("%gymsystem_active_gym%")) {
            String gymId = GymBridge.getPlayerActiveGymId(player.getUUID());
            text = text.replace("%gymsystem_active_gym%", gymId != null ? gymId : "None");
        }
        if (text.contains("%gymsystem_in_battle%")) {
            text = text.replace("%gymsystem_in_battle%", String.valueOf(GymBridge.isChallengerInBattle(player.getUUID())));
        }
        if (text.contains("%gymsystem_bt_active_floor%")) {
            text = text.replace("%gymsystem_bt_active_floor%", GymBridge.getActiveBTFloor(player.getUUID()));
        }
        if (text.contains("%gymsystem_player_champion_gym%")) {
            String gym = GymBridge.getGymWherePlayerIsChampion(player.getUUID());
            text = text.replace("%gymsystem_player_champion_gym%", gym != null ? gym : "None");
        }

        return text;
    }

    public static String parseAllPlaceholders(ServerPlayer player, String text) {
        if (player == null || text == null || text.isEmpty()) {
            return text;
        }
        try {
            Player bukkitPlayer = Bukkit.getPlayer(player.getUUID());
            if (bukkitPlayer != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                text = PlaceholderAPI.setPlaceholders(bukkitPlayer, text);
            }
        } catch (Throwable ignored) {}

        text = parseRaidPlaceholders(player, text);
        text = parseGymPlaceholders(player, text);
        return text;
    }

    public static List<String> getTopPrestige(int limit) {
        return DatabaseManager.getTopPrestige(limit);
    }
}
