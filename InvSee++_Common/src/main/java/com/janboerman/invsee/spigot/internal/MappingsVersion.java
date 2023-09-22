package com.janboerman.invsee.spigot.internal;

import org.bukkit.Server;
import org.bukkit.UnsafeValues;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MappingsVersion {

    private MappingsVersion() {
    }

    //https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/util/CraftMagicNumbers.java?until=5be2ddcbd57fc4a9e192cc398f9d881e917b0210&untilPath=src%2Fmain%2Fjava%2Forg%2Fbukkit%2Fcraftbukkit%2Futil%2FCraftMagicNumbers.java#162
    public static final String _1_17   = "acd6e6c27e5a0a9440afba70a96c27c9";
    public static final String _1_17_1 = "f0e3dfc7390de285a4693518dd5bd126";
    public static final String _1_18   = "9e9fe6961a80f3e586c25601590b51ec";
    public static final String _1_18_1 = "20b026e774dbf715e40a0b2afe114792";
    public static final String _1_18_2 = "eaeedbff51b16ead3170906872fda334";
    public static final String _1_19   = "7b9de0da1357e5b251eddde9aa762916";
    public static final String _1_19_1 = "4cc0cc97cac491651bff3af8b124a214";
    public static final String _1_19_2 = "69c84c88aeb92ce9fa9525438b93f4fe";
    public static final String _1_19_3 = "1afe2ffe8a9d7fc510442a168b3d4338";
    public static final String _1_19_4 = "3009edc0fff87fa34680686663bd59df";
    public static final String _1_20   = "34f399b4f2033891290b7f0700e9e47b";
    public static final String _1_20_1 = "bcf3dcb22ad42792794079f9443df2c0";
    public static final String _1_20_2 = "3478a65bfd04b15b431fe107b3617dfc";

    /**
     * Get the version of the mappings used by CraftBukkit. Note that this method only works on (forks of) CraftBukkit.
     * @note org.bukkit.craftbukkit.util.CraftMagicNumbers#getMappingsVersion() was only introduced at CraftBukkit 1.14
     * @param server the Server instance
     * @return the mappings version (may be used for equality checking only), or null if running on CraftBukkit 1.13.2 or earlier.
     */
    @SuppressWarnings("deprecation")
    public static String getMappingsVersion(Server server) {
        UnsafeValues craftMagicNumbers = server.getUnsafe();
        try {
            Method method = craftMagicNumbers.getClass().getMethod("getMappingsVersion");
            return (String) method.invoke(craftMagicNumbers, new Object[0]);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassCastException e) {
            return null;
        }
    }

}
