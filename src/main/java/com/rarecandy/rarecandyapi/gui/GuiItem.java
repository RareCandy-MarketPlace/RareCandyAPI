package com.rarecandy.rarecandyapi.gui;

import com.rarecandy.rarecandyapi.item.ItemBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

public class GuiItem {

    private final ItemStack itemStack;
    private final Consumer<GuiClickContext> clickHandler;

    public GuiItem(ItemStack itemStack, Consumer<GuiClickContext> clickHandler) {
        this.itemStack = itemStack != null ? itemStack : ItemStack.EMPTY;
        this.clickHandler = clickHandler;
    }

    public static GuiItem of(ItemStack itemStack) {
        return new GuiItem(itemStack, null);
    }

    public static GuiItem of(ItemStack itemStack, Consumer<GuiClickContext> clickHandler) {
        return new GuiItem(itemStack, clickHandler);
    }

    public static GuiItem of(ItemBuilder builder) {
        return new GuiItem(builder.build(), null);
    }

    public static GuiItem of(ItemBuilder builder, Consumer<GuiClickContext> clickHandler) {
        return new GuiItem(builder.build(), clickHandler);
    }

    public static GuiItem filler(ItemLike item) {
        return new GuiItem(ItemBuilder.of(item).name(" ").build(), null);
    }

    public static GuiItem filler(ItemStack stack) {
        return new GuiItem(stack, null);
    }

    public static GuiItem empty() {
        return new GuiItem(ItemStack.EMPTY, null);
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public Consumer<GuiClickContext> getClickHandler() {
        return clickHandler;
    }

    public void onClick(GuiClickContext context) {
        if (clickHandler != null) {
            clickHandler.accept(context);
        }
    }
}

