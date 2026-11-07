package com.rarecandy.rarecandyapi.gui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

public class GuiClickContext {

    private final ServerPlayer player;
    private final int slot;
    private final ClickType clickType;
    private final int button;
    private final ItemStack clickedItem;
    private final RareCandyGui gui;

    public GuiClickContext(ServerPlayer player, int slot, ClickType clickType, int button, ItemStack clickedItem, RareCandyGui gui) {
        this.player = player;
        this.slot = slot;
        this.clickType = clickType;
        this.button = button;
        this.clickedItem = clickedItem;
        this.gui = gui;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public int getSlot() {
        return slot;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public int getButton() {
        return button;
    }

    public ItemStack getClickedItem() {
        return clickedItem;
    }

    public RareCandyGui getGui() {
        return gui;
    }

    public void close() {
        player.closeContainer();
    }
}

