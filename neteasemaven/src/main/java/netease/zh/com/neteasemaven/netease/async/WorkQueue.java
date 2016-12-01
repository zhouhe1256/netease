package netease.zh.com.neteasemaven.netease.async;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorkQueue {
    private static final WorkQueue instance = new WorkQueue(2);

    private final ExecutorService executor;

    public WorkQueue(int maxThreadNum) {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
        executor = new ThreadPoolExecutor(0, maxThreadNum, 3, TimeUnit.SECONDS, queue);
    }

    public IPromise add(final Runnable work) {
        final Deferred deferred = new Deferred(new LooperCallbackExecutor());
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    work.run();
                    deferred.resolved(Arguments.create());
                } catch (Exception e) {
                    deferred.reject(Arguments.create(e));
                }
            }
        });
        return deferred;
    }

    public static IPromise run(Runnable work) {
        return instance.add(work);
    }

    public static void runOnUiThread(Runnable runnable) {
        runOnUiThread(runnable, 0);
    }

    public static void runOnUiThread(Runnable runnable, long delay) {
        if (delay == 0 && Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(runnable, delay);
        }
    }

}
