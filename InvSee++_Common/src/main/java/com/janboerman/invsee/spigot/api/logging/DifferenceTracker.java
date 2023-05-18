package com.janboerman.invsee.spigot.api.logging;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DifferenceTracker {

    private final LogOutput output;
    private final LogGranularity granularity;

    private Difference difference;

    public DifferenceTracker(LogOutput output, LogGranularity granularity) {
        assert granularity != LogGranularity.LOG_NEVER : "Should not be instantiated with " + LogGranularity.class.getSimpleName() + "." + LogGranularity.LOG_NEVER.name();
        this.output = output;
        this.granularity = granularity;
    }

    public void onOpen() {
        difference = new Difference();
    }

    public void onClick(List<ItemStack> oldItems, List<ItemStack> newItems) {
        Difference diff = Difference.calculate(oldItems, newItems);
        if (granularity == LogGranularity.LOG_EVERY_CHANGE && !diff.isEmpty()) {
            output.log(diff);
        }
        difference.merge(diff);
    }

    public void onClose() {
        if (granularity == LogGranularity.LOG_ON_CLOSE && !difference.isEmpty()) {
            output.log(difference);
        }
        output.close();
    }

    public Difference getDifference() {
        return difference;
    }
}
