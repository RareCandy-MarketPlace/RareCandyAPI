package com.rarecandy.rarecandyapi.item;

import com.rarecandy.rarecandyapi.util.ColorUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack stack;
    private final List<Component> lore = new ArrayList<>();

    private ItemBuilder(ItemStack stack) {
        this.stack = stack.copy();
        ItemLore existingLore = this.stack.get(DataComponents.LORE);
        if (existingLore != null) {
            this.lore.addAll(existingLore.lines());
        }
    }

    public static ItemBuilder of(ItemLike item) {
        return new ItemBuilder(new ItemStack(item));
    }

    public static ItemBuilder of(ItemLike item, int count) {
        return new ItemBuilder(new ItemStack(item, count));
    }

    public static ItemBuilder of(ItemStack stack) {
        return new ItemBuilder(stack);
    }

    public ItemBuilder amount(int count) {
        this.stack.setCount(Math.max(1, count));
        return this;
    }

    public ItemBuilder name(String name) {
        this.stack.set(DataComponents.CUSTOM_NAME, ColorUtils.format(name));
        return this;
    }

    public ItemBuilder name(Component component) {
        this.stack.set(DataComponents.CUSTOM_NAME, component);
        return this;
    }

    public ItemBuilder lore(List<String> lines) {
        this.lore.clear();
        for (String line : lines) {
            this.lore.add(ColorUtils.format(line));
        }
        return this;
    }

    public ItemBuilder lore(String... lines) {
        return lore(Arrays.asList(lines));
    }

    public ItemBuilder addLore(String... lines) {
        for (String line : lines) {
            this.lore.add(ColorUtils.format(line));
        }
        return this;
    }

    public ItemBuilder addLore(Component... components) {
        this.lore.addAll(Arrays.asList(components));
        return this;
    }

    public ItemBuilder customModelData(int customModelData) {
        this.stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(customModelData));
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        this.stack.set(DataComponents.UNBREAKABLE, new Unbreakable(unbreakable));
        return this;
    }

    public ItemBuilder glint(boolean hasGlint) {
        this.stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, hasGlint);
        return this;
    }

    public ItemStack build() {
        if (!lore.isEmpty()) {
            stack.set(DataComponents.LORE, new ItemLore(lore));
        }
        return stack.copy();
    }
}

