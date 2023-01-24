package com.janboerman.invsee.spigot.api.logging;

import com.janboerman.invsee.spigot.api.logging.LogOutputImpl.Given;
import com.janboerman.invsee.spigot.api.logging.LogOutputImpl.Taken;
import com.janboerman.invsee.spigot.api.target.Target;

import java.util.Formattable;
import java.util.Formatter;
import java.util.UUID;

class Action implements Formattable {

    private static final String FORMAT =
            "\nSpectator UUID: %1$s" +
            "\nSpectator Name: %2$s" +
            "\nTaken         : %3$s" +
            "\nGiven         : %4$s" +
            "\nTarget        : %5$s";

    final UUID spectatorId;
    final String spectatorName;
    final Target targetPlayer;
    final Difference outcome;

    Action(UUID spectatorId, String spectatorName, Target target, Difference outcome) {
        this.spectatorId = spectatorId;
        this.spectatorName = spectatorName;
        this.targetPlayer = target;
        this.outcome = outcome;
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision) {
        formatter.format(FORMAT, spectatorId, spectatorName, Taken.from(outcome), Given.from(outcome), targetPlayer);
    }

}
