package com.rarecandy.rarecandyapi.api;

import com.PixelmonRaid.RaidSaveData;
import com.PixelmonRaid.RaidSession;
import com.PixelmonRaid.RaidSpawner;
import com.gym.gymsystem.api.GymSystemAPI;
import com.gym.gymsystem.config.GymProfile;
import com.gym.gymsystem.data.DatabaseManager.GymLeaderData;
import com.pixelmonmod.pixelmon.api.economy.BankAccount;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.entities.npcs.NPC;
import com.rarecandy.rarecandyapi.RareCandyPlugin;
import com.rarecandy.rarecandyapi.bridge.GymBridge;
import com.rarecandy.rarecandyapi.data.PlayerData;
import com.rarecandy.rarecandyapi.data.PlayerDataManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class RareCandyAPIProvider {

    public static PlayerData getPlayerData(UUID uuid) {
        return PlayerDataManager.get(uuid);
    }

    public static PlayerData getPlayerData(ServerPlayer player) {
        return getPlayerData(player.getUUID());
    }

    public static void addPixelPassXP(ServerPlayer player, int xpAmount) {
        PlayerData data = getPlayerData(player);
        data.addXp(xpAmount);
        PlayerDataManager.save(player.getUUID());
    }

    public static void setQuestProgress(ServerPlayer player, String questId, int progress) {
        PlayerData data = getPlayerData(player);
        data.setQuestProgress(questId, progress);
        PlayerDataManager.save(player.getUUID());
    }

    public static int getBalance(ServerPlayer player) {
        BankAccount account = (BankAccount) StorageProxy.getPartyNow(player);
        return account != null ? account.getBalance().intValue() : 0;
    }

    public static void addBalance(ServerPlayer player, int amount) {
        BankAccount account = (BankAccount) StorageProxy.getPartyNow(player);
        if (account != null) {
            account.add(BigDecimal.valueOf(amount));
        }
    }

    public static void removeBalance(ServerPlayer player, int amount) {
        BankAccount account = (BankAccount) StorageProxy.getPartyNow(player);
        if (account != null) {
            account.take(BigDecimal.valueOf(amount));
        }
    }

    public static void setBalance(ServerPlayer player, int amount) {
        BankAccount account = (BankAccount) StorageProxy.getPartyNow(player);
        if (account != null) {
            account.setBalance(BigDecimal.valueOf(amount));
        }
    }

    public static int countPokemonInParty(ServerPlayer player, String speciesName) {
        PlayerPartyStorage party = StorageProxy.getPartyNow(player);
        int count = 0;
        for (Pokemon pokemon : party.getAll()) {
            if (pokemon != null && pokemon.getSpecies().getName().equalsIgnoreCase(speciesName)) {
                count++;
            }
        }
        return count;
    }

    public static int countPokemonInPC(ServerPlayer player, String speciesName) {
        PCStorage pc = StorageProxy.getPCForPlayerNow(player);
        int count = 0;
        for (Pokemon pokemon : pc.getAll()) {
            if (pokemon != null && pokemon.getSpecies().getName().equalsIgnoreCase(speciesName)) {
                count++;
            }
        }
        return count;
    }

    public static boolean hasPokemonInParty(ServerPlayer player, String speciesName) {
        return countPokemonInParty(player, speciesName) > 0;
    }

    public static boolean hasPokemonInPC(ServerPlayer player, String speciesName) {
        return countPokemonInPC(player, speciesName) > 0;
    }

    public static int countCustomPokemonInParty(ServerPlayer player, Predicate<Pokemon> condition) {
        PlayerPartyStorage party = StorageProxy.getPartyNow(player);
        int count = 0;
        for (Pokemon pokemon : party.getAll()) {
            if (pokemon != null && condition.test(pokemon)) {
                count++;
            }
        }
        return count;
    }

    public static int countCustomPokemonInPC(ServerPlayer player, Predicate<Pokemon> condition) {
        PCStorage pc = StorageProxy.getPCForPlayerNow(player);
        int count = 0;
        for (Pokemon pokemon : pc.getAll()) {
            if (pokemon != null && condition.test(pokemon)) {
                count++;
            }
        }
        return count;
    }

    public static boolean takePokemonFromParty(ServerPlayer player, Predicate<Pokemon> condition, int amountToTake) {
        if (countCustomPokemonInParty(player, condition) < amountToTake) {
            return false;
        }
        PlayerPartyStorage party = StorageProxy.getPartyNow(player);
        int removed = 0;
        for (Pokemon pokemon : party.getAll()) {
            if (pokemon != null && condition.test(pokemon)) {
                party.set(pokemon.getPosition(), null);
                removed++;
                if (removed >= amountToTake) {
                    return true;
                }
            }
        }
        return true;
    }

    public static boolean takePokemonFromPC(ServerPlayer player, Predicate<Pokemon> condition, int amountToTake) {
        if (countCustomPokemonInPC(player, condition) < amountToTake) {
            return false;
        }
        PCStorage pc = StorageProxy.getPCForPlayerNow(player);
        int removed = 0;
        for (Pokemon pokemon : pc.getAll()) {
            if (pokemon != null && condition.test(pokemon)) {
                pc.set(pokemon.getPosition(), null);
                removed++;
                if (removed >= amountToTake) {
                    return true;
                }
            }
        }
        return true;
    }

    public static int countItem(ServerPlayer player, Item item) {
        return player.getInventory().countItem(item);
    }

    public static boolean hasItem(ServerPlayer player, Item item, int amount) {
        return countItem(player, item) >= amount;
    }

    public static boolean takeItem(ServerPlayer player, Item item, int amountToTake) {
        if (!hasItem(player, item, amountToTake)) {
            return false;
        }

        int remaining = amountToTake;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                if (stack.getCount() >= remaining) {
                    stack.shrink(remaining);
                    return true;
                } else {
                    remaining -= stack.getCount();
                    stack.setCount(0);
                }
            }
        }
        return true;
    }

    public static RaidSession getRaidSession(ServerLevel level) {
        return RaidSpawner.getSessionSafe(level);
    }

    public static boolean isRaidActive(ServerLevel level) {
        RaidSession session = getRaidSession(level);
        return session != null && session.isBattleActive();
    }

    public static boolean isRaidActive(ServerPlayer player) {
        return isRaidActive((ServerLevel) player.level());
    }

    public static String getRaidBossName(ServerLevel level) {
        RaidSession session = getRaidSession(level);
        return session != null && session.getCurrentBossName() != null ? session.getCurrentBossName() : "None";
    }

    public static String getRaidBossName(ServerPlayer player) {
        return getRaidBossName((ServerLevel) player.level());
    }

    public static int getRaidBossCurrentHP(ServerLevel level) {
        RaidSession session = getRaidSession(level);
        return session != null ? session.getTotalRaidHP() : 0;
    }

    public static int getRaidBossCurrentHP(ServerPlayer player) {
        return getRaidBossCurrentHP((ServerLevel) player.level());
    }

    public static int getRaidBossMaxHP(ServerLevel level) {
        RaidSession session = getRaidSession(level);
        return session != null ? session.getMaxRaidHP() : 0;
    }

    public static int getRaidBossMaxHP(ServerPlayer player) {
        return getRaidBossMaxHP((ServerLevel) player.level());
    }

    public static int getPlayerRaidDamage(ServerLevel level, UUID playerUuid) {
        RaidSession session = getRaidSession(level);
        return session != null ? session.getDamageTracker().getDamage(playerUuid) : 0;
    }

    public static int getPlayerRaidDamage(ServerPlayer player) {
        return getPlayerRaidDamage((ServerLevel) player.level(), player.getUUID());
    }

    public static int getPlayerRaidRank(ServerLevel level, UUID playerUuid) {
        RaidSession session = getRaidSession(level);
        return session != null ? session.getDamageTracker().getRank(playerUuid) : -1;
    }

    public static int getPlayerRaidRank(ServerPlayer player) {
        return getPlayerRaidRank((ServerLevel) player.level(), player.getUUID());
    }

    public static RaidSaveData.RaidPlayerStats getPlayerRaidStats(ServerLevel level, UUID playerUuid) {
        return RaidSaveData.get(level).getPlayerStats(playerUuid);
    }

    public static RaidSaveData.RaidPlayerStats getPlayerRaidStats(ServerPlayer player) {
        return getPlayerRaidStats((ServerLevel) player.level(), player.getUUID());
    }

    public static GymSystemAPI getGymAPI() {
        return GymBridge.getApi();
    }

    public static CompletableFuture<Boolean> hasBeatenGym(UUID uuid, String gymId) {
        return GymBridge.hasBeatenNode(uuid, gymId);
    }

    public static CompletableFuture<Boolean> hasBeatenGym(ServerPlayer player, String gymId) {
        return hasBeatenGym(player.getUUID(), gymId);
    }

    public static CompletableFuture<Integer> getGymLifetimeWins(UUID uuid, String gymId) {
        return GymBridge.getLifetimeWins(uuid, gymId);
    }

    public static CompletableFuture<Integer> getGymLifetimeWins(ServerPlayer player, String gymId) {
        return getGymLifetimeWins(player.getUUID(), gymId);
    }

    public static CompletableFuture<Integer> getTotalGymWins(UUID uuid) {
        return GymBridge.getTotalWinsByCategory(uuid, "gyms");
    }

    public static CompletableFuture<Integer> getTotalGymWins(ServerPlayer player) {
        return getTotalGymWins(player.getUUID());
    }

    public static CompletableFuture<Integer> getTotalE4Wins(UUID uuid) {
        return GymBridge.getTotalWinsByCategory(uuid, "e4");
    }

    public static CompletableFuture<Integer> getTotalE4Wins(ServerPlayer player) {
        return getTotalE4Wins(player.getUUID());
    }

    public static GymLeaderData getChampionData(String gymId) {
        return GymBridge.getChampionData(gymId);
    }

    public static String getChampionName(String gymId) {
        return GymBridge.getChampionName(gymId);
    }

    public static int getChampionStreak(String gymId) {
        return GymBridge.getChampionStreak(gymId);
    }

    public static UUID getChampionUUID(String gymId) {
        return GymBridge.getChampionUUID(gymId);
    }

    public static String getGymWherePlayerIsChampion(UUID uuid) {
        return GymBridge.getGymWherePlayerIsChampion(uuid);
    }

    public static String getGymWherePlayerIsChampion(ServerPlayer player) {
        return getGymWherePlayerIsChampion(player.getUUID());
    }

    public static boolean isPlayerInGymBattle(UUID uuid) {
        return GymBridge.isChallengerInBattle(uuid);
    }

    public static boolean isPlayerInGymBattle(ServerPlayer player) {
        return isPlayerInGymBattle(player.getUUID());
    }

    public static String getPlayerActiveGymId(UUID uuid) {
        return GymBridge.getPlayerActiveGymId(uuid);
    }

    public static String getPlayerActiveGymId(ServerPlayer player) {
        return getPlayerActiveGymId(player.getUUID());
    }

    public static NPC getPlayerActiveNPC(UUID uuid) {
        return GymBridge.getPlayerActiveNPC(uuid);
    }

    public static NPC getPlayerActiveNPC(ServerPlayer player) {
        return getPlayerActiveNPC(player.getUUID());
    }

    public static CompletableFuture<Long> getPlayerGymCooldownMinutes(UUID uuid, String gymId) {
        return GymBridge.getPlayerCooldownMinutes(uuid, gymId);
    }

    public static CompletableFuture<Long> getPlayerGymCooldownMinutes(ServerPlayer player, String gymId) {
        return getPlayerGymCooldownMinutes(player.getUUID(), gymId);
    }

    public static CompletableFuture<Integer> getPlayerElo(UUID uuid, String format) {
        return GymBridge.fetchPlayerEloValue(uuid, format);
    }

    public static CompletableFuture<Integer> getPlayerElo(ServerPlayer player, String format) {
        return getPlayerElo(player.getUUID(), format);
    }

    public static CompletableFuture<Integer> getPlayerGymWins(UUID uuid, String gymId) {
        return GymBridge.getPlayerWins(uuid, gymId);
    }

    public static CompletableFuture<Integer> getPlayerGymWins(ServerPlayer player, String gymId) {
        return getPlayerGymWins(player.getUUID(), gymId);
    }

    public static CompletableFuture<Integer> getPlayerGymLosses(UUID uuid, String gymId) {
        return GymBridge.getPlayerLosses(uuid, gymId);
    }

    public static CompletableFuture<Integer> getPlayerGymLosses(ServerPlayer player, String gymId) {
        return getPlayerGymLosses(player.getUUID(), gymId);
    }

    public static CompletableFuture<Integer> getPlayerGymAttempts(UUID uuid, String gymId) {
        return GymBridge.getPlayerAttempts(uuid, gymId);
    }

    public static CompletableFuture<Integer> getPlayerGymAttempts(ServerPlayer player, String gymId) {
        return getPlayerGymAttempts(player.getUUID(), gymId);
    }

    public static String getActiveBattleTowerFloor(UUID uuid) {
        return GymBridge.getActiveBTFloor(uuid);
    }

    public static String getActiveBattleTowerFloor(ServerPlayer player) {
        return getActiveBattleTowerFloor(player.getUUID());
    }

    public static int getBattleTowerFloorAttempts(UUID uuid, String floorId) {
        return GymBridge.getFloorAttempts(uuid, floorId);
    }

    public static int getBattleTowerFloorAttempts(ServerPlayer player, String floorId) {
        return getBattleTowerFloorAttempts(player.getUUID(), floorId);
    }

    public static List<Pokemon> getBattleTowerRotationTeam(String floorId) {
        return GymBridge.getBattleTowerRotationTeam(floorId);
    }

    public static Collection<GymProfile> getAllConfiguredGyms() {
        return GymBridge.getAllConfiguredGyms();
    }

    public static GymProfile getGymProfile(String gymId) {
        return GymBridge.getGymProfile(gymId);
    }

    public static String parsePlaceholders(ServerPlayer player, String text) {
        return RareCandyPlugin.parseAllPlaceholders(player, text);
    }
}
