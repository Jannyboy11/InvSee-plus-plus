package com.janboerman.invsee.metrics;

import com.janboerman.invsee.faststats.FastStats;
import com.janboerman.invsee.spigot.InvseePlusPlus;
import com.janboerman.invsee.spigot.perworldinventory.PerWorldInventorySeeApi;
import com.janboerman.invsee.utils.Compat;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

public final class Metrics {

    private static final int MAJOR_JAVA_VERSION = Compat.majorJavaVersion();

    private final org.bstats.bukkit.Metrics bStats;
    private final dev.faststats.bukkit.BukkitMetrics fastStats;

    private Metrics(
            org.bstats.bukkit.Metrics bStats,
            dev.faststats.bukkit.BukkitMetrics fastStats
    ) {
        this.bStats = bStats;
        this.fastStats = fastStats;
    }

    public static Metrics enable(InvseePlusPlus plugin) {
        String downloadSource = DownloadSource.detect(plugin).toString();
        String backEnd = getBackendMetric(plugin);
        Instant start = Instant.now();

        org.bstats.bukkit.Metrics bStats;
        dev.faststats.bukkit.BukkitMetrics fastStats;

        {
            final int pluginId = 9309;
            bStats = new org.bstats.bukkit.Metrics(plugin, pluginId);
            bStats.addCustomChart(new org.bstats.charts.SimplePie("Back-end", () -> backEnd));  // bStats no longer allows dashes in chartIds in custom charts on their website?!
            bStats.addCustomChart(new org.bstats.charts.SimplePie("downloadSource", () -> downloadSource));
            bStats.addCustomChart(new org.bstats.charts.SimplePie("uptime", () -> getUptime(start).toString()));
        }

        {
            fastStats = dev.faststats.bukkit.BukkitMetrics.factory()
                    .addMetric(dev.faststats.core.data.Metric.number("major_java_version", () -> MAJOR_JAVA_VERSION))
                    .addMetric(dev.faststats.core.data.Metric.string("back_end", () -> backEnd))
                    .addMetric(dev.faststats.core.data.Metric.string("download_source", () -> downloadSource))
                    .addMetric(dev.faststats.core.data.Metric.number("uptime_days", () -> getDaysUptime(start)))
                    .token(FastStats.API_TOKEN)
                    .create(plugin);
            fastStats.ready();
        }

        return new Metrics(bStats, fastStats);
    }

    public void disable() {
        bStats.shutdown();
        fastStats.shutdown();
    }

    private static String getBackendMetric(InvseePlusPlus plugin) {
        if (plugin.getApi() instanceof PerWorldInventorySeeApi) {
            return "PerWorldInventory";
//            } else if (this.api instanceof MultiverseInventoriesSeeApi) {
//                return "Multiverse-Inventories";
        }
        //else if: MyWorlds
        //else if: Separe-World-Items
        //else if: PolyVerse (Lokka30/ArcanePlugins)
        //else if: World (TheNextLvl)
        else {
            return "Vanilla";
        }
    }

    private static Uptime getUptime(Instant from) {
        Instant now = Instant.now();
        return Uptime.calculate(from, now);
    }

    private static int getDaysUptime(Instant from) {
        Instant now = Instant.now();
        return (int)Duration.between(from, now).toDays();
    }
}
