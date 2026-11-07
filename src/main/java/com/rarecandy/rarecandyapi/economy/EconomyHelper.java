package com.rarecandy.rarecandyapi.economy;

import com.pixelmonmod.pixelmon.api.economy.BankAccount;
import com.pixelmonmod.pixelmon.api.economy.BankAccountProxy;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import net.minecraft.server.level.ServerPlayer;

import java.math.BigDecimal;
import java.util.Optional;

public class EconomyHelper {

    public static Optional<BankAccount> getAccount(ServerPlayer player) {
        if (player == null) return Optional.empty();
        try {
            BankAccount account = BankAccountProxy.getBankAccount(player.getUUID()).join();
            if (account != null) {
                return Optional.of(account);
            }
        } catch (Throwable t) {
            try {
                Object party = StorageProxy.getPartyNow(player);
                if (party instanceof BankAccount account) {
                    return Optional.of(account);
                }
            } catch (Throwable ignored) {}
        }
        return Optional.empty();
    }

    public static int getBalance(ServerPlayer player) {
        return getAccount(player).map(a -> a.getBalance().intValue()).orElse(0);
    }

    public static boolean hasBalance(ServerPlayer player, int amount) {
        return getBalance(player) >= amount;
    }

    public static boolean addBalance(ServerPlayer player, int amount) {
        Optional<BankAccount> opt = getAccount(player);
        if (opt.isPresent()) {
            opt.get().add(BigDecimal.valueOf(amount));
            return true;
        }
        return false;
    }

    public static boolean takeBalance(ServerPlayer player, int amount) {
        if (!hasBalance(player, amount)) {
            return false;
        }
        Optional<BankAccount> opt = getAccount(player);
        if (opt.isPresent()) {
            opt.get().take(BigDecimal.valueOf(amount));
            return true;
        }
        return false;
    }

    public static boolean setBalance(ServerPlayer player, int amount) {
        Optional<BankAccount> opt = getAccount(player);
        if (opt.isPresent()) {
            opt.get().setBalance(BigDecimal.valueOf(amount));
            return true;
        }
        return false;
    }

    public static String format(int amount) {
        return String.format("$%,d", amount);
    }
}

