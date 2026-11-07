package com.rarecandy.rarecandyapi.cooldown;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CooldownManager<K> {

    private final Map<K, Long> cooldowns = new ConcurrentHashMap<>();

    public static <K> CooldownManager<K> create() {
        return new CooldownManager<>();
    }

    public boolean tryCooldown(K key, Duration duration) {
        return tryCooldown(key, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public boolean tryCooldown(K key, long amount, TimeUnit unit) {
        long now = System.currentTimeMillis();
        long expireAt = cooldowns.getOrDefault(key, 0L);
        if (now < expireAt) {
            return false;
        }
        cooldowns.put(key, now + unit.toMillis(amount));
        return true;
    }

    public boolean hasCooldown(K key) {
        Long expireAt = cooldowns.get(key);
        if (expireAt == null) {
            return false;
        }
        if (System.currentTimeMillis() >= expireAt) {
            cooldowns.remove(key);
            return false;
        }
        return true;
    }

    public long getRemainingMillis(K key) {
        Long expireAt = cooldowns.get(key);
        if (expireAt == null) {
            return 0L;
        }
        long diff = expireAt - System.currentTimeMillis();
        if (diff <= 0) {
            cooldowns.remove(key);
            return 0L;
        }
        return diff;
    }

    public long getRemainingSeconds(K key) {
        return Math.max(0L, (getRemainingMillis(key) + 999) / 1000);
    }

    public String getRemainingFormatted(K key) {
        long seconds = getRemainingSeconds(key);
        if (seconds <= 0) {
            return "0s";
        }
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0) {
            sb.append(minutes).append("m ");
        }
        sb.append(secs).append("s");
        return sb.toString().trim();
    }

    public void setCooldown(K key, Duration duration) {
        setCooldown(key, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void setCooldown(K key, long amount, TimeUnit unit) {
        cooldowns.put(key, System.currentTimeMillis() + unit.toMillis(amount));
    }

    public void resetCooldown(K key) {
        cooldowns.remove(key);
    }

    public void cleanExpired() {
        long now = System.currentTimeMillis();
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= now);
    }

    public void clear() {
        cooldowns.clear();
    }
}

