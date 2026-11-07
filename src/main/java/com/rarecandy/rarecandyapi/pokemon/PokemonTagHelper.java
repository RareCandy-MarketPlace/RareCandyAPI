package com.rarecandy.rarecandyapi.pokemon;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public class PokemonTagHelper {

    private static final String UNTRADEABLE_TAG = "rarecandy_untradeable";
    private static final String UNBREEDABLE_TAG = "rarecandy_unbreedable";
    private static final String CUSTOM_RARITY_TAG = "rarecandy_rarity";

    public static CompoundTag getTag(Pokemon pokemon) {
        if (pokemon == null) return new CompoundTag();
        return pokemon.getPersistentData();
    }

    public static void setString(Pokemon pokemon, String key, String value) {
        if (pokemon != null && key != null && value != null) {
            pokemon.getPersistentData().putString(key, value);
        }
    }

    public static Optional<String> getString(Pokemon pokemon, String key) {
        if (pokemon == null || key == null || !pokemon.getPersistentData().contains(key)) {
            return Optional.empty();
        }
        return Optional.of(pokemon.getPersistentData().getString(key));
    }

    public static void setBoolean(Pokemon pokemon, String key, boolean value) {
        if (pokemon != null && key != null) {
            pokemon.getPersistentData().putBoolean(key, value);
        }
    }

    public static boolean getBoolean(Pokemon pokemon, String key) {
        if (pokemon == null || key == null) return false;
        return pokemon.getPersistentData().getBoolean(key);
    }

    public static void setInt(Pokemon pokemon, String key, int value) {
        if (pokemon != null && key != null) {
            pokemon.getPersistentData().putInt(key, value);
        }
    }

    public static int getInt(Pokemon pokemon, String key) {
        if (pokemon == null || key == null) return 0;
        return pokemon.getPersistentData().getInt(key);
    }

    public static boolean hasKey(Pokemon pokemon, String key) {
        return pokemon != null && key != null && pokemon.getPersistentData().contains(key);
    }

    public static void removeKey(Pokemon pokemon, String key) {
        if (pokemon != null && key != null) {
            pokemon.getPersistentData().remove(key);
        }
    }

    public static void setUntradeable(Pokemon pokemon, boolean untradeable) {
        setBoolean(pokemon, UNTRADEABLE_TAG, untradeable);
    }

    public static boolean isUntradeable(Pokemon pokemon) {
        return getBoolean(pokemon, UNTRADEABLE_TAG);
    }

    public static void setUnbreedable(Pokemon pokemon, boolean unbreedable) {
        setBoolean(pokemon, UNBREEDABLE_TAG, unbreedable);
        if (unbreedable) {
            pokemon.getPersistentData().putBoolean("unbreedable", true);
        }
    }

    public static boolean isUnbreedable(Pokemon pokemon) {
        return getBoolean(pokemon, UNBREEDABLE_TAG) || pokemon.getPersistentData().getBoolean("unbreedable");
    }

    public static void setCustomRarity(Pokemon pokemon, String rarity) {
        setString(pokemon, CUSTOM_RARITY_TAG, rarity);
    }

    public static Optional<String> getCustomRarity(Pokemon pokemon) {
        return getString(pokemon, CUSTOM_RARITY_TAG);
    }
}

