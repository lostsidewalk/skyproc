package skyproc;

import lev.debug.LDebug;
import lev.debug.LLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleLogger extends LLogger {

    private static final Logger consoleLog = LoggerFactory.getLogger(ConsoleLogger.class);

    public ConsoleLogger(String debugPath) {
        super(debugPath);
    }

    @Override
    public void logSync(String header, String... log) {
        if (this.loggingSync() || this.syncing && this.loggingAsync()) {
            if (this.syncing) {
                this.w(this.synced, header, log);
            } else {
                this.log(header, log);
            }
        }
    }

    @Override
    public void logMain(String header, String... log) {
        this.w(this.main, header, log);
        this.main.flushDebug();
    }

    @Override
    public void logSpecial(Enum e, String header, String... log) {
        if (this.logging()) {
            LDebug spec = this.special.get(e);
            if (spec != null) {
                this.w(spec, header, log);
            }
        }
    }

    @Override
    public void log(String header, String... log) {
        if (this.loggingAsync()) {
            this.w(this.asynced, header, log);
        }
    }
    //
    //
    //
    private void w(LDebug logStream, String header, String... inputs) {
        logStream.w(header, inputs);
        consoleLog.info("logStream={}, header={}, inputs={}", getLogStreamName(logStream), header, inputs);
    }

    private static String getLogStreamName(LDebug logStream) {
        String[] nameParts = logStream.getClass().getSimpleName().split("\\.");
        return nameParts[nameParts.length - 1];
    }
}
