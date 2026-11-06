package com.rarecandy.rarecandyapi;

import com.rarecandy.rarecandyapi.listener.CommandListener;
import com.rarecandy.rarecandyapi.listener.PixelmonEventInterceptor;
import com.rarecandy.rarecandyapi.listener.PlayerLifecycleListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod("rarecandyapi")
public class RareCandyMod {

    public static final String MOD_ID = "rarecandyapi";

    public RareCandyMod(IEventBus modEventBus) {
        CommandListener.register();
        PlayerLifecycleListener.register();
        try {
            PixelmonEventInterceptor.register();
        } catch (Throwable ignored) {}
    }
}
