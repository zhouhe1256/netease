package netease.zh.com.neteasemaven.netease.util.log;

/**
 * Created by xtzzz on 15/8/20.
 */
public interface ILogger {
    void v(String tag, String message);

    void d(String tag, String message);

    void i(String tag, String message);

    void w(String tag, String message);

    void e(String tag, String message);

    void println(int priority, String tag, String message);
}
