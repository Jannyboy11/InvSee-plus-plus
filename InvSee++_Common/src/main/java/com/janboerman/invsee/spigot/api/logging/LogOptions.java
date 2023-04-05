package com.janboerman.invsee.spigot.api.logging;

import static com.janboerman.invsee.spigot.api.logging.LogTarget.*;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class LogOptions implements Cloneable {

    private static final String FORMAT_SERVER_LOG_FILE =
            "\nSpectator UUID: <spectator_uuid>" +
            "\nSpectator Name: <spectator_name>" +
            "\nTaken         : <taken>" +
            "\nGiven         : <given>" +
            "\nTarget        : <target>";
    private static final String FORMAT_PLUGIN_LOG_FILE =
            "\n[<date> <time>] [<level>]:" +
            "\nSpectator UUID: <spectator_uuid>" +
            "\nSpectator Name: <spectator_name>" +
            "\nTaken         : <taken>" +
            "\nGiven         : <given>" +
            "\nTarget        : <target>";
    private static final String FORMAT_SPECTATOR_LOG_FILE =
            "\n[<date> <time>] [<level>]:" +
            "\nTaken         : <taken>" +
            "\nGiven         : <given>" +
            "\nTarget        : <target>";
    private static final String FORMAT_CONSOLE = FORMAT_PLUGIN_LOG_FILE;

    private LogGranularity granularity;
    private EnumSet<LogTarget> logTargets;
    private EnumMap<LogTarget, String> formats;

    private LogOptions(LogGranularity granularity, Set<LogTarget> logTargets, Map<LogTarget, String> formats) {
        this.granularity = granularity;
        this.logTargets = logTargets == null ? null : EnumSet.copyOf(logTargets);
        this.formats = formats == null ? null : new EnumMap<>(formats);
    }

    public LogOptions() {
    }

    @Deprecated(since = "0.19.1")
    public static LogOptions of(LogGranularity granularity, Set<LogTarget> logTargets) {
        return new LogOptions(granularity, logTargets, null);
    }

    public static LogOptions of(LogGranularity granularity, Set<LogTarget> logTargets, EnumMap<LogTarget, String> formats) {
        return new LogOptions(granularity, logTargets, formats);
    }

    public static boolean isEmpty(LogOptions options) {
        if (options == null) return true;

        LogGranularity granularity = options.granularity;
        Set<LogTarget> targets = options.logTargets;

        return granularity == null || granularity == LogGranularity.LOG_NEVER
                || targets == null || targets.isEmpty();
    }

    public static LogOptions empty() {
        return new LogOptions(LogGranularity.LOG_NEVER, EnumSet.noneOf(LogTarget.class), new EnumMap<>(LogTarget.class));
    }

    @Override
    public LogOptions clone() {
        return new LogOptions(getGranularity(), getTargets(), getFormats());
    }

    public LogOptions withGranularity(LogGranularity granularity) {
        this.granularity = granularity;
        return this;
    }

    public LogOptions withLogTargets(Collection<LogTarget> logTargets) {
        this.logTargets = logTargets == null ? null : EnumSet.copyOf(logTargets);
        return this;
    }

    public LogOptions withFormats(Map<LogTarget, String> logFormats) {
        this.formats = logFormats == null ? null : new EnumMap<>(logFormats);
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

    public Map<LogTarget, String> getFormats() {
        if (formats == null) return Map.of(
                SERVER_LOG_FILE, FORMAT_SERVER_LOG_FILE,
                PLUGIN_LOG_FILE, FORMAT_PLUGIN_LOG_FILE,
                SPECTATOR_LOG_FILE, FORMAT_SPECTATOR_LOG_FILE,
                CONSOLE, FORMAT_CONSOLE
        );

        return Collections.unmodifiableMap(formats);
    }

    public String getFormat(LogTarget logTarget) {
        if (logTarget == null) {
            return FORMAT_PLUGIN_LOG_FILE;
        } else if (formats == null || !formats.containsKey(logTarget)) {
            switch (logTarget) {
                case SERVER_LOG_FILE: return FORMAT_SERVER_LOG_FILE;
                case PLUGIN_LOG_FILE: return FORMAT_PLUGIN_LOG_FILE;
                case SPECTATOR_LOG_FILE: return FORMAT_SPECTATOR_LOG_FILE;
                case CONSOLE: return FORMAT_CONSOLE;
                default: throw new RuntimeException("Unrecognised LogTarget: "+ logTarget);
            }
        } else {
            return formats.get(logTarget);
        }
    }

    public static EnumMap<LogTarget, String> defaultLogFormats() {
        EnumMap<LogTarget, String> map = new EnumMap<>(LogTarget.class);
        map.put(SERVER_LOG_FILE, FORMAT_SERVER_LOG_FILE);
        map.put(PLUGIN_LOG_FILE, FORMAT_PLUGIN_LOG_FILE);
        map.put(SPECTATOR_LOG_FILE, FORMAT_SPECTATOR_LOG_FILE);
        map.put(CONSOLE, FORMAT_CONSOLE);
        return map;
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
