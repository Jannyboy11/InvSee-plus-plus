package com.janboerman.invsee.spigot.api.logging;

import static com.janboerman.invsee.spigot.api.logging.LogTarget.*;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Options for logging interactions with {@link com.janboerman.invsee.spigot.api.SpectatorInventoryView}s.
 *
 * @see com.janboerman.invsee.spigot.api.CreationOptions
 */
public class LogOptions implements Cloneable {

    public static final String FORMAT_SERVER_LOG_FILE =
            "\nSpectator UUID: <spectator_uuid>" +
            "\nSpectator Name: <spectator_name>" +
            "\nTaken         : <taken>" +
            "\nGiven         : <given>" +
            "\nTarget        : <target>";
    public static final String FORMAT_PLUGIN_LOG_FILE =
            "\n[<date> <time>] [<level>]:" +
            "\nSpectator UUID: <spectator_uuid>" +
            "\nSpectator Name: <spectator_name>" +
            "\nTaken         : <taken>" +
            "\nGiven         : <given>" +
            "\nTarget        : <target>";
    public static final String FORMAT_SPECTATOR_LOG_FILE =
            "\n[<date> <time>] [<level>]:" +
            "\nTaken         : <taken>" +
            "\nGiven         : <given>" +
            "\nTarget        : <target>";
    public static final String FORMAT_CONSOLE = FORMAT_PLUGIN_LOG_FILE;

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

    /**
     * Create new logging options.
     * @param granularity the granularity at which to log
     * @param logTargets where to log
     * @param formats in which formats to log
     * @return the new logging options
     */
    public static LogOptions of(LogGranularity granularity, Set<LogTarget> logTargets, EnumMap<LogTarget, String> formats) {
        return new LogOptions(granularity, logTargets, formats);
    }

    /**
     * Get whether the logging options are effectively zero.
     * @param options the logging options
     * @return true if the logging options don't cause any logging, otherwise false
     */
    public static boolean isEmpty(LogOptions options) {
        if (options == null) return true;

        LogGranularity granularity = options.granularity;
        Set<LogTarget> targets = options.logTargets;

        return granularity == null || granularity == LogGranularity.LOG_NEVER
                || targets == null || targets.isEmpty();
    }

    /**
     * Create new no-op logging options.
     * @return new logging options.
     */
    public static LogOptions empty() {
        return new LogOptions(LogGranularity.LOG_NEVER, EnumSet.noneOf(LogTarget.class), new EnumMap<>(LogTarget.class));
    }

    /**
     * Create a deep copy of these logging options.
     * @return a deep copy of these options
     */
    @Override
    public LogOptions clone() {
        return new LogOptions(getGranularity(), getTargets(), getFormats());
    }

    /** Set the log granularity. */
    public LogOptions withGranularity(LogGranularity granularity) {
        this.granularity = granularity;
        return this;
    }

    /** Set the log targets. */
    public LogOptions withLogTargets(Collection<LogTarget> logTargets) {
        this.logTargets = logTargets == null ? null : EnumSet.copyOf(logTargets);
        return this;
    }

    /** Set the log formats. */
    public LogOptions withFormats(Map<LogTarget, String> logFormats) {
        this.formats = logFormats == null ? null : new EnumMap<>(logFormats);
        return this;
    }

    /** Get the log granularity. */
    public LogGranularity getGranularity() {
        if (granularity == null) return LogGranularity.LOG_ON_CLOSE;

        return granularity;
    }

    /** Get the log targets. */
    public Set<LogTarget> getTargets() {
        if (logTargets == null) return EnumSet.allOf(LogTarget.class);

        return Collections.unmodifiableSet(logTargets);
    }

    /** Get the log formats. */
    public Map<LogTarget, String> getFormats() {
        if (formats == null) return Map.of(
                SERVER_LOG_FILE, FORMAT_SERVER_LOG_FILE,
                PLUGIN_LOG_FILE, FORMAT_PLUGIN_LOG_FILE,
                SPECTATOR_LOG_FILE, FORMAT_SPECTATOR_LOG_FILE,
                CONSOLE, FORMAT_CONSOLE
        );

        return Collections.unmodifiableMap(formats);
    }

    /** Get the log format for a given log target. */
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

    /**
     * Get the default log formats.
     * @return a new map instance.
     */
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
