package com.janboerman.invsee.spigot.api;

import java.util.Set;

public interface OfflinePlayerProvider {

    public Set<String> getAll();

    public Set<String> getWithPrefix(String prefix);

    public static class Dummy implements OfflinePlayerProvider {

        public static final Dummy INSTANCE = new Dummy();

        private Dummy() {
        }

        @Override
        public Set<String> getAll() {
            return Set.of();
        }

        @Override
        public Set<String> getWithPrefix(String prefix) {
            return Set.of();
        }
    }

}
