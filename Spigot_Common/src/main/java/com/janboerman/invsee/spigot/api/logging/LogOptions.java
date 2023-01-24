package com.janboerman.invsee.spigot.api.logging;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class LogOptions implements Cloneable {

    private LogGranularity granularity;
    private EnumSet<LogTarget> logTargets;

    private LogOptions(LogGranularity granularity, Set<LogTarget> logTargets) {
        this.granularity = granularity;
        this.logTargets = logTargets == null ? null : EnumSet.copyOf(logTargets);
    }

    public LogOptions() {
    }

    public static LogOptions of(LogGranularity granularity, Set<LogTarget> logTargets) {
        return new LogOptions(granularity, logTargets);
    }

    public static boolean isEmpty(LogOptions options) {
        if (options == null) return true;

        LogGranularity granularity = options.granularity;
        Set<LogTarget> targets = options.logTargets;

        return granularity == null || granularity == LogGranularity.LOG_NEVER
                || targets == null || targets.isEmpty();
    }

    public static LogOptions empty() {
        return new LogOptions(LogGranularity.LOG_NEVER, EnumSet.noneOf(LogTarget.class));
    }

    @Override
    public LogOptions clone() {
        return new LogOptions(getGranularity(), getTargets());
    }

    public LogOptions withGranularity(LogGranularity granularity) {
        this.granularity = granularity;
        return this;
    }

    public LogOptions withLogTargets(Collection<LogTarget> logTargets) {
        this.logTargets = logTargets == null ? null : EnumSet.copyOf(logTargets);
        return this;
    }

    public LogGranularity getGranularity() {
        if (granularity == null) return LogGranularity.LOG_ON_CLOSE;

        return granularity;
    }

    public Set<LogTarget> getTargets() {
        if (logTargets == null) return EnumSet.allOf(LogTarget.class);

        return Collections.unmodifiableSet(logTargets);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof LogOptions)) return false;

        LogOptions that = (LogOptions) o;
        return this.getGranularity() == that.getGranularity()
                && Objects.equals(this.getTargets(), that.getTargets());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGranularity(), getTargets());
    }

    @Override
    public String toString() {
        return "LogOptions" +
                "{granularity=" + getGranularity() +
                ",logTargets=" + getTargets() +
                "}";
    }

}
