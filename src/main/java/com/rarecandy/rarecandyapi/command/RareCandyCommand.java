package com.rarecandy.rarecandyapi.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.rarecandy.rarecandyapi.api.RareCandyAPIProvider;
import com.rarecandy.rarecandyapi.data.PlayerData;
import com.rarecandy.rarecandyapi.util.ColorUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class RareCandyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rarecandy")
                .then(Commands.literal("stats")
                        .executes(context -> showStats(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player())
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> showStats(context.getSource(), EntityArgument.getPlayer(context, "target")))))
                .then(Commands.literal("raid")
                        .executes(context -> showRaidStatus(context.getSource(), context.getSource().getPlayerOrException())))
                .then(Commands.literal("gym")
                        .then(Commands.argument("gymId", StringArgumentType.word())
                                .executes(context -> showGymInfo(context.getSource(), context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "gymId")))))
                .then(Commands.literal("xp")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("add")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(context -> addXp(context.getSource(), EntityArgument.getPlayer(context, "target"), IntegerArgumentType.getInteger(context, "amount"))))))));
    }

    private static int showStats(CommandSourceStack source, ServerPlayer target) {
        PlayerData data = RareCandyAPIProvider.getPlayerData(target);

        source.sendSystemMessage(ColorUtils.format("<gradient:#00FFAA:#00AAFF>--- " + target.getName().getString() + "'s Stats ---</gradient>", target));
        source.sendSystemMessage(ColorUtils.format("<#00FFAA>Level: <#FFFFFF>" + data.getLevel() + " <#AAAAAA>(XP: " + data.getXp() + ")", target));
        source.sendSystemMessage(ColorUtils.format("<#00FFAA>Total Catches: <#FFFFFF>" + data.getTotalCatches(), target));
        source.sendSystemMessage(ColorUtils.format("<#00FFAA>Shiny Catches: <#FFFFFF>" + data.getShinyCatches(), target));
        source.sendSystemMessage(ColorUtils.format("<#00FFAA>Boss Defeats: <#FFFFFF>" + data.getBossDefeats(), target));
        source.sendSystemMessage(ColorUtils.format("<#00FFAA>NPC Wins: <#FFFFFF>" + data.getNpcWins(), target));
        source.sendSystemMessage(ColorUtils.format("<#00FFAA>Raid Wins: <#FFFFFF>" + data.getRaidWins(), target));

        return 1;
    }

    private static int showRaidStatus(CommandSourceStack source, ServerPlayer player) {
        boolean active = RareCandyAPIProvider.isRaidActive(player);
        if (!active) {
            source.sendSystemMessage(ColorUtils.format("<#FFAA00>No active raid battle at the moment.", player));
            return 1;
        }

        String boss = RareCandyAPIProvider.getRaidBossName(player);
        int hp = RareCandyAPIProvider.getRaidBossCurrentHP(player);
        int maxHp = RareCandyAPIProvider.getRaidBossMaxHP(player);
        int damage = RareCandyAPIProvider.getPlayerRaidDamage(player);
        int rank = RareCandyAPIProvider.getPlayerRaidRank(player);

        source.sendSystemMessage(ColorUtils.format("<#00FFAA>--- Current Raid: <#FFFFFF>" + boss + " <#00FFAA>---", player));
        source.sendSystemMessage(ColorUtils.format("<#00FFAA>Boss HP: <#FFFFFF>" + hp + "/" + maxHp, player));
        source.sendSystemMessage(ColorUtils.format("<#00FFAA>Your Damage: <#FFFFFF>" + damage + " <#AAAAAA>(Rank #" + (rank >= 0 ? rank : "-") + ")", player));

        return 1;
    }

    private static int showGymInfo(CommandSourceStack source, ServerPlayer player, String gymId) {
        String champ = RareCandyAPIProvider.getChampionName(gymId);
        int streak = RareCandyAPIProvider.getChampionStreak(gymId);

        source.sendSystemMessage(ColorUtils.format("<#00FFAA>--- Gym Info: <#FFFFFF>" + gymId + " <#00FFAA>---", player));
        source.sendSystemMessage(ColorUtils.format("<#00FFAA>Reigning Champion: <#FFFFFF>" + champ + " <#AAAAAA>(Streak: " + streak + ")", player));

        RareCandyAPIProvider.hasBeatenGym(player, gymId).thenAccept(beaten -> {
            source.sendSystemMessage(ColorUtils.format("<#00FFAA>Cleared: <#FFFFFF>" + (beaten ? "Yes" : "No"), player));
        });

        return 1;
    }

    private static int addXp(CommandSourceStack source, ServerPlayer target, int amount) {
        RareCandyAPIProvider.addPixelPassXP(target, amount);
        source.sendSystemMessage(ColorUtils.format("<#00FF00>Added " + amount + " XP to " + target.getName().getString() + ".", target));
        target.sendSystemMessage(ColorUtils.format("<#00FF00>You received " + amount + " XP!", target));
        return 1;
    }
}
