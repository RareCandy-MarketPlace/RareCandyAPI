package com.rarecandy.rarecandyapi.battle;

import com.pixelmonmod.pixelmon.api.battles.BattleEndCause;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.api.BattleBuilder;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BattleHelper {

    public static CompletableFuture<BattleController> startPvP(ServerPlayer player1, ServerPlayer player2) {
        return startPvP(player1, player2, true, true);
    }

    public static CompletableFuture<BattleController> startPvP(ServerPlayer player1, ServerPlayer player2, boolean allowSpectators, boolean expEnabled) {
        if (player1 == null || player2 == null || isInBattle(player1) || isInBattle(player2)) {
            return CompletableFuture.completedFuture(null);
        }

        BattleBuilder builder = BattleBuilder.builder()
                .teamOne(new PlayerParticipant(player1))
                .teamTwo(new PlayerParticipant(player2))
                .allowSpectators(allowSpectators);

        if (!expEnabled) {
            builder.disableExp();
        }

        return builder.start();
    }

    public static CompletableFuture<BattleController> startWild(ServerPlayer player, Pokemon pokemon) {
        if (player == null || pokemon == null || isInBattle(player)) {
            return CompletableFuture.completedFuture(null);
        }

        return BattleBuilder.builder()
                .teamOne(new PlayerParticipant(player))
                .teamTwo(new WildPixelmonParticipant(pokemon))
                .start();
    }

    public static CompletableFuture<BattleController> startWild(ServerPlayer player, PixelmonEntity entity) {
        if (player == null || entity == null || isInBattle(player)) {
            return CompletableFuture.completedFuture(null);
        }

        return BattleBuilder.builder()
                .teamOne(new PlayerParticipant(player))
                .teamTwo(new WildPixelmonParticipant(entity))
                .start();
    }

    public static boolean isInBattle(ServerPlayer player) {
        return player != null && BattleRegistry.inBattle(player);
    }

    public static Optional<BattleController> getBattle(ServerPlayer player) {
        if (player == null) return Optional.empty();
        return Optional.ofNullable(BattleRegistry.getBattle(player));
    }

    public static boolean endBattle(ServerPlayer player) {
        Optional<BattleController> opt = getBattle(player);
        if (opt.isPresent()) {
            opt.get().endBattle(BattleEndCause.FORCE);
            return true;
        }
        return false;
    }

    public static boolean forfeit(ServerPlayer player) {
        Optional<BattleController> opt = getBattle(player);
        if (opt.isPresent()) {
            opt.get().forfeitBattleForPlayerAndTheirTeam(BattleEndCause.FORFEIT, player);
            return true;
        }
        return false;
    }

    public static boolean spectate(ServerPlayer spectator, ServerPlayer target) {
        if (spectator == null || target == null || isInBattle(spectator)) {
            return false;
        }
        Optional<BattleController> battle = getBattle(target);
        if (battle.isPresent()) {
            BattleRegistry.registerSpectator(spectator, battle.get());
            return true;
        }
        return false;
    }

    public static void stopSpectating(ServerPlayer spectator) {
        if (spectator != null) {
            BattleRegistry.unregisterSpectator(spectator);
        }
    }
}

