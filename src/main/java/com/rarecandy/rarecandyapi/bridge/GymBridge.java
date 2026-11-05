package com.rarecandy.rarecandyapi.bridge;

import com.gym.gymsystem.GymSystem;
import com.gym.gymsystem.api.GymSystemAPI;
import com.gym.gymsystem.config.GymProfile;
import com.gym.gymsystem.data.DatabaseManager.GymLeaderData;
import com.gym.gymsystem.logic.BTManager;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.npcs.NPC;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GymBridge {

    public static GymSystemAPI getApi() {
        return GymSystemAPI.getInstance();
    }

    public static CompletableFuture<Boolean> hasBeatenNode(UUID uuid, String gymId) {
        if (GymSystem.databaseManager == null) return CompletableFuture.completedFuture(false);
        return GymSystem.databaseManager.hasBeatenAsync(uuid, gymId);
    }

    public static CompletableFuture<Integer> getLifetimeWins(UUID uuid, String gymId) {
        if (GymSystem.databaseManager == null) return CompletableFuture.completedFuture(0);
        return GymSystem.databaseManager.getPlayerWinsAsync(uuid, gymId);
    }

    public static CompletableFuture<Integer> getTotalWinsByCategory(UUID uuid, String category) {
        if (GymSystem.databaseManager == null) return CompletableFuture.completedFuture(0);
        return GymSystem.databaseManager.getTotalWinsByCategoryAsync(uuid, category);
    }

    public static GymLeaderData getChampionData(String gymId) {
        if (GymSystem.gymManager == null) return null;
        return GymSystem.gymManager.getChampionData(gymId);
    }

    public static String getChampionName(String gymId) {
        GymSystemAPI api = getApi();
        return api != null ? api.getChampionName(gymId) : "Unclaimed";
    }

    public static int getChampionStreak(String gymId) {
        GymSystemAPI api = getApi();
        return api != null ? api.getChampionStreak(gymId) : 0;
    }

    public static UUID getChampionUUID(String gymId) {
        GymSystemAPI api = getApi();
        return api != null ? api.getChampionUUID(gymId) : null;
    }

    public static String getGymWherePlayerIsChampion(UUID uuid) {
        GymSystemAPI api = getApi();
        return api != null ? api.getGymWherePlayerIsChampion(uuid) : null;
    }

    public static boolean isChallengerInBattle(UUID uuid) {
        GymSystemAPI api = getApi();
        return api != null && api.isChallengerInBattle(uuid);
    }

    public static String getPlayerActiveGymId(UUID uuid) {
        GymSystemAPI api = getApi();
        return api != null ? api.getPlayerActiveGymId(uuid) : null;
    }

    public static NPC getPlayerActiveNPC(UUID uuid) {
        if (GymSystem.gymManager == null) return null;
        return GymSystem.gymManager.getActiveNPC(uuid);
    }

    public static CompletableFuture<Long> getPlayerCooldownMinutes(UUID uuid, String gymId) {
        GymSystemAPI api = getApi();
        if (api == null) return CompletableFuture.completedFuture(0L);
        return api.getPlayerCooldownMinutes(uuid, gymId);
    }

    public static CompletableFuture<Integer> fetchPlayerEloValue(UUID uuid, String format) {
        GymSystemAPI api = getApi();
        if (api == null) return CompletableFuture.completedFuture(1000);
        return api.fetchPlayerEloValue(uuid, format);
    }

    public static CompletableFuture<Integer> getPlayerWins(UUID uuid, String gymId) {
        GymSystemAPI api = getApi();
        if (api == null) return CompletableFuture.completedFuture(0);
        return api.getPlayerWins(uuid, gymId);
    }

    public static CompletableFuture<Integer> getPlayerLosses(UUID uuid, String gymId) {
        GymSystemAPI api = getApi();
        if (api == null) return CompletableFuture.completedFuture(0);
        return api.getPlayerLosses(uuid, gymId);
    }

    public static CompletableFuture<Integer> getPlayerAttempts(UUID uuid, String gymId) {
        GymSystemAPI api = getApi();
        if (api == null) return CompletableFuture.completedFuture(0);
        return api.getPlayerAttempts(uuid, gymId);
    }

    public static List<Pokemon> getBattleTowerRotationTeam(String floorId) {
        return BTManager.getActiveTowerRotationTeam(floorId);
    }

    public static Collection<GymProfile> getAllConfiguredGyms() {
        GymSystemAPI api = getApi();
        if (api == null) return Collections.emptyList();
        return api.getAllConfiguredGyms();
    }

    public static GymProfile getGymProfile(String gymId) {
        GymSystemAPI api = getApi();
        if (api == null) return null;
        return api.getGymProfile(gymId);
    }

    public static String getActiveBTFloor(UUID uuid) {
        if (ServerLifecycleHooks.getCurrentServer() == null) return "None";
        ServerPlayer serverPlayer = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
        if (serverPlayer != null && serverPlayer.getPersistentData().contains("ActiveBT")) {
            return serverPlayer.getPersistentData().getString("ActiveBT");
        }
        return "None";
    }

    public static int getFloorAttempts(UUID uuid, String floorId) {
        if (ServerLifecycleHooks.getCurrentServer() == null) return 0;
        ServerPlayer serverPlayer = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
        String attemptKey = "BT_Attempts_" + floorId;
        if (serverPlayer != null && serverPlayer.getPersistentData().contains(attemptKey)) {
            return serverPlayer.getPersistentData().getInt(attemptKey);
        }
        return 0;
    }
}
