package netease.zh.com.neteasemaven.netease.util;


import netease.zh.com.neteasemaven.netease.util.log.ILogger;
import netease.zh.com.neteasemaven.netease.util.log.PrintToLogCatLogger;

public class Logger {

    /**
     * 打开或者关闭日志的打印
     */
    private static boolean DEBUG = true;

    public static enum Level {
        VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT
    }

    private static ILogger defaultLogger = new PrintToLogCatLogger();

    private Logger() {
    }

    public static void setDebug(boolean DEBUG) {
        Logger.DEBUG = DEBUG;
    }

    public static void setLogger(ILogger defaultLogger) {
        Logger.defaultLogger = defaultLogger;
    }

    public static void d(Object object, String message) {
        printLogger(Level.DEBUG, object, message);
    }

    public static void e(Object object, String message) {
        printLogger(Level.ERROR, object, message);
    }

    public static void i(Object object, String message) {
        printLogger(Level.INFO, object, message);
    }

    public static void v(Object object, String message) {
        printLogger(Level.VERBOSE, object, message);
    }

    public static void w(Object object, String message) {
        printLogger(Level.WARN, object, message);
    }

    public static void d(String tag, String message) {
        printLogger(Level.DEBUG, tag, message);
    }

    public static void e(String tag, String message) {
        printLogger(Level.ERROR, tag, message);
    }

    public static void i(String tag, String message) {
        printLogger(Level.INFO, tag, message);
    }

    public static void v(String tag, String message) {
        printLogger(Level.VERBOSE, tag, message);
    }

    public static void w(String tag, String message) {
        printLogger(Level.WARN, tag, message);
    }

    public static void println(Level level, String tag, String message) {
        printLogger(level, tag, message);
    }

    private static void printLogger(Level level, Object object, String message) {
        Class<?> cls = object.getClass();
        String tag = cls.getName();
        String arrays[] = tag.split("\\.");
        tag = arrays[arrays.length - 1];
        printLogger(level, tag, message);
    }

    private static void printLogger(Level level, String tag, String message) {
        if (DEBUG) {
            printLogger(defaultLogger, level, tag, message);
        }
    }

    private static void printLogger(ILogger logger, Level level, String tag,
                                    String message) {
        switch (level) {
            case VERBOSE:
                logger.v(tag, message);
                break;
            case DEBUG:
                logger.d(tag, message);
                break;
            case INFO:
                logger.i(tag, message);
                break;
            case WARN:
                logger.w(tag, message);
                break;
            case ERROR:
                logger.e(tag, message);
                break;
            default:
                break;
        }
    }
}
