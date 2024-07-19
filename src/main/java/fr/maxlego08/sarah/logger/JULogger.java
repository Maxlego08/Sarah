package fr.maxlego08.sarah.logger;

public class JULogger {

    public static Logger from(java.util.logging.Logger logger) {
        return new Logger() {
            @Override
            public void info(String string) {
                logger.info(string);
            }
        };
    }

}
