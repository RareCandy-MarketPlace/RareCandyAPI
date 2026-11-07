package com.rarecandy.rarecandyapi.storage;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.PokemonStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.rarecandy.rarecandyapi.pokemon.PokemonGiveResult;
import com.rarecandy.rarecandyapi.pokemon.PokemonHelper;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class StorageHelper {

    public static List<Pokemon> getAll(ServerPlayer player) {
        List<Pokemon> all = new ArrayList<>();
        if (player == null) return all;

        PlayerPartyStorage party = StorageProxy.getPartyNow(player);
        if (party != null) {
            for (Pokemon p : party.getAll()) {
                if (p != null) all.add(p);
            }
        }

        PCStorage pc = StorageProxy.getPCForPlayerNow(player);
        if (pc != null) {
            for (Pokemon p : pc.getAll()) {
                if (p != null) all.add(p);
            }
        }

        return all;
    }

    public static List<Pokemon> find(ServerPlayer player, Predicate<Pokemon> filter) {
        List<Pokemon> matches = new ArrayList<>();
        if (filter == null) return matches;
        for (Pokemon p : getAll(player)) {
            if (filter.test(p)) {
                matches.add(p);
            }
        }
        return matches;
    }

    public static Optional<Pokemon> findFirst(ServerPlayer player, Predicate<Pokemon> filter) {
        if (filter == null) return Optional.empty();
        for (Pokemon p : getAll(player)) {
            if (filter.test(p)) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    public static int countShinies(ServerPlayer player) {
        return find(player, Pokemon::isShiny).size();
    }

    public static int countLegendaries(ServerPlayer player) {
        return find(player, Pokemon::isLegendary).size();
    }

    public static int countUltraBeasts(ServerPlayer player) {
        return find(player, Pokemon::isUltraBeast).size();
    }

    public static boolean delete(Pokemon pokemon) {
        if (pokemon == null || pokemon.getPosition() == null) {
            return false;
        }
        PokemonStorage storage = pokemon.getStorage();
        if (storage != null) {
            storage.set(pokemon.getPosition(), null);
            return true;
        }
        return false;
    }

    public static boolean transfer(ServerPlayer from, ServerPlayer to, Pokemon pokemon) {
        if (from == null || to == null || pokemon == null) {
            return false;
        }

        PokemonGiveResult result = PokemonHelper.givePokemon(to, pokemon);
        if (result == PokemonGiveResult.STORAGE_FULL || result == PokemonGiveResult.ERROR) {
            return false;
        }

        delete(pokemon);
        return true;
    }
}
