package com.rarecandy.rarecandyapi.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class PageableGui<T> extends RareCandyGui {

    private final List<T> elements = new ArrayList<>();
    private final List<Integer> contentSlots = new ArrayList<>();
    private BiFunction<T, Integer, GuiItem> itemMapper;
    private int currentPage = 0;

    private int previousPageSlot = -1;
    private int nextPageSlot = -1;
    private int pageInfoSlot = -1;

    private GuiItem previousPageItem;
    private GuiItem nextPageItem;
    private BiFunction<Integer, Integer, GuiItem> pageInfoSupplier;
    private BiFunction<Integer, Integer, Component> titleFunction;

    public PageableGui(int rows, Component title) {
        super(rows, title);
        initDefaultContentSlots();
    }

    public static <T> PageableGui<T> create(int rows, String title) {
        return new PageableGui<>(rows, Component.literal(title));
    }

    public static <T> PageableGui<T> create(int rows, Component title) {
        return new PageableGui<>(rows, title);
    }

    private void initDefaultContentSlots() {
        if (rows <= 2) {
            for (int i = 0; i < (rows * 9) - 2; i++) {
                contentSlots.add(i);
            }
        } else {
            for (int r = 1; r < rows - 1; r++) {
                for (int c = 1; c < 8; c++) {
                    contentSlots.add(r * 9 + c);
                }
            }
        }
    }

    public PageableGui<T> setElements(List<T> elements) {
        this.elements.clear();
        if (elements != null) {
            this.elements.addAll(elements);
        }
        return this;
    }

    public PageableGui<T> setContentSlots(List<Integer> slots) {
        this.contentSlots.clear();
        if (slots != null) {
            this.contentSlots.addAll(slots);
        }
        return this;
    }

    public PageableGui<T> setItemMapper(BiFunction<T, Integer, GuiItem> mapper) {
        this.itemMapper = mapper;
        return this;
    }

    public PageableGui<T> setPreviousButton(int slot, GuiItem item) {
        this.previousPageSlot = slot;
        this.previousPageItem = item;
        return this;
    }

    public PageableGui<T> setNextButton(int slot, GuiItem item) {
        this.nextPageSlot = slot;
        this.nextPageItem = item;
        return this;
    }

    public PageableGui<T> setPageInfo(int slot, BiFunction<Integer, Integer, GuiItem> supplier) {
        this.pageInfoSlot = slot;
        this.pageInfoSupplier = supplier;
        return this;
    }

    public PageableGui<T> setTitleFunction(BiFunction<Integer, Integer, Component> titleFunction) {
        this.titleFunction = titleFunction;
        return this;
    }

    public int getTotalPages() {
        if (contentSlots.isEmpty() || elements.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil((double) elements.size() / contentSlots.size());
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void renderPage(ServerPlayer player) {
        for (int slot : contentSlots) {
            items.remove(slot);
        }

        int pageSize = Math.max(1, contentSlots.size());
        int totalPages = getTotalPages();
        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }

        if (titleFunction != null) {
            this.title = titleFunction.apply(currentPage + 1, totalPages);
        }

        int startIndex = currentPage * pageSize;
        int endIndex = Math.min(startIndex + pageSize, elements.size());

        for (int i = startIndex; i < endIndex; i++) {
            int targetSlot = contentSlots.get(i - startIndex);
            if (itemMapper != null) {
                GuiItem guiItem = itemMapper.apply(elements.get(i), i);
                if (guiItem != null) {
                    items.put(targetSlot, guiItem);
                }
            }
        }

        if (previousPageSlot >= 0 && previousPageItem != null) {
            if (currentPage > 0) {
                items.put(previousPageSlot, GuiItem.of(previousPageItem.getItemStack(), ctx -> {
                    currentPage--;
                    open(player);
                }));
            } else {
                items.remove(previousPageSlot);
            }
        }

        if (nextPageSlot >= 0 && nextPageItem != null) {
            if (currentPage < totalPages - 1) {
                items.put(nextPageSlot, GuiItem.of(nextPageItem.getItemStack(), ctx -> {
                    currentPage++;
                    open(player);
                }));
            } else {
                items.remove(nextPageSlot);
            }
        }

        if (pageInfoSlot >= 0 && pageInfoSupplier != null) {
            GuiItem infoItem = pageInfoSupplier.apply(currentPage + 1, totalPages);
            if (infoItem != null) {
                items.put(pageInfoSlot, infoItem);
            }
        }
    }

    @Override
    public void open(ServerPlayer player) {
        renderPage(player);
        super.open(player);
    }
}

