package com.rarecandy.rarecandyapi.pokemon;

import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PokemonHelper {

    public static PokemonGiveResult givePokemon(ServerPlayer player, Pokemon pokemon) {
        if (player == null || pokemon == null) {
            return PokemonGiveResult.ERROR;
        }

        PlayerPartyStorage party = StorageProxy.getPartyNow(player);
        if (party != null && party.add(pokemon)) {
            return PokemonGiveResult.ADDED_TO_PARTY;
        }

        PCStorage pc = StorageProxy.getPCForPlayerNow(player);
        if (pc != null && pc.add(pokemon)) {
            return PokemonGiveResult.STORED_IN_PC;
        }

        return PokemonGiveResult.STORAGE_FULL;
    }

    public static Pokemon createPokemon(String spec) {
        try {
            var attempt = PokemonSpecificationProxy.create(spec);
            if (attempt != null && attempt.wasSuccess() && attempt.get() != null) {
                return attempt.get().create();
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static void setPerfectIVs(Pokemon pokemon) {
        if (pokemon == null) return;
        for (BattleStatsType stat : BattleStatsType.getEVIVStatValues()) {
            pokemon.getIVs().setStat(stat, 31);
        }
    }

    public static void setZeroIVs(Pokemon pokemon) {
        if (pokemon == null) return;
        for (BattleStatsType stat : BattleStatsType.getEVIVStatValues()) {
            pokemon.getIVs().setStat(stat, 0);
        }
    }

    public static double getIVPercentage(Pokemon pokemon) {
        if (pokemon == null) return 0.0;
        return pokemon.getIVs().getPercentage(2);
    }

    public static int getTotalIVs(Pokemon pokemon) {
        if (pokemon == null) return 0;
        int total = 0;
        for (BattleStatsType stat : BattleStatsType.getEVIVStatValues()) {
            total += pokemon.getIVs().getStat(stat);
        }
        return total;
    }

    public static int getTotalEVs(Pokemon pokemon) {
        if (pokemon == null) return 0;
        int total = 0;
        for (BattleStatsType stat : BattleStatsType.getEVIVStatValues()) {
            total += pokemon.getEVs().getStat(stat);
        }
        return total;
    }

    public static boolean isInBattle(ServerPlayer player) {
        return BattleRegistry.getBattle(player) != null;
    }

    public static void healParty(ServerPlayer player) {
        PlayerPartyStorage party = StorageProxy.getPartyNow(player);
        if (party != null) {
            party.heal();
        }
    }

    public static List<Pokemon> getPartyPokemon(ServerPlayer player) {
        List<Pokemon> result = new ArrayList<>();
        PlayerPartyStorage party = StorageProxy.getPartyNow(player);
        if (party != null) {
            for (Pokemon pokemon : party.getAll()) {
                if (pokemon != null) {
                    result.add(pokemon);
                }
            }
        }
        return result;
    }

    public static ItemStack getPokemonPhoto(Pokemon pokemon) {
        if (pokemon == null) return ItemStack.EMPTY;
        try {
            return SpriteItemHelper.getPhoto(pokemon);
        } catch (Throwable ignored) {
            return ItemStack.EMPTY;
        }
    }
}

