package com.rarecandy.rarecandyapi.database;

import com.rarecandy.rarecandyapi.RareCandyPlugin;
import com.rarecandy.rarecandyapi.data.PlayerData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {

    private static HikariDataSource dataSource;
    private static final ConcurrentHashMap<UUID, PlayerData> PENDING_SAVES = new ConcurrentHashMap<>();

    public static void init(RareCandyPlugin plugin) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + plugin.getConfig().getString("database.host", "localhost") + ":" +
                plugin.getConfig().getInt("database.port", 3306) + "/" +
                plugin.getConfig().getString("database.database", "rarecandy"));
        config.setUsername(plugin.getConfig().getString("database.username", "root"));
        config.setPassword(plugin.getConfig().getString("database.password", ""));

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.setMaximumPoolSize(10);
        config.setInitializationFailTimeout(0);

        dataSource = new HikariDataSource(config);
        createTable();
        startFlushTask(plugin);
    }

    public static void close() {
        if (!PENDING_SAVES.isEmpty()) {
            Bukkit.getLogger().info("[RareCandyAPI] Attempting final flush of pending saves before shutdown...");
            try (Connection conn = dataSource.getConnection()) {
                for (PlayerData data : PENDING_SAVES.values()) {
                    executeSave(data, conn);
                }
            } catch (SQLException e) {
                Bukkit.getLogger().severe("[RareCandyAPI] Database offline during shutdown! Pending saves lost.");
            }
        }
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    private static void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS rarecandy_stats (" +
                "uuid VARCHAR(36) PRIMARY KEY, level INT, xp INT, totalCatches INT, shinyCatches INT, " +
                "bossDefeats INT, wildDefeats INT, npcWins INT, eggsHatched INT, evolutions INT, " +
                "raidWins INT, raidCaptures INT, tradesCompleted INT, legendariesCaught INT, " +
                "ultraBeastsCaught INT, pokestopsSpun INT, pokeLootsClaimed INT, apricornsPicked INT, " +
                "successfulFishes INT, pokemonLeveledUp INT, megasUsed INT, dynamaxUsed INT, " +
                "fossilsRestored INT, eggsObtained INT, shrinesActivated INT, zMovesUsed INT, " +
                "mythicalsCaught INT, prestigeLevel INT, evolutionStonesUsed INT, prestigeTokens INT, " +
                "lifetimeCatches INT, lifetimeLegendaries INT, lifetimeBossDefeats INT" +
                ");";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            Bukkit.getLogger().severe("[RareCandyAPI] Failed to create database table!");
        }
    }

    private static void startFlushTask(RareCandyPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (PENDING_SAVES.isEmpty()) return;

                try (Connection conn = dataSource.getConnection()) {
                    if (!conn.isValid(3)) return;

                    List<UUID> flushed = new ArrayList<>();
                    for (PlayerData data : PENDING_SAVES.values()) {
                        try {
                            executeSave(data, conn);
                            flushed.add(data.getUuid());
                        } catch (SQLException e) {
                            Bukkit.getLogger().warning("[RareCandyAPI] Failed to flush save for " + data.getUuid());
                        }
                    }

                    flushed.forEach(PENDING_SAVES::remove);

                    if (!flushed.isEmpty()) {
                        plugin.getLogger().info("Successfully flushed " + flushed.size() + " pending saves to the database.");
                    }
                } catch (SQLException ignored) {
                }
            }
        }.runTaskTimerAsynchronously(plugin, 1200L, 1200L);
    }

    public static CompletableFuture<PlayerData> loadPlayerAsync(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            if (PENDING_SAVES.containsKey(uuid)) {
                return PENDING_SAVES.get(uuid);
            }

            PlayerData data = new PlayerData(uuid);
            String sql = "SELECT * FROM rarecandy_stats WHERE uuid = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, uuid.toString());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        data.setLevel(rs.getInt("level"));
                        data.setXp(rs.getInt("xp"));
                        data.setTotalCatches(rs.getInt("totalCatches"));
                        data.setShinyCatches(rs.getInt("shinyCatches"));
                        data.setBossDefeats(rs.getInt("bossDefeats"));
                        data.setWildDefeats(rs.getInt("wildDefeats"));
                        data.setNpcWins(rs.getInt("npcWins"));
                        data.setEggsHatched(rs.getInt("eggsHatched"));
                        data.setEvolutions(rs.getInt("evolutions"));
                        data.setRaidWins(rs.getInt("raidWins"));
                        data.setRaidCaptures(rs.getInt("raidCaptures"));
                        data.setTradesCompleted(rs.getInt("tradesCompleted"));
                        data.setLegendariesCaught(rs.getInt("legendariesCaught"));
                        data.setUltraBeastsCaught(rs.getInt("ultraBeastsCaught"));
                        data.setPokestopsSpun(rs.getInt("pokestopsSpun"));
                        data.setPokeLootsClaimed(rs.getInt("pokeLootsClaimed"));
                        data.setApricornsPicked(rs.getInt("apricornsPicked"));
                        data.setSuccessfulFishes(rs.getInt("successfulFishes"));
                        data.setPokemonLeveledUp(rs.getInt("pokemonLeveledUp"));
                        data.setMegasUsed(rs.getInt("megasUsed"));
                        data.setDynamaxUsed(rs.getInt("dynamaxUsed"));
                        data.setFossilsRestored(rs.getInt("fossilsRestored"));
                        data.setEggsObtained(rs.getInt("eggsObtained"));
                        data.setShrinesActivated(rs.getInt("shrinesActivated"));
                        data.setZMovesUsed(rs.getInt("zMovesUsed"));
                        data.setMythicalsCaught(rs.getInt("mythicalsCaught"));
                        data.setPrestigeLevel(rs.getInt("prestigeLevel"));
                        data.setEvolutionStonesUsed(rs.getInt("evolutionStonesUsed"));
                        data.setPrestigeTokens(rs.getInt("prestigeTokens"));
                        data.setLifetimeCatches(rs.getInt("lifetimeCatches"));
                        data.setLifetimeLegendaries(rs.getInt("lifetimeLegendaries"));
                        data.setLifetimeBossDefeats(rs.getInt("lifetimeBossDefeats"));
                    }
                }
            } catch (SQLException e) {
                Bukkit.getLogger().severe("[RareCandyAPI] Database connection failed for UUID: " + uuid + " - Serving local cache.");
            }
            return data;
        });
    }

    public static CompletableFuture<Void> savePlayerAsync(PlayerData data) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                executeSave(data, conn);
                PENDING_SAVES.remove(data.getUuid());
            } catch (SQLException e) {
                Bukkit.getLogger().warning("[RareCandyAPI] Database offline. Queuing save for " + data.getUuid());
                PENDING_SAVES.put(data.getUuid(), data);
            }
        });
    }

    private static void executeSave(PlayerData data, Connection conn) throws SQLException {
        String sql = "INSERT INTO rarecandy_stats (uuid, level, xp, totalCatches, shinyCatches, bossDefeats, wildDefeats, npcWins, eggsHatched, evolutions, raidWins, raidCaptures, tradesCompleted, legendariesCaught, ultraBeastsCaught, pokestopsSpun, pokeLootsClaimed, apricornsPicked, successfulFishes, pokemonLeveledUp, megasUsed, dynamaxUsed, fossilsRestored, eggsObtained, shrinesActivated, zMovesUsed, mythicalsCaught, prestigeLevel, evolutionStonesUsed, prestigeTokens, lifetimeCatches, lifetimeLegendaries, lifetimeBossDefeats) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "level=VALUES(level), xp=VALUES(xp), totalCatches=VALUES(totalCatches), shinyCatches=VALUES(shinyCatches), " +
                "bossDefeats=VALUES(bossDefeats), wildDefeats=VALUES(wildDefeats), npcWins=VALUES(npcWins), " +
                "eggsHatched=VALUES(eggsHatched), evolutions=VALUES(evolutions), raidWins=VALUES(raidWins), " +
                "raidCaptures=VALUES(raidCaptures), tradesCompleted=VALUES(tradesCompleted), " +
                "legendariesCaught=VALUES(legendariesCaught), ultraBeastsCaught=VALUES(ultraBeastsCaught), " +
                "pokestopsSpun=VALUES(pokestopsSpun), pokeLootsClaimed=VALUES(pokeLootsClaimed), " +
                "apricornsPicked=VALUES(apricornsPicked), successfulFishes=VALUES(successfulFishes), " +
                "pokemonLeveledUp=VALUES(pokemonLeveledUp), megasUsed=VALUES(megasUsed), " +
                "dynamaxUsed=VALUES(dynamaxUsed), fossilsRestored=VALUES(fossilsRestored), " +
                "eggsObtained=VALUES(eggsObtained), shrinesActivated=VALUES(shrinesActivated), " +
                "zMovesUsed=VALUES(zMovesUsed), mythicalsCaught=VALUES(mythicalsCaught), " +
                "prestigeLevel=VALUES(prestigeLevel), evolutionStonesUsed=VALUES(evolutionStonesUsed), " +
                "prestigeTokens=VALUES(prestigeTokens), lifetimeCatches=VALUES(lifetimeCatches), " +
                "lifetimeLegendaries=VALUES(lifetimeLegendaries), lifetimeBossDefeats=VALUES(lifetimeBossDefeats)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, data.getUuid().toString());
            stmt.setInt(2, data.getLevel());
            stmt.setInt(3, data.getXp());
            stmt.setInt(4, data.getTotalCatches());
            stmt.setInt(5, data.getShinyCatches());
            stmt.setInt(6, data.getBossDefeats());
            stmt.setInt(7, data.getWildDefeats());
            stmt.setInt(8, data.getNpcWins());
            stmt.setInt(9, data.getEggsHatched());
            stmt.setInt(10, data.getEvolutions());
            stmt.setInt(11, data.getRaidWins());
            stmt.setInt(12, data.getRaidCaptures());
            stmt.setInt(13, data.getTradesCompleted());
            stmt.setInt(14, data.getLegendariesCaught());
            stmt.setInt(15, data.getUltraBeastsCaught());
            stmt.setInt(16, data.getPokestopsSpun());
            stmt.setInt(17, data.getPokeLootsClaimed());
            stmt.setInt(18, data.getApricornsPicked());
            stmt.setInt(19, data.getSuccessfulFishes());
            stmt.setInt(20, data.getPokemonLeveledUp());
            stmt.setInt(21, data.getMegasUsed());
            stmt.setInt(22, data.getDynamaxUsed());
            stmt.setInt(23, data.getFossilsRestored());
            stmt.setInt(24, data.getEggsObtained());
            stmt.setInt(25, data.getShrinesActivated());
            stmt.setInt(26, data.getZMovesUsed());
            stmt.setInt(27, data.getMythicalsCaught());
            stmt.setInt(28, data.getPrestigeLevel());
            stmt.setInt(29, data.getEvolutionStonesUsed());
            stmt.setInt(30, data.getPrestigeTokens());
            stmt.setInt(31, data.getLifetimeCatches());
            stmt.setInt(32, data.getLifetimeLegendaries());
            stmt.setInt(33, data.getLifetimeBossDefeats());

            stmt.executeUpdate();
        }
    }

    public static List<String> getTopPrestige(int limit) {
        List<String> topPlayers = new ArrayList<>();
        String sql = "SELECT uuid, prestigeLevel FROM rarecandy_stats ORDER BY prestigeLevel DESC LIMIT ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String uuidStr = rs.getString("uuid");
                    int level = rs.getInt("prestigeLevel");
                    String playerName = Bukkit.getOfflinePlayer(UUID.fromString(uuidStr)).getName();
                    if (playerName == null) playerName = "Unknown";
                    topPlayers.add(playerName + ":" + level);
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().warning("[RareCandyAPI] Database offline, cannot fetch top prestige list.");
        }
        return topPlayers;
    }
}