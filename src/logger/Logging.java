package logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Class for handling logging for client and server
 */
public class Logging {
    private final FileHandler logHandler;

    public Logging(FileHandler logHandler) {
        this.logHandler = logHandler;
    }

    /**
     * Formatting the logging output, so it has the logging level,
     * date and time precise to milliseconds and the logging message
     */
    public void formatLogging() {
        logHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                SimpleDateFormat logDate = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(record.getMillis());
                return record.getLevel() + " || " + logDate.format(cal.getTime()) + " "
                        + record.getMillis() + " || " + record.getMessage() + "\n";
            }
        });
    }
}
