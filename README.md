# RareCandy API

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.1-brightgreen.svg" alt="Minecraft Version">
  <img src="https://img.shields.io/badge/NeoForge-21.1.248-orange.svg" alt="NeoForge Version">
  <img src="https://img.shields.io/badge/Pixelmon-9.4.1-blue.svg" alt="Pixelmon Version">
  <img src="https://img.shields.io/badge/Java-21-red.svg" alt="Java Version">
  <img src="https://img.shields.io/badge/Version-1.4.0-purple.svg" alt="API Version">
</p>

RareCandy API is the core, scalable API built for Minecraft 1.21.1 NeoForge and Pixelmon. It was designed from the ground up to replace older, clunky APIs (like Envyware) with a clean, modular system that any developer can easily use for complex server mods and plugins.

Works both as a normal NeoForge mod in `/mods` and in hybrid environments (like Arclight) with PlaceholderAPI support.

---

## What It Includes

### 1. Modern ItemBuilder (`com.rarecandy.rarecandyapi.item.ItemBuilder`)
Full native support for Minecraft 1.21.1 `DataComponents`. No deprecated NBT writing:
```java
ItemStack stack = ItemBuilder.of(Items.DIAMOND_SWORD)
    .name("<#FF5555>Rare Candy Blade")
    .lore("&7Ancient relic from battles", "&eDamage: +10")
    .customModelData(1001)
    .unbreakable(true)
    .glint(true)
    .build();
```
- Supports hex colors `<#RRGGBB>` and Minecraft color codes `&`.

### 2. Theft-Proof GUI & Pagination Engine (`com.rarecandy.rarecandyapi.gui`)
Stop players from stealing items or desyncing menus with shift-clicks:
- **`RareCandyGui`**: Fast 1 to 6 row chest menus with slot click handlers, fill borders, row fills, and column fills.
- **`PageableGui<T>`**: Automated pagination framework for any list of objects with previous/next page buttons and page counter placeholders.
- Anti-theft built-in: Overrides `quickMoveStack` and `clicked` so items cannot be extracted by clients.

```java
RareCandyGui gui = RareCandyAPIProvider.createGui(6, "&8Special Rewards");
gui.fillBorder(GuiItem.filler(Items.BLACK_STAINED_GLASS_PANE));
gui.setItem(4, 2, GuiItem.of(rewardItem, ctx -> {
    ctx.getPlayer().sendSystemMessage(Component.literal("You clicked reward!"));
    ctx.close();
}));
gui.open(player);
```

### 3. Battle Automation (`com.rarecandy.rarecandyapi.battle.BattleHelper`)
Start and manage Pixelmon battles in 1 line of code:
- `BattleHelper.startPvP(player1, player2)` - Starts 1v1 battle immediately.
- `BattleHelper.startWild(player, pokemon)` - Starts wild Pokémon battle.
- `BattleHelper.endBattle(player)` - Safely terminates battle with `BattleEndCause.FORCE`.
- `BattleHelper.forfeit(player)` - Forfeits active battle.
- `BattleHelper.spectate(spectator, target)` - Allows players to spectate battles.

### 4. Full Pokédex Integration (`com.rarecandy.rarecandyapi.pokedex.DexHelper`)
Direct bridge to Pixelmon's `PokeDexStorageProxy`:
- `DexHelper.getCaughtCount(player)` - Number of caught Pokémon.
- `DexHelper.getSeenCount(player)` - Number of seen Pokémon.
- `DexHelper.getCaughtPercentage(player)` - Catch completion percentage (e.g. `85.4%`).
- `DexHelper.hasCaught(player, "Charizard")` - Fast boolean species check.
- `DexHelper.registerCaught(player, pokemon)` - Awards caught status directly.

### 5. Pokémon Showdown Converter (`com.rarecandy.rarecandyapi.pokemon.ShowdownHelper`)
Full 2-way conversion between Pixelmon `Pokemon` instances and standard Pokémon Showdown export text:
```java
// Export
String showdownText = ShowdownHelper.exportToShowdown(pokemon);

// Import
Pokemon pokemon = ShowdownHelper.importFromShowdown(showdownText);
```
Supports species, held item, ability, nature, level, shiny status, EVs, IVs, and full movesets.

### 6. Egg & Breeding Engine (`com.rarecandy.rarecandyapi.breeding.EggHelper`)
- `EggHelper.isEgg(pokemon)` - Checks if Pokémon is an unhatched egg.
- `EggHelper.getEggSteps(pokemon)` & `EggHelper.setEggSteps(pokemon, steps)` - Reads/sets steps.
- `EggHelper.reduceEggSteps(pokemon, steps)` - Accelerates egg hatching.
- `EggHelper.hatch(pokemon)` - Instantly hatches egg running Pixelmon animations and sounds.
- `EggHelper.createEgg(spec)` - Creates custom egg from spec string.

### 7. Multi-Storage Aggregator (`com.rarecandy.rarecandyapi.storage.StorageHelper`)
Query party and all PC boxes in one call:
- `StorageHelper.getAll(player)` - Returns all Pokémon across party and PC.
- `StorageHelper.find(player, filter)` - Finds Pokémon matching any custom filter.
- `StorageHelper.countShinies(player)` - Total shinies owned.
- `StorageHelper.countLegendaries(player)` - Total legendaries owned.
- `StorageHelper.transfer(from, to, pokemon)` - Transfers Pokémon between players with overflow protection.

### 8. Cooldowns & Task Scheduler (`CooldownManager`, `Scheduler`)
- **`CooldownManager`**: Thread-safe keyed cooldowns with formatted remaining time strings (`2h 15m 30s`).
- **`Scheduler`**: Unified async thread pool (`Scheduler.async`) and server main-thread tick runner (`Scheduler.syncDelayed`, `Scheduler.syncRepeating`).

### 9. Custom Pokémon Tags (`PokemonTagHelper`)
Store custom persistent NBT metadata on Pokémon:
- `PokemonTagHelper.setUntradeable(pokemon, true)` - Locks Pokémon from being traded.
- `PokemonTagHelper.setUnbreedable(pokemon, true)` - Prevents egg breeding.
- `PokemonTagHelper.setCustomRarity(pokemon, "Mythic")` - Custom rarity tags.

### 10. Functional Events (`EventManager`)
Register listeners with clean lambdas without writing boilerplate listener classes:
```java
EventManager.subscribe(PlayerLoggedInEvent.class, event -> {
    // Handle player login
});

EventManager.subscribePixelmon(PixelmonCaptureEvent.class, event -> {
    // Handle capture
});
```

### 11. Raid Boss & Gym Bridges
Direct integration with Pixelmon Raid and GymSystem mods:
- Read active raid boss name, current HP, max HP, and player damage ranking.
- Query gym leaders, champions, challenge cooldowns, and Battle Tower rotation teams.

---

## In-Game Commands

| Command | Permission | Description |
|---|---|---|
| `/rarecandy stats` | `rarecandy.stats` | Shows your total caught, wins, and PixelPass progress. |
| `/rarecandy raid` | `rarecandy.raid` | Checks current Raid Boss HP and your damage/rank. |
| `/rarecandy gym <gymId>` | `rarecandy.gym` | Checks Gym champion, streak, and clearance status. |

---

## Placeholders (PlaceholderAPI)

- `%rarecandy_xp%` - Your current PixelPass XP.
- `%rarecandy_level%` - Your current PixelPass level.
- `%rarecandy_raid_active%` - True if a raid boss is currently active.
- `%rarecandy_raid_boss%` - Name of the active raid boss.
- `%rarecandy_raid_hp%` - Current HP of the raid boss.
- `%rarecandy_raid_damage%` - Your damage dealt to the current boss.
- `%rarecandy_raid_rank%` - Your damage rank.
- `%rarecandy_gym_champion_<gymId>%` - Current champion of the gym.
- `%rarecandy_gym_streak_<gymId>%` - Current champion defense streak.

---

## Building

```bash
./gradlew build
```
Built jar output is placed in `build/libs/`.
