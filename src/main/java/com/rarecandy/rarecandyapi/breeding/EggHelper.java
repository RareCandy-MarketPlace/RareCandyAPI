package com.rarecandy.rarecandyapi.breeding;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.rarecandy.rarecandyapi.pokemon.PokemonHelper;
import net.minecraft.server.level.ServerPlayer;

public class EggHelper {

    public static boolean isEgg(Pokemon pokemon) {
        return pokemon != null && pokemon.isEgg();
    }

    public static int getEggSteps(Pokemon pokemon) {
        if (pokemon == null || !pokemon.isEgg()) {
            return 0;
        }
        return pokemon.getEggSteps();
    }

    public static void setEggSteps(Pokemon pokemon, int steps) {
        if (pokemon != null && pokemon.isEgg()) {
            pokemon.setEggSteps(Math.max(0, steps));
        }
    }

    public static void reduceEggSteps(Pokemon pokemon, int steps) {
        if (pokemon != null && pokemon.isEgg()) {
            int current = pokemon.getEggSteps();
            int newSteps = Math.max(0, current - Math.max(0, steps));
            pokemon.setEggSteps(newSteps);
            if (newSteps <= 0) {
                pokemon.hatchEgg();
            }
        }
    }

    public static boolean hatch(Pokemon pokemon) {
        if (pokemon != null && pokemon.isEgg()) {
            pokemon.hatchEgg();
            return true;
        }
        return false;
    }

    public static boolean hatch(ServerPlayer player, Pokemon pokemon) {
        return hatch(pokemon);
    }

    public static Pokemon makeEgg(Pokemon pokemon) {
        if (pokemon == null) return null;
        return pokemon.makeEgg();
    }

    public static Pokemon createEgg(String spec) {
        Pokemon pokemon = PokemonHelper.createPokemon(spec);
        if (pokemon == null) return null;
        return pokemon.makeEgg();
    }
}
