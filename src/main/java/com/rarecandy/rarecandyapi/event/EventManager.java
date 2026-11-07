package com.rarecandy.rarecandyapi.event;

import com.pixelmonmod.pixelmon.Pixelmon;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Consumer;

public class EventManager {

    public static <T extends Event> void subscribe(Consumer<T> listener) {
        NeoForge.EVENT_BUS.addListener(listener);
    }

    public static <T extends Event> void subscribe(EventPriority priority, Consumer<T> listener) {
        NeoForge.EVENT_BUS.addListener(priority, listener);
    }

    public static <T extends Event> void subscribePixelmon(Consumer<T> listener) {
        Pixelmon.EVENT_BUS.addListener(listener);
    }

    public static <T extends Event> void subscribePixelmon(EventPriority priority, Consumer<T> listener) {
        Pixelmon.EVENT_BUS.addListener(priority, listener);
    }

    public static void post(Event event) {
        NeoForge.EVENT_BUS.post(event);
    }

    public static void postPixelmon(Event event) {
        Pixelmon.EVENT_BUS.post(event);
    }
}

