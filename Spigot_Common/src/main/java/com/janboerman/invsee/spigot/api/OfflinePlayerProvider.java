package com.janboerman.invsee.spigot.api;

import com.janboerman.invsee.spigot.internal.MappingsVersion;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.logging.Level;

public interface OfflinePlayerProvider {

    public Set<String> getAll();

    public Set<String> getWithPrefix(String prefix);

    public static OfflinePlayerProvider setup(Plugin plugin) {
        OfflinePlayerProvider offlinePlayerProvider = Dummy.INSTANCE;

        Server server = plugin.getServer();

        try {
            Class<?> implementationClass;
            try {
                Constructor<?> ctor;
                if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_18_R1.CraftServer")) {
                    if (MappingsVersion._1_18_1.equals(MappingsVersion.getMappingsVersion(server))) {
                        implementationClass = Class.forName("com.janboerman.invsee.spigot.impl_1_18_1_R1.KnownPlayersProvider");
                        ctor = implementationClass.getConstructor(Plugin.class);
                        offlinePlayerProvider = OfflinePlayerProvider.class.cast(ctor.newInstance(plugin));
                    } else if (MappingsVersion._1_18.equals(MappingsVersion.getMappingsVersion(server))) {
                        implementationClass = Class.forName("com.janboerman.invsee.spigot.impl_1_18_R1.KnownPlayersProvider");
                        ctor = implementationClass.getConstructor(Plugin.class);
                        offlinePlayerProvider = OfflinePlayerProvider.class.cast(ctor.newInstance(plugin));
                    }
                } else if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_17_R1.CraftServer")) {
                    if (MappingsVersion._1_17_1.equals(MappingsVersion.getMappingsVersion(server))) {
                        implementationClass = Class.forName("com.janboerman.invsee.spigot.impl_1_17_1_R1.KnownPlayersProvider");
                        ctor = implementationClass.getConstructor(Plugin.class);
                        offlinePlayerProvider = OfflinePlayerProvider.class.cast(ctor.newInstance(plugin));
                    } else if (MappingsVersion._1_17.equals(MappingsVersion.getMappingsVersion(server))) {
                        implementationClass = Class.forName("com.janboerman.invsee.spigot.impl_1_17_R1.KnownPlayersProvider");
                        ctor = implementationClass.getConstructor(Plugin.class);
                        offlinePlayerProvider = OfflinePlayerProvider.class.cast(ctor.newInstance(plugin));
                    }
                } else if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_16_R3.CraftServer")) {
                    implementationClass = Class.forName("com.janboerman.invsee.spigot.impl_1_16_R3.KnownPlayersProvider");
                    ctor = implementationClass.getConstructor(Plugin.class);
                    offlinePlayerProvider = OfflinePlayerProvider.class.cast(ctor.newInstance(plugin));
                } else if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_15_R1.CraftServer")) {
                    implementationClass = Class.forName("com.janboerman.invsee.spigot.impl_1_15_R1.KnownPlayersProvider");
                    ctor = implementationClass.getConstructor(Plugin.class);
                    offlinePlayerProvider = OfflinePlayerProvider.class.cast(ctor.newInstance(plugin));
                } else if (server.getClass().getName().equals("org.bukkit.craftbukkit.v1_12_R1.CraftServer")) {
                    implementationClass = Class.forName("com.janboerman.invsee.spigot.impl_1_12_R1.KnownPlayersProvider");
                    ctor = implementationClass.getConstructor(Plugin.class);
                    offlinePlayerProvider = OfflinePlayerProvider.class.cast(ctor.newInstance(plugin));
                }
                //add a bunch of else-if's here in future minecraft versions
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create OfflinePlayerProvider, it needs a public constructor with one parameter: the plugin", e);
            }

        } catch (ClassNotFoundException e) {
            //could not find our own class..
            throw new RuntimeException(e);
        }

        return offlinePlayerProvider;
    }

    static class Dummy implements OfflinePlayerProvider {

        private static final Dummy INSTANCE = new Dummy();

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