package netease.zh.com.neteasemaven.netease.remote;



import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import netease.zh.com.neteasemaven.netease.async.ICallback;
import netease.zh.com.neteasemaven.netease.async.ICallbackExecutor;
import netease.zh.com.neteasemaven.netease.async.LooperCallbackExecutor;
import netease.zh.com.neteasemaven.netease.cache.ICache;

public class Http {
    private static final String DEFAULT_USER_AGENT = "android";
    private static final String DEFAULT_ACCEPT_ENCODING = "gzip";
    private static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";

    private static final Http defaultInstance;
    private static final Http imageInstance;

    static {
        defaultInstance = new Http(2).setContentDecoder(new IContentDecoder.JSONDecoder());
        imageInstance = new Http(6).setContentDecoder(new IContentDecoder.BitmapDecoder());

        LooperCallbackExecutor callbackExecutor = new LooperCallbackExecutor();
        defaultInstance.callbackExecutor(callbackExecutor);
        imageInstance.callbackExecutor(callbackExecutor);
    }

    private ExecutorService executor;
    private HttpSettings httpSettings = new HttpSettings();

    private IContentDecoder<?> contentDecoder = new IContentDecoder.BinaryDecoder();

    public Http(int maxThreadNum) {
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
        executor = new ThreadPoolExecutor(1, maxThreadNum, 3, TimeUnit.SECONDS, queue);
        httpSettings.set(HttpOption.USER_AGENT, DEFAULT_USER_AGENT);
        httpSettings.set(HttpOption.ACCEPT_ENCODING, DEFAULT_ACCEPT_ENCODING);
        httpSettings.set(HttpOption.REQUEST_CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
    }

    public Http option(HttpOption name, Object value) {
        httpSettings.set(name, value);
        return this;
    }

    public Http baseUrl(String baseUrl) {
        httpSettings.set(HttpOption.BASE_URL, baseUrl);
        return this;
    }

    public Http param(String name, Object value) {
        httpSettings.param(name, value);
        return this;
    }

    public Http setContentDecoder(IContentDecoder decoder) {
        this.contentDecoder = decoder;
        return this;
    }

    public <K, V> Http cache(ICache<K, V> cache) {
        httpSettings.set(HttpOption.CACHE, cache);
        return this;
    }

    public Http fallbackToCache(boolean b) {
        httpSettings.set(HttpOption.FALLBACK_TO_CACHE, b);
        return this;
    }

    public Http aheadReadInCache(boolean b) {
        httpSettings.set(HttpOption.AHEAD_READ_IN_CACHE, b);
        return this;
    }

    public Http callbackExecutor(ICallbackExecutor callbackExecutor) {
        httpSettings.set(HttpOption.CALLBACK_EXECUTOR, callbackExecutor);
        return this;
    }

    /**
     * http请求开始前触发.
     *
     * @param callback
     * @return
     */
    public Http start(ICallback callback) {
        httpSettings.addCallback(HttpOption.START_CALLBACK, callback);
        return this;
    }

    public Http progress(ICallback callback) {
        httpSettings.addCallback(HttpOption.PROGRESS_CALLBACK, callback);
        return this;
    }

    public Http done(ICallback callback) {
        httpSettings.addCallback(HttpOption.DONE_CALLBACK, callback);
        return this;
    }

    public Http fail(ICallback callback) {
        httpSettings.addCallback(HttpOption.FAIL_CALLBACK, callback);
        return this;
    }

    public Http always(ICallback callback) {
        httpSettings.addCallback(HttpOption.ALWAYS_CALLBACK, callback);
        return this;
    }

    /**
     * http请求完成，always callback调用后触发。
     *
     * @param callback
     * @return
     */
    public Http complete(ICallback callback) {
        httpSettings.addCallback(HttpOption.COMPLETE_CALLBACK, callback);
        return this;
    }

    private HttpClient createHttpClient(HttpClient.Method method, String url) {
        return new HttpClient(executor, method, url).
                settings(httpSettings).contentDecoder(contentDecoder);
    }

    public HttpClient get(String url) {
        return createHttpClient(HttpClient.Method.GET, url);
    }

    public HttpClient post(String url) {
        return createHttpClient(HttpClient.Method.POST, url);
    }

    public HttpClient put(String url) {
        return createHttpClient(HttpClient.Method.PUT, url);
    }

    public HttpClient delete(String url) {
        return createHttpClient(HttpClient.Method.DELETE, url);
    }

    public static Http instance() {
        return defaultInstance;
    }

    public static Http imageInstance() {
        return imageInstance;
    }

}
