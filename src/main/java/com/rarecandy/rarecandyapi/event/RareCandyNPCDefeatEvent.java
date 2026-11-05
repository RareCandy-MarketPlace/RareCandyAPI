package com.rarecandy.rarecandyapi.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

import java.util.UUID;

public class RareCandyNPCDefeatEvent extends Event {
    private final ServerPlayer player;
    private final String npcName;
    private final UUID npcUUID;

    public RareCandyNPCDefeatEvent(ServerPlayer player, String npcName, UUID npcUUID) {
        this.player = player;
        this.npcName = npcName;
        this.npcUUID = npcUUID;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public String getNpcName() {
        return npcName;
    }

    public UUID getNpcUUID() {
        return npcUUID;
    }
}
