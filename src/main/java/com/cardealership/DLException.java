package com.cardealership;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DLException extends Exception {

    private static final Logger LOGGER = Logger.getLogger(DLException.class.getName());
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String USER_MESSAGE = "Unable to complete operation. Please contact the administrator.";

    private final Exception originalException;
    private final String[] additionalInfo;

    static {
        try {
            Path logDirectory = Path.of("logs");
            Files.createDirectories(logDirectory);

            FileHandler fileHandler = new FileHandler("logs/exceptions.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Failed to initialize exception log file: " + e.getMessage());
        }
    }

    public DLException(Exception exception, String... additionalInfo) {
        super(buildUserMessage(exception), exception);
        this.originalException = exception;
        this.additionalInfo = additionalInfo;
        log();
    }

    private static String buildUserMessage(Exception exception) {
        if (exception instanceof SQLException) {
            return USER_MESSAGE + " (Database error)";
        }
        return USER_MESSAGE + " (Application error)";
    }

    private void log() {
        StringBuilder details = new StringBuilder();
        details.append("Timestamp: ")
                .append(LocalDateTime.now().format(TIMESTAMP_FORMAT))
                .append(System.lineSeparator());
        details.append("Exception Type: ")
                .append(originalException.getClass().getName())
                .append(System.lineSeparator());
        details.append("Message: ")
                .append(originalException.getMessage())
                .append(System.lineSeparator());

        Throwable cause = originalException.getCause();
        if (cause != null) {
            details.append("Cause Type: ")
                    .append(cause.getClass().getName())
                    .append(System.lineSeparator());
            details.append("Cause Message: ")
                    .append(cause.getMessage())
                    .append(System.lineSeparator());
        }

        if (additionalInfo != null && additionalInfo.length > 0) {
            details.append("Additional Info:")
                    .append(System.lineSeparator());
            for (int i = 0; i < additionalInfo.length; i++) {
                details.append("  [")
                        .append(i)
                        .append("] ")
                        .append(additionalInfo[i])
                        .append(System.lineSeparator());
            }
        }

        details.append("Stack Trace:")
                .append(System.lineSeparator())
                .append(stackTraceToString(originalException));

        LOGGER.log(Level.SEVERE, details.toString());
    }

    private String stackTraceToString(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}
