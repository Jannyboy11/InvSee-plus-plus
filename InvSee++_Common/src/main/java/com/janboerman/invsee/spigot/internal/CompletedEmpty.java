package com.janboerman.invsee.spigot.internal;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class CompletedEmpty {

    private static final CompletableFuture THE = CompletableFuture.completedFuture(Optional.empty());

    private CompletedEmpty() {}

    public static <T> CompletableFuture<Optional<T>> the() {
        return THE;
    }

}
