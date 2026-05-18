package common.logger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AppLogger {
    private final Logger logger;

    private AppLogger(Class<?> source) {
        this.logger = Logger.getLogger(source.getName());
    }

    public static AppLogger of(Class<?> source) {
        return new AppLogger(source);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void warn(String message) {
        logger.warning(message);
    }

    public void error(String message, Throwable error) {
        logger.log(Level.SEVERE, message, error);
    }

    public void error(String message) {
        logger.severe(message);
    }

    public void debug(String message) {
        logger.fine(message);
    }
}
