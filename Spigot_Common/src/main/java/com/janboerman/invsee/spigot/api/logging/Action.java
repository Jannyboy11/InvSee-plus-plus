package com.janboerman.invsee.spigot.api.logging;

import com.janboerman.invsee.spigot.api.logging.LogOutputImpl.Given;
import com.janboerman.invsee.spigot.api.logging.LogOutputImpl.Taken;
import com.janboerman.invsee.spigot.api.target.Target;

import java.util.Formattable;
import java.util.Formatter;
import java.util.UUID;

class Action implements Formattable {

    private static final String FORMAT =
            "\nSpectator UUID: %1s" +
            "\nSpectator Name: %2s" +
            "\nTaken:          %3s" +
            "\nGiven:          %4s" +
            "\nTarget:         %5s";

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
