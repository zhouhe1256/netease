package netease.zh.com.neteasemaven.netease.util.log;

import android.util.Log;

/**
 * Created by xtzzz on 15/8/20.
 */
public class PrintToLogCatLogger implements ILogger {
    @Override
    public void v(String tag, String message) {
        Log.v(tag, message);
    }

    @Override
    public void d(String tag, String message) {
        Log.d(tag, message);
    }

    @Override
    public void i(String tag, String message) {
        Log.i(tag, message);
    }

    @Override
    public void w(String tag, String message) {
        Log.w(tag, message);
    }

    @Override
    public void e(String tag, String message) {
        Log.e(tag, message);
    }

    @Override
    public void println(int priority, String tag, String message) {
        Log.println(priority, tag, message);
    }
}
