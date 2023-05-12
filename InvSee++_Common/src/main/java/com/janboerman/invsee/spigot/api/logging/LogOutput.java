package com.janboerman.invsee.spigot.api.logging;

import com.janboerman.invsee.spigot.api.target.Target;
import com.janboerman.invsee.utils.Pair;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;

public interface LogOutput {

    public static LogOutput make(Plugin plugin, UUID spectatorId, String spectatorName, Target target, LogOptions logOptions) {
        if (LogOptions.isEmpty(logOptions)) return NoOutput.INSTANCE;

        return LogOutputImpl.of(plugin, spectatorId, spectatorName, target, logOptions);
    }

    public void log(Difference difference);

    //TODO should there be a close method (which closes the Handlers attached to the logger) ?
    //TODO How do I handle logging to the same output(handler) using different formats?
    //TODO it this even possible? I guess I will have to pre-process the format myself.

}

class NoOutput implements LogOutput {

    static final NoOutput INSTANCE = new NoOutput();

    private NoOutput() {}

    @Override
    public void log(Difference difference) {
        // no-op
    }
}

class LogOutputImpl implements LogOutput {

    private static final String LOG_FOLDER_NAME = "InvSee++ logs";

    private static final int
            SPECTATOR_UUID_IDX = 0,
            SPECTATOR_NAME_IDX = 1,
            TAKEN_IDX          = 2,
            GIVEN_IDX          = 3,
            TARGET_IDX         = 4;

    private final UUID spectatorId;
    private final String spectatorName;
    private final Logger logger;
    private final Target targetPlayer;
    private final Map<LogTarget, String> logFormats;

    LogOutputImpl(Plugin plugin, UUID spectatorId, String spectatorName, Target targetPlayer, Set<LogTarget> logTargets, Map<LogTarget, String> formats) {
        this.spectatorId = spectatorId;
        this.spectatorName = spectatorName;
        this.targetPlayer = targetPlayer;
        this.logger = Logger.getLogger("InvSee++." + spectatorId);
        this.logger.setLevel(Level.ALL);
        this.logFormats = formats;

        File logFileFolder = new File(plugin.getDataFolder(), LOG_FOLDER_NAME);
        if (!logTargets.isEmpty()) {
            logFileFolder.mkdirs();
        }

        for (LogTarget target : logTargets) {
            switch (target) {
                case SERVER_LOG_FILE:
                    logger.setParent(plugin.getLogger());
                    break;
                case PLUGIN_LOG_FILE:
                    try {
                        File file = new File(logFileFolder, "_global.log");
                        if (!file.exists()) file.createNewFile();
                        FileHandler fileHandler = new FileHandler(file.getAbsolutePath(), true);
                        fileHandler.setLevel(Level.ALL);
                        fileHandler.setFormatter(new DifferenceFormatter(logFormats.get(LogTarget.PLUGIN_LOG_FILE)));
                        logger.addHandler(fileHandler);
                    } catch (IOException e) {
                        plugin.getLogger().log(Level.SEVERE, "Could not create new file handler", e);
                    }
                    break;
                case SPECTATOR_LOG_FILE:
                    try {
                        File file = new File(logFileFolder, spectatorId + ".log");
                        if (!file.exists()) file.createNewFile();
                        FileHandler fileHandler = new FileHandler(file.getAbsolutePath(), true);
                        fileHandler.setLevel(Level.ALL);
                        fileHandler.setFormatter(new DifferenceFormatter(logFormats.get(LogTarget.SPECTATOR_LOG_FILE)));
                        logger.addHandler(fileHandler);
                    } catch (IOException e) {
                        plugin.getLogger().log(Level.SEVERE, "Could not create new file handler", e);
                    }
                    break;
                case CONSOLE:
                    if (logTargets.contains(LogTarget.SERVER_LOG_FILE)) break; //server logger already outputs to console

                    ConsoleHandler consoleHandler = new ConsoleHandler();
                    consoleHandler.setLevel(Level.ALL);
                    consoleHandler.setFormatter(new DifferenceFormatter(logFormats.get(LogTarget.CONSOLE)));
                    logger.addHandler(consoleHandler);
                    break;
            }
        }

        if (!logTargets.contains(LogTarget.SERVER_LOG_FILE)) {
            logger.setUseParentHandlers(false);
        }
    }

    static LogOutputImpl of(Plugin plugin, UUID spectatorId, String spectatorName, Target target, LogOptions options) {
        return new LogOutputImpl(plugin, spectatorId, spectatorName, target, options.getTargets(), options.getFormats());
    }

    @Override
    public void log(Difference difference) {
        String format = logFormats.get(LogTarget.SERVER_LOG_FILE);
        Date now = new Date();
        Taken taken = Taken.from(difference);
        Given given = Given.from(difference);

        String message = LogOutputImpl.format(format, Level.INFO, now, spectatorId, spectatorName, taken, given, targetPlayer);
        logger.log(Level.INFO, message, new Object[] { spectatorId, spectatorName, taken, given, targetPlayer });
    }

    private static class DifferenceFormatter extends SimpleFormatter {

        private final String format;

        private DifferenceFormatter(String format) {
            this.format = format;
        }

        @Override
        public String format(LogRecord record) {
            Object[] parameters = record.getParameters();
            Date time = new Date(record.getMillis());
            Level level = record.getLevel();
            UUID spectatorId = (UUID) parameters[SPECTATOR_UUID_IDX];
            String spectatorName = (String) parameters[SPECTATOR_NAME_IDX];
            Taken taken = (Taken) parameters[TAKEN_IDX];
            Given given = (Given) parameters[GIVEN_IDX];
            Target targetPlayer = (Target) parameters[TARGET_IDX];
            return LogOutputImpl.format(format, level, time, spectatorId, spectatorName, taken, given, targetPlayer);
        }
    }

    private static String format(String format, Level logLevel, Date date, UUID spectatorId, String spectatorName, Taken taken, Given given, Target target) {
        return processTarget(processGiven(processTaken(processSpectatorName(processSpectatorId(processTime(processDate(processLogLevel(format,
                logLevel), date), date), spectatorId), spectatorName), taken), given), target);
    }

    private static String processLogLevel(String format, Level logLevel) {
        return format.replace("<level>", String.format("%s", logLevel.getLocalizedName()));
    }

    private static String processDate(String format, Date date) {
        return format.replace("<date>", String.format("%tF", date));
    }

    private static String processTime(String format, Date date) {
        return format.replace("<time>", String.format("%tT", date));
    }

    private static String processSpectatorId(String format, UUID spectatorId) {
        return format.replace("<spectator_uuid>", String.format("%s", spectatorId));
    }

    private static String processSpectatorName(String format, String spectatorName) {
        return format.replace("<spectator_name>", String.format("%s", spectatorName));
    }

    private static String processTaken(String format, Taken taken) {
        return format.replace("<taken>", String.format("%s", taken));
    }

    private static String processGiven(String format, Given given) {
        return format.replace("<given>", String.format("%s", given));
    }

    private static String processTarget(String format, Target target) {
        return format.replace("<target>", String.format("%s", target));
    }

    private static class Diff {

        private final List<Pair<ItemType, Integer>> items;

        protected Diff(List<Pair<ItemType, Integer>> items) {
            this.items = Objects.requireNonNull(items);
        }

        @Override
        public String toString() {
            return items.stream()
                    .map(pair -> {
                        ItemType type = pair.getFirst();
                        Material material = type.getMaterial();
                        ItemMeta meta = type.getItemMeta();
                        int amount = pair.getSecond();

                        if (meta == null || meta.equals(new ItemStack(material).getItemMeta())) {
                            return material.name() + " x " + amount;
                        } else {
                            return material.name() + " & " + meta + " x " + amount;
                        }
                    })
                    .collect(Collectors.joining(", ", "[", "]"));
        }
    }

    static class Given extends Diff {

        private Given(List<Pair<ItemType, Integer>> items) {
            super(items);
        }

        static Given from(Difference difference) {
            var diff = difference.getDifference();
            List<Pair<ItemType, Integer>> items = new ArrayList<>(diff.size());
            for (var entry : diff.entrySet()) {
                Integer added = entry.getValue();
                if (added != null && added > 0) {
                    items.add(new Pair<>(entry.getKey(), added));
                }
            }
            return new Given(items);
        }
    }

    static class Taken extends Diff {

        private Taken(List<Pair<ItemType, Integer>> items) {
            super(items);
        }

        static Taken from(Difference difference) {
            var diff = difference.getDifference();
            List<Pair<ItemType, Integer>> items = new ArrayList<>(diff.size());
            for (var entry : diff.entrySet()) {
                Integer added = entry.getValue();
                if (added != null && added < 0) {
                    items.add(new Pair<>(entry.getKey(), -1 * added));
                }
            }
            return new Taken(items);
        }
    }

}
