package com.rarecandy.rarecandyapi.listener;

import com.rarecandy.rarecandyapi.command.RareCandyCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class CommandListener {

    private static boolean isRegistered = false;

    public static void register() {
        if (!isRegistered) {
            NeoForge.EVENT_BUS.register(new CommandListener());
            isRegistered = true;
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        RareCandyCommand.register(event.getDispatcher());
    }
}
