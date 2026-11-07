package com.rarecandy.rarecandyapi.gui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class RareCandyMenu extends ChestMenu {

    private final RareCandyGui gui;
    private final int rows;

    public RareCandyMenu(MenuType<?> menuType, int containerId, Inventory playerInventory, Container container, int rows, RareCandyGui gui) {
        super(menuType, containerId, playerInventory, container, rows);
        this.gui = gui;
        this.rows = rows;
    }

    public static MenuType<?> getMenuTypeForRows(int rows) {
        return switch (rows) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < rows * 9) {
            if (player instanceof ServerPlayer serverPlayer) {
                GuiItem item = gui.getItem(slotId);
                if (item != null) {
                    GuiClickContext ctx = new GuiClickContext(serverPlayer, slotId, clickType, button, item.getItemStack(), gui);
                    item.onClick(ctx);
                }
                serverPlayer.containerMenu.sendAllDataToRemote();
            }
            return;
        }

        if (clickType == ClickType.QUICK_MOVE) {
            return;
        }

        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer serverPlayer) {
            gui.handleClose(serverPlayer);
        }
    }
}

