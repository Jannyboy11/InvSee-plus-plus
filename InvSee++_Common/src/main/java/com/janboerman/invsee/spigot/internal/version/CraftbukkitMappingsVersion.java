package com.janboerman.invsee.spigot.internal.version;

import org.bukkit.Server;
import org.bukkit.UnsafeValues;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CraftbukkitMappingsVersion {

    private CraftbukkitMappingsVersion() {
    }

    //https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/util/CraftMagicNumbers.java?until=5be2ddcbd57fc4a9e192cc398f9d881e917b0210&untilPath=src%2Fmain%2Fjava%2Forg%2Fbukkit%2Fcraftbukkit%2Futil%2FCraftMagicNumbers.java#162
    static final String _1_17   = "acd6e6c27e5a0a9440afba70a96c27c9";
    static final String _1_17_1 = "f0e3dfc7390de285a4693518dd5bd126";
    static final String _1_18   = "9e9fe6961a80f3e586c25601590b51ec";
    static final String _1_18_1 = "20b026e774dbf715e40a0b2afe114792";
    static final String _1_18_2 = "eaeedbff51b16ead3170906872fda334";
    static final String _1_19   = "7b9de0da1357e5b251eddde9aa762916";
    static final String _1_19_1 = "4cc0cc97cac491651bff3af8b124a214";
    static final String _1_19_2 = "69c84c88aeb92ce9fa9525438b93f4fe";
    static final String _1_19_3 = "1afe2ffe8a9d7fc510442a168b3d4338";
    static final String _1_19_4 = "3009edc0fff87fa34680686663bd59df";
    static final String _1_20   = "34f399b4f2033891290b7f0700e9e47b";
    static final String _1_20_1 = "bcf3dcb22ad42792794079f9443df2c0";
    static final String _1_20_2 = "3478a65bfd04b15b431fe107b3617dfc";
    static final String _1_20_3 = "60a2bb6bf2684dc61c56b90d7c41bddc";
    static final String _1_20_4 = "60a2bb6bf2684dc61c56b90d7c41bddc";
    static final String _1_20_5 = "ad1a88fd7eaf2277f2507bf34d7b994c";
    static final String _1_20_6 = "ee13f98a43b9c5abffdcc0bb24154460";
    static final String _1_21   = "229d7afc75b70a6c388337687ac4da1f";
    static final String _1_21_1 = "7092ff1ff9352ad7e2260dc150e6a3ec";
    static final String _1_21_2 = "ec8b033a89c54252f1dfcb809eab710a";
    static final String _1_21_3 = "61a218cda78417b6039da56e08194083";
    static final String _1_21_4 = "60ac387ca8007aa018e6aeb394a6988c";
    static final String _1_21_5 = "7ecad754373a5fbc43d381d7450c53a5";
    static final String _1_21_6 = "164f8e872cb3dff744982fca079642b2";
    static final String _1_21_7 = "98b42190c84edaa346fd96106ee35d6f";
    static final String _1_21_8 = "98b42190c84edaa346fd96106ee35d6f";
    static final String _1_21_9 = "614efe5192cd0510bc2ddc5feefa155d";

    /**
     * Get the version of the mappings used by CraftBukkit. Note that this method only works on (forks of) CraftBukkit.
     * @note org.bukkit.craftbukkit.util.CraftMagicNumbers#getMappingsVersion() was only introduced at CraftBukkit 1.14
     * @param server the Server instance
     * @return the mappings version (may be used for equality checking only), or null if running on CraftBukkit 1.13.2 or earlier.
     */
    @SuppressWarnings("deprecation")
    static String getMappingsVersion(Server server) {
        UnsafeValues craftMagicNumbers = server.getUnsafe();
        try {
            Method method = craftMagicNumbers.getClass().getMethod("getMappingsVersion");
            return (String) method.invoke(craftMagicNumbers, new Object[0]);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassCastException e) {
            return null;
        }
    }

}
