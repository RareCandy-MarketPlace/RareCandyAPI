package com.rarecandy.rarecandyapi.scheduler;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class Scheduler {

    private static final ScheduledExecutorService ASYNC_POOL = Executors.newScheduledThreadPool(4, r -> {
        Thread thread = new Thread(r, "RareCandy-Scheduler");
        thread.setDaemon(true);
        return thread;
    });

    private static final List<TickTask> TICK_TASKS = new CopyOnWriteArrayList<>();
    private static boolean registered = false;

    public static void init() {
        if (!registered) {
            NeoForge.EVENT_BUS.register(new SchedulerListener());
            registered = true;
        }
    }

    public static CompletableFuture<Void> async(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, ASYNC_POOL);
    }

    public static <T> CompletableFuture<T> asyncSupply(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, ASYNC_POOL);
    }

    public static CancellableTask asyncDelayed(Runnable runnable, long delay, TimeUnit unit) {
        ScheduledFuture<?> future = ASYNC_POOL.schedule(runnable, delay, unit);
        return new CancellableTask() {
            @Override
            public void cancel() {
                future.cancel(false);
            }

            @Override
            public boolean isCancelled() {
                return future.isCancelled();
            }
        };
    }

    public static CancellableTask asyncRepeating(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        ScheduledFuture<?> future = ASYNC_POOL.scheduleAtFixedRate(runnable, initialDelay, period, unit);
        return new CancellableTask() {
            @Override
            public void cancel() {
                future.cancel(false);
            }

            @Override
            public boolean isCancelled() {
                return future.isCancelled();
            }
        };
    }

    public static void sync(Runnable runnable) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.execute(runnable);
        } else {
            runnable.run();
        }
    }

    public static CancellableTask syncDelayed(Runnable runnable, long delayTicks) {
        init();
        TickTask task = new TickTask(runnable, delayTicks, 0, false);
        TICK_TASKS.add(task);
        return task;
    }

    public static CancellableTask syncRepeating(Runnable runnable, long initialDelayTicks, long periodTicks) {
        init();
        TickTask task = new TickTask(runnable, initialDelayTicks, periodTicks, true);
        TICK_TASKS.add(task);
        return task;
    }

    private static class TickTask implements CancellableTask {
        private final Runnable runnable;
        private long ticksRemaining;
        private final long periodTicks;
        private final boolean repeating;
        private volatile boolean cancelled = false;

        public TickTask(Runnable runnable, long initialDelayTicks, long periodTicks, boolean repeating) {
            this.runnable = runnable;
            this.ticksRemaining = Math.max(0, initialDelayTicks);
            this.periodTicks = Math.max(1, periodTicks);
            this.repeating = repeating;
        }

        public void tick(MinecraftServer server) {
            if (cancelled) return;
            if (ticksRemaining > 0) {
                ticksRemaining--;
                return;
            }
            try {
                runnable.run();
            } catch (Throwable ignored) {}
            if (repeating) {
                ticksRemaining = periodTicks;
            } else {
                cancelled = true;
            }
        }

        @Override
        public void cancel() {
            this.cancelled = true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }
    }

    private static class SchedulerListener {
        @SubscribeEvent
        public void onServerTick(ServerTickEvent.Post event) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;
            TICK_TASKS.removeIf(task -> {
                if (task.isCancelled()) return true;
                task.tick(server);
                return task.isCancelled();
            });
        }
    }
}

