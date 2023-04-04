package com.janboerman.invsee.spigot.internal;

import java.util.logging.Level;

public class LogRecord {

    public final Level level;
    public final String message;
    public final Throwable cause;

    public LogRecord(Level level, String message, Throwable cause) {
        this.level = level;
        this.message = message;
        this.cause = cause;
    }

}
