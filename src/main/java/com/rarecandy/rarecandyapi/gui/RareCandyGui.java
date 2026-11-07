package com.rarecandy.rarecandyapi.gui;

import com.rarecandy.rarecandyapi.util.ColorUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RareCandyGui {

    protected final int rows;
    protected Component title;
    protected final Map<Integer, GuiItem> items = new HashMap<>();
    protected Consumer<ServerPlayer> closeHandler;

    public RareCandyGui(int rows, Component title) {
        this.rows = Math.max(1, Math.min(6, rows));
        this.title = title != null ? title : Component.literal("Menu");
    }

    public static RareCandyGui of(int rows, String title) {
        return new RareCandyGui(rows, ColorUtils.format(title));
    }

    public static RareCandyGui of(int rows, Component title) {
        return new RareCandyGui(rows, title);
    }

    public int getRows() {
        return rows;
    }

    public int getSize() {
        return rows * 9;
    }

    public Component getTitle() {
        return title;
    }

    public RareCandyGui setTitle(Component title) {
        this.title = title;
        return this;
    }

    public RareCandyGui setTitle(String title) {
        this.title = ColorUtils.format(title);
        return this;
    }

    public RareCandyGui setItem(int slot, GuiItem item) {
        if (slot >= 0 && slot < getSize()) {
            if (item != null) {
                items.put(slot, item);
            } else {
                items.remove(slot);
            }
        }
        return this;
    }

    public RareCandyGui setItem(int col, int row, GuiItem item) {
        return setItem(row * 9 + col, item);
    }

    public GuiItem getItem(int slot) {
        return items.get(slot);
    }

    public RareCandyGui fill(GuiItem item) {
        for (int i = 0; i < getSize(); i++) {
            if (!items.containsKey(i)) {
                items.put(i, item);
            }
        }
        return this;
    }

    public RareCandyGui fillAll(GuiItem item) {
        for (int i = 0; i < getSize(); i++) {
            items.put(i, item);
        }
        return this;
    }

    public RareCandyGui fillRow(int row, GuiItem item) {
        if (row >= 0 && row < rows) {
            for (int col = 0; col < 9; col++) {
                items.put(row * 9 + col, item);
            }
        }
        return this;
    }

    public RareCandyGui fillColumn(int col, GuiItem item) {
        if (col >= 0 && col < 9) {
            for (int row = 0; row < rows; row++) {
                items.put(row * 9 + col, item);
            }
        }
        return this;
    }

    public RareCandyGui fillBorder(GuiItem item) {
        fillRow(0, item);
        fillRow(rows - 1, item);
        fillColumn(0, item);
        fillColumn(8, item);
        return this;
    }

    public RareCandyGui onClose(Consumer<ServerPlayer> handler) {
        this.closeHandler = handler;
        return this;
    }

    public void handleClose(ServerPlayer player) {
        if (closeHandler != null) {
            closeHandler.accept(player);
        }
    }

    public void open(ServerPlayer player) {
        SimpleContainer container = new SimpleContainer(getSize());
        for (Map.Entry<Integer, GuiItem> entry : items.entrySet()) {
            if (entry.getKey() >= 0 && entry.getKey() < getSize() && entry.getValue() != null) {
                container.setItem(entry.getKey(), entry.getValue().getItemStack());
            }
        }

        player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, p) -> new RareCandyMenu(
                        RareCandyMenu.getMenuTypeForRows(rows),
                        containerId,
                        playerInventory,
                        container,
                        rows,
                        this
                ),
                this.title
        ));
    }

    public void clear() {
        items.clear();
    }
}

