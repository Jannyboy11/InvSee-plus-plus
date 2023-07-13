package com.janboerman.invsee.folia;

public class FoliaSupport {

    private static final boolean FOLIA;
    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        FOLIA = folia;
    }

    private FoliaSupport() {}

    public static boolean isFolia() {
        return FOLIA;
    }

}
