package com.rarecandy.rarecandyapi.pokemon;

import com.pixelmonmod.pixelmon.api.pokemon.Nature;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.ability.AbilityRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.pokemon.stats.EVStore;
import com.pixelmonmod.pixelmon.api.pokemon.stats.IVStore;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShowdownHelper {

    public static String exportToShowdown(Pokemon pokemon) {
        if (pokemon == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(pokemon.getSpecies().getName());
        ItemStack held = pokemon.getHeldItem();
        if (!held.isEmpty()) {
            sb.append(" @ ").append(held.getHoverName().getString());
        }
        sb.append("\n");

        if (pokemon.getAbility() != null) {
            sb.append("Ability: ").append(pokemon.getAbility().getName()).append("\n");
        }

        sb.append("Level: ").append(pokemon.getPokemonLevel()).append("\n");
        if (pokemon.isShiny()) {
            sb.append("Shiny: Yes\n");
        }

        EVStore evs = pokemon.getEVs();
        List<String> evParts = new ArrayList<>();
        if (evs.getStat(BattleStatsType.HP) > 0) evParts.add(evs.getStat(BattleStatsType.HP) + " HP");
        if (evs.getStat(BattleStatsType.ATTACK) > 0) evParts.add(evs.getStat(BattleStatsType.ATTACK) + " Atk");
        if (evs.getStat(BattleStatsType.DEFENSE) > 0) evParts.add(evs.getStat(BattleStatsType.DEFENSE) + " Def");
        if (evs.getStat(BattleStatsType.SPECIAL_ATTACK) > 0) evParts.add(evs.getStat(BattleStatsType.SPECIAL_ATTACK) + " SpA");
        if (evs.getStat(BattleStatsType.SPECIAL_DEFENSE) > 0) evParts.add(evs.getStat(BattleStatsType.SPECIAL_DEFENSE) + " SpD");
        if (evs.getStat(BattleStatsType.SPEED) > 0) evParts.add(evs.getStat(BattleStatsType.SPEED) + " Spe");
        if (!evParts.isEmpty()) {
            sb.append("EVs: ").append(String.join(" / ", evParts)).append("\n");
        }

        if (pokemon.getNature() != null) {
            sb.append(pokemon.getNature().name()).append(" Nature\n");
        }

        IVStore ivs = pokemon.getIVs();
        List<String> ivParts = new ArrayList<>();
        if (ivs.getStat(BattleStatsType.HP) != 31) ivParts.add(ivs.getStat(BattleStatsType.HP) + " HP");
        if (ivs.getStat(BattleStatsType.ATTACK) != 31) ivParts.add(ivs.getStat(BattleStatsType.ATTACK) + " Atk");
        if (ivs.getStat(BattleStatsType.DEFENSE) != 31) ivParts.add(ivs.getStat(BattleStatsType.DEFENSE) + " Def");
        if (ivs.getStat(BattleStatsType.SPECIAL_ATTACK) != 31) ivParts.add(ivs.getStat(BattleStatsType.SPECIAL_ATTACK) + " SpA");
        if (ivs.getStat(BattleStatsType.SPECIAL_DEFENSE) != 31) ivParts.add(ivs.getStat(BattleStatsType.SPECIAL_DEFENSE) + " SpD");
        if (ivs.getStat(BattleStatsType.SPEED) != 31) ivParts.add(ivs.getStat(BattleStatsType.SPEED) + " Spe");
        if (!ivParts.isEmpty()) {
            sb.append("IVs: ").append(String.join(" / ", ivParts)).append("\n");
        }

        for (Attack attack : pokemon.getMoveset()) {
            if (attack != null && attack.getMove() != null) {
                sb.append("- ").append(attack.getMove().getAttackName()).append("\n");
            }
        }

        return sb.toString().trim();
    }

    public static Pokemon importFromShowdown(String showdownText) {
        if (showdownText == null || showdownText.isBlank()) return null;
        String[] lines = showdownText.split("\\r?\\n");
        if (lines.length == 0) return null;

        String header = lines[0].trim();
        String speciesName = header;
        if (header.contains("@")) {
            speciesName = header.substring(0, header.indexOf('@')).trim();
        }
        if (speciesName.contains("(") && speciesName.contains(")")) {
            speciesName = speciesName.substring(speciesName.indexOf('(') + 1, speciesName.indexOf(')')).trim();
        }

        Pokemon pokemon = PokemonHelper.createPokemon(speciesName);
        if (pokemon == null) return null;

        List<String> moves = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("Ability:")) {
                AbilityRegistry.getAbility(line.substring(8).trim()).ifPresent(pokemon::setAbility);
            } else if (line.startsWith("Level:")) {
                try {
                    pokemon.setLevel(Integer.parseInt(line.substring(6).trim()));
                } catch (Throwable ignored) {}
            } else if (line.startsWith("Shiny:")) {
                pokemon.setShiny(line.substring(6).trim().equalsIgnoreCase("Yes"));
            } else if (line.endsWith("Nature")) {
                String natureStr = line.replace("Nature", "").trim();
                Nature nat = Nature.natureFromString(natureStr);
                if (nat != null) pokemon.setNature(nat);
            } else if (line.startsWith("EVs:")) {
                parseStatLine(line.substring(4).trim(), pokemon.getEVs());
            } else if (line.startsWith("IVs:")) {
                parseStatLine(line.substring(4).trim(), pokemon.getIVs());
            } else if (line.startsWith("-")) {
                moves.add(line.substring(1).trim());
            }
        }

        if (!moves.isEmpty()) {
            pokemon.getMoveset().replaceWith(moves);
        }

        return pokemon;
    }

    private static void parseStatLine(String text, EVStore evs) {
        String[] parts = text.split("/");
        for (String part : parts) {
            String trimmed = part.trim();
            String[] tokens = trimmed.split("\\s+");
            if (tokens.length >= 2) {
                try {
                    int val = Integer.parseInt(tokens[0].trim());
                    BattleStatsType type = getStatType(tokens[1].trim());
                    if (type != null) evs.setStat(type, val);
                } catch (Throwable ignored) {}
            }
        }
    }

    private static void parseStatLine(String text, IVStore ivs) {
        String[] parts = text.split("/");
        for (String part : parts) {
            String trimmed = part.trim();
            String[] tokens = trimmed.split("\\s+");
            if (tokens.length >= 2) {
                try {
                    int val = Integer.parseInt(tokens[0].trim());
                    BattleStatsType type = getStatType(tokens[1].trim());
                    if (type != null) ivs.setStat(type, val);
                } catch (Throwable ignored) {}
            }
        }
    }

    private static BattleStatsType getStatType(String abbreviation) {
        return switch (abbreviation.toLowerCase()) {
            case "hp" -> BattleStatsType.HP;
            case "atk" -> BattleStatsType.ATTACK;
            case "def" -> BattleStatsType.DEFENSE;
            case "spa" -> BattleStatsType.SPECIAL_ATTACK;
            case "spd" -> BattleStatsType.SPECIAL_DEFENSE;
            case "spe" -> BattleStatsType.SPEED;
            default -> null;
        };
    }
}
