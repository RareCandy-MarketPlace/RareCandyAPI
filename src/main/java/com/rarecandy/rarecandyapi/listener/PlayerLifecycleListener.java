package com.rarecandy.rarecandyapi.listener;

import com.rarecandy.rarecandyapi.data.PlayerDataManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public class PlayerLifecycleListener {

    private static boolean isRegistered = false;

    public static void register() {
        if (!isRegistered) {
            NeoForge.EVENT_BUS.register(new PlayerLifecycleListener());
            isRegistered = true;
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerDataManager.get(player.getUUID());
        }
    }

    @SubscribeEvent
    public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerDataManager.saveAndUnload(player.getUUID());
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        PlayerDataManager.saveAll();
    }
}
