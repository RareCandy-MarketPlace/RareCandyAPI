package com.rarecandy.rarecandyapi.pokedex;

import com.pixelmonmod.pixelmon.api.pokedex.PokeDexStorageProxy;
import com.pixelmonmod.pixelmon.api.pokedex.PokedexStorage;
import com.pixelmonmod.pixelmon.api.pokedex.status.PokedexRegistrationStatus;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonBase;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class DexHelper {

    public static Optional<PokedexStorage> getStorage(ServerPlayer player) {
        if (player == null) return Optional.empty();
        try {
            return Optional.ofNullable(PokeDexStorageProxy.getStorageNow(player));
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }

    public static int getCaughtCount(ServerPlayer player) {
        return getStorage(player).map(PokedexStorage::countCaught).orElse(0);
    }

    public static int getSeenCount(ServerPlayer player) {
        return getStorage(player).map(PokedexStorage::countSeen).orElse(0);
    }

    public static double getCaughtPercentage(ServerPlayer player) {
        return getStorage(player).map(s -> Math.round(s.getPercentage(PokedexRegistrationStatus.CAUGHT) * 100.0) / 100.0).orElse(0.0);
    }

    public static double getSeenPercentage(ServerPlayer player) {
        return getStorage(player).map(s -> Math.round(s.getPercentage(PokedexRegistrationStatus.SEEN) * 100.0) / 100.0).orElse(0.0);
    }

    public static boolean hasCaught(ServerPlayer player, Pokemon pokemon) {
        if (pokemon == null) return false;
        return getStorage(player).map(s -> s.hasCaught(pokemon)).orElse(false);
    }

    public static boolean hasCaught(ServerPlayer player, String speciesName) {
        Optional<Species> species = PixelmonSpecies.fromNameOrDex(speciesName);
        if (species.isEmpty()) return false;
        PokemonBase base = new PokemonBase(species.get());
        return getStorage(player).map(s -> s.hasCaught(base)).orElse(false);
    }

    public static boolean hasSeen(ServerPlayer player, Pokemon pokemon) {
        if (pokemon == null) return false;
        return getStorage(player).map(s -> s.hasSeen(pokemon)).orElse(false);
    }

    public static boolean hasSeen(ServerPlayer player, String speciesName) {
        Optional<Species> species = PixelmonSpecies.fromNameOrDex(speciesName);
        if (species.isEmpty()) return false;
        PokemonBase base = new PokemonBase(species.get());
        return getStorage(player).map(s -> s.hasSeen(base)).orElse(false);
    }

    public static boolean registerCaught(ServerPlayer player, Pokemon pokemon) {
        Optional<PokedexStorage> storage = getStorage(player);
        if (storage.isPresent() && pokemon != null) {
            storage.get().setCaught(pokemon, "RareCandyAPI");
            return true;
        }
        return false;
    }

    public static boolean registerSeen(ServerPlayer player, Pokemon pokemon) {
        Optional<PokedexStorage> storage = getStorage(player);
        if (storage.isPresent() && pokemon != null) {
            storage.get().setSeen(pokemon, "RareCandyAPI");
            return true;
        }
        return false;
    }

    public static void clear(ServerPlayer player) {
        getStorage(player).ifPresent(PokedexStorage::clear);
    }
}
