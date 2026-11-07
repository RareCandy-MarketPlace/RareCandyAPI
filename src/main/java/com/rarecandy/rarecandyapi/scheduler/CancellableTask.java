package com.rarecandy.rarecandyapi.scheduler;

public interface CancellableTask {

    void cancel();

    boolean isCancelled();
}

