package com.janboerman.invsee.spigot.logging;

import com.janboerman.invsee.spigot.api.logging.Difference;
import com.janboerman.invsee.spigot.api.logging.ItemType;
import org.bukkit.Material;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class FormattingTest {

    //TODO convert this to an actual JUnit test.

    public static void main(String[] args) {
        Difference difference = new Difference();
        difference.accumulate(new ItemType(Material.DIAMOND, null), 64);
        difference.accumulate(new ItemType(Material.DIAMOND, null), -13);
        difference.accumulate(new ItemType(Material.OBSIDIAN, null), 51);
        difference.accumulate(new ItemType(Material.OBSIDIAN, null), -51);


        String format =
                "[%1$tF %1$tT] [%2$-7s] Spectator UUID: %s\n" +
                "[%1$tF %1$tT] [%2$-7s] Spectator Name: %s\n" +
                "[%1$tF %1$tT] [%2$-7s] Taken:          %s\n" +
                "[%1$tF %1$tT] [%2$-7s] Given:          %s\n" +
                "[%1$tF %1$tT] [%2$-7s] Target:         %s\n";

        Logger logger = Logger.getLogger("Invsee++.foo");
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(new SimpleFormatter() {
            private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(format, new Date(lr.getMillis()), lr.getLevel().getLocalizedName(), lr.getMessage());
            }
        });

        logger.addHandler(consoleHandler);

        logger.info("foo!");
    }

}
