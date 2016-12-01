package netease.zh.com.neteasemaven.netease.remote;




import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;

import netease.zh.com.neteasemaven.netease.async.Arguments;
import netease.zh.com.neteasemaven.netease.async.Deferred;
import netease.zh.com.neteasemaven.netease.async.ICallback;
import netease.zh.com.neteasemaven.netease.async.ICallbackExecutor;
import netease.zh.com.neteasemaven.netease.async.ICancellable;
import netease.zh.com.neteasemaven.netease.async.IPromise;
import netease.zh.com.neteasemaven.netease.async.State;
import netease.zh.com.neteasemaven.netease.cache.ICache;
import netease.zh.com.neteasemaven.netease.json.JSONUtil;
import netease.zh.com.neteasemaven.netease.util.Logger;

/**
 * 不使用Content-Type检测, 要与Cache兼容太麻烦。
 */
public class HttpClient implements ICancellable {

    static enum Method {
        GET, POST, PUT, DELETE
    }

    public static enum Progress {
        CONNECTED, DATA_SENT, READING, DECOMPRESSING, DONE
    }

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private final ExecutorService executor;
    private final Method method;
    private final String url;
    private boolean cacheFlag;
    private HttpSettings httpSettings = new HttpSettings();

    private Map<String, String> headers = new HashMap<String, String>();
    private Map<String, Object> params = new HashMap<String, Object>();

    private Charset paramCharset = DEFAULT_CHARSET;
    private boolean encodeParamsInUrl = true;
    private byte[] data;

    private IContentDecoder<?> contentDecoder = new IContentDecoder.BinaryDecoder();

    private Fetcher fetcher;
    private Future<?> future;

    private Object mock;

    //private boolean fallbackToCache;
    //private boolean aheadReadInCache;

    HttpClient(ExecutorService executor, Method method, String url) {
        this.executor = executor;
        this.method = method;
        this.url = url;
    }

    public HttpClient settings(HttpSettings settings) {
        this.httpSettings = new HttpSettings(settings);
        return this;
    }

    public HttpClient ignoreGlobalCallbacks() {
        httpSettings.removeAllCallbacks();
        return this;
    }

    public HttpClient callbackExecutor(ICallbackExecutor executor) {
        httpSettings.set(HttpOption.CALLBACK_EXECUTOR, executor);
        return this;
    }

    public HttpClient option(HttpOption name, Object value) {
        httpSettings.set(name, value);
        return this;
    }

    public HttpClient header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public HttpClient requestContentType(String value) {
        httpSettings.set(HttpOption.REQUEST_CONTENT_TYPE, value);
        return this;
    }

    public HttpClient param(String name, Object value) {
        if (value != null)
            params.put(name, value);
        return this;
    }

    public HttpClient param(Map<String, String> keyValues) {
        params.putAll(keyValues);
        return this;
    }

    public HttpClient data(byte[] data) {
        this.data = data;
        httpSettings.set(HttpOption.REQUEST_CONTENT_TYPE, "binary");
        return this;
    }
public HttpClient isCache(boolean f){
    this.cacheFlag=f;
    return this;
}
    public HttpClient encodeParamsInUrl(boolean b) {
        encodeParamsInUrl = b;
        return this;
    }

    public HttpClient contentDecoder(IContentDecoder contentDecoder) {
        this.contentDecoder = contentDecoder;
        return this;
    }

    /**
     * 远程调用失败时从缓存读取。
     *
     * @param fallbackToCache
     * @return
     */
    public HttpClient fallbackToCache(boolean fallbackToCache) {
        httpSettings.set(HttpOption.FALLBACK_TO_CACHE, fallbackToCache);
        return this;
    }

    private boolean isFallbackToCache() {
        return httpSettings.get(HttpOption.FALLBACK_TO_CACHE, Boolean.FALSE);
    }

    public HttpClient aheadReadInCache(boolean aheadReadInCache) {
        httpSettings.set(HttpOption.AHEAD_READ_IN_CACHE, aheadReadInCache);
        return this;
    }

    private boolean isAheadReadInCache() {
        return httpSettings.get(HttpOption.AHEAD_READ_IN_CACHE, Boolean.FALSE);
    }

    public HttpClient cache(ICache<String, byte[]> cache) {
        httpSettings.set(HttpOption.CACHE, cache);
        return this;
    }

    private ICache<String, byte[]> getCache() {
        return httpSettings.get(HttpOption.CACHE);
    }

    public IPromise run() {
        ICallbackExecutor executor = httpSettings
                .get(HttpOption.CALLBACK_EXECUTOR);
        if (executor == null) {
            executor = new ICallbackExecutor.CurrentThreadExecutor();
        }
        return run(executor);
    }

    public HttpClient mock(Object mock) {
        this.mock = mock;
        return this;
    }

    private void call(ICallbackExecutor callbackExecutor,
                      HttpOption callbackName) {
        ICallback callback = httpSettings.getCallback(callbackName);
        if (callback != null) {
            callbackExecutor.run(callback, Arguments.create(this));
        }
    }

    private void setupDeferredOptions(Deferred deferred) {
        HttpSettings o = httpSettings;

        ICallback callback = o.getCallback(HttpOption.PROGRESS_CALLBACK);
        if (callback != null) {
            deferred.progress(callback);
        }

        callback = o.getCallback(HttpOption.DONE_CALLBACK);
        if (callback != null) {
            deferred.done(callback);
        }

        callback = o.getCallback(HttpOption.FAIL_CALLBACK);
        if (callback != null) {
            deferred.fail(callback);
        }

        callback = o.getCallback(HttpOption.ALWAYS_CALLBACK);
        if (callback != null) {
            deferred.always(callback);
        }
    }

    public Deferred run(final ICallbackExecutor callbackExecutor) {
        final Deferred deferred = new Deferred(callbackExecutor);
        setupDeferredOptions(deferred);

        call(callbackExecutor, HttpOption.START_CALLBACK);

        if (mock != null) {
            try {
                String json = mock.toString();
                if (!(mock instanceof String)) {
                    JSONUtil.dump(mock);
                }
                Arguments arguments = buildArguments(json.getBytes(DEFAULT_CHARSET));

                deferred.resolved(arguments);
                return deferred;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        byte[] data = readInCache(url);
        if (data != null) {
            try {
                Arguments arguments = buildArguments(data);
                deferred.resolved(arguments);
                return deferred;
            } catch (Exception e) {
            }
        }

        fetcher = new Fetcher(deferred, callbackExecutor);
        future = executor.submit(fetcher);
        return deferred;
    }

    public void doCancel(Arguments arguments) {
        if (future != null && !future.isDone()) {
            future.cancel(true);
            future = null;

            // 可能Fetcher还在executor队列就被取消
            if (!fetcher.isStarted()) {
                fetcher.notifyCompleted();
            }
        }
    }

    private byte[] readInCache(String url) {
        if(cacheFlag==false){
            return null;
        }
        ICache<String, byte[]> cache = getCache();
        if (isAheadReadInCache() && cache != null) {
            return cache.get(url);
        }
        return null;
    }

    private byte[] fallback(String url) {
        if(cacheFlag==false){
            return null;
        }
        ICache<String, byte[]> cache = getCache();
        if (isFallbackToCache() && cache != null) {
            return cache.get(url);
        }
        return null;
    }

    private void addToCache(String url, byte[] data) {
        if (data == null) {
            return;
        }
        if(cacheFlag==false){
            return;
        }
        ICache<String, byte[]> cache = getCache();
        if (cache != null) {
            cache.set(url, data);
        }
    }

    private String getMime() {
        return httpSettings.get(HttpOption.MIME);
    }

    private Charset getCharset() {
        if (httpSettings.has(HttpOption.CHARSET)) {
            return httpSettings.get(HttpOption.CHARSET);
        }

        return DEFAULT_CHARSET;
    }

    private Arguments buildArguments(byte[] data) throws Exception {
        Arguments arguments = new Arguments();

        ContentType contentType = new ContentType(getMime(), getCharset());
        Object content = contentDecoder.decode(contentType, data);
        arguments.add(content);

        arguments.add(HttpClient.this);
        arguments.add(data);
        return arguments;
    }

    class Fetcher implements Runnable {
        private final Deferred deferred;
        private final ICallbackExecutor callbackExecutor;

        private volatile boolean started;
        private HttpURLConnection connection;

        public Fetcher(Deferred deferred, ICallbackExecutor callbackExecutor) {
            this.deferred = deferred;
            this.callbackExecutor = callbackExecutor;
        }

        public boolean isStarted() {
            return started;
        }

        // 兼容android level 8
        private byte[] getBytes(String s, Charset charset) {
            try {
                return s.getBytes(charset.name());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        private String encodeParams() {
            Map<String, Object> map = new HashMap<String, Object>(
                    httpSettings.getParams());
            map.putAll(params);

            if (map.isEmpty()) {
                return "";
            }

            StringBuilder ss = new StringBuilder();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (ss.length() > 0) {
                    ss.append("&");
                }
                ss.append(encodeUrl(key)).append("=").append(encodeUrl(value));
            }
            return ss.toString();
        }

        private String encodeUrl(Object value) {
            if (value == null) {
                return "";
            }
            try {
                return URLEncoder.encode(value.toString(), paramCharset.name());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        private String buildFinalUrl() {
            String url = HttpClient.this.url;
            String baseUrl = httpSettings.getString(HttpOption.BASE_URL);
            if (baseUrl != null
                    && !(url.startsWith("http://") || url
                    .startsWith("https://"))) {
                if (baseUrl.endsWith("/") && url.startsWith("/")) {
                    url = url.substring(1);
                }
                if (!baseUrl.endsWith("/") && !url.startsWith("/")) {
                    url = "/" + url;
                }

                url = baseUrl + url;
            }
            if (!encodeParamsInUrl) {
                return url;
            }

            String queryString = encodeParams();
            if (queryString.length() == 0) {
                return url;
            }

            if (url.contains("?")) {
                return url + "&" + queryString;
            }
            Logger.i("url", url + "?" + queryString);
            return url + "?" + queryString;
        }

        private HttpURLConnection openConnection() throws IOException {
            return (HttpURLConnection) new URL(buildFinalUrl())
                    .openConnection();
        }

        private void setupHttpHeaders(HttpURLConnection connection) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        private void setupHttpOptions(HttpURLConnection connection) {
            HttpSettings o = httpSettings;
            if (o.has(HttpOption.CONNECT_TIMEOUT)) {
                connection.setConnectTimeout(o
                        .getInt(HttpOption.CONNECT_TIMEOUT));
            }
            if (o.has(HttpOption.READ_TIMEOUT)) {
                connection.setReadTimeout(o.getInt(HttpOption.READ_TIMEOUT));
            }
            if (o.has(HttpOption.USER_AGENT)) {
                connection.setRequestProperty("User-Agent",
                        o.getString(HttpOption.USER_AGENT));
            }
            if (o.has(HttpOption.ACCEPT_ENCODING)) {
                connection.setRequestProperty("Accept-Encoding",
                        o.getString(HttpOption.ACCEPT_ENCODING));
            }
            String contentType = o.getString(HttpOption.REQUEST_CONTENT_TYPE);
            if (contentType != null) {
                connection.setRequestProperty("Content-Type", contentType);
            }
        }

        private byte[] readData() throws Exception {
            InputStream in = null;
            try {
                started = true;
                connection = openConnection();
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod(method.name());
                if (method != Method.GET) {
                    connection.setDoOutput(true);
                }
                connection.setDoInput(true);
                setupHttpOptions(connection);
                setupHttpHeaders(connection);
                connection.connect();
                if (isCancelled()) {
                    return null;
                }

                deferred.notify(Arguments.create(Progress.CONNECTED,
                        connection, HttpClient.this));

                if (method != Method.GET) {
                    OutputStream out = new BufferedOutputStream(
                            connection.getOutputStream(), 1024);
                    if (!encodeParamsInUrl) {
                        String paramsData = encodeParams();
                        out.write(getBytes(paramsData, paramCharset));
                        if (data != null) {
                            out.write('&');
                        }
                    }
                    if (data != null) {
                        out.write(data);
                    }
                    if (isCancelled()) {
                        return null;
                    }
                    out.flush();
                    out.close();
                }
                deferred.notify(Arguments.create(Progress.DATA_SENT,
                        connection, HttpClient.this));

                in = connection.getInputStream();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[4 * 1024];
                for (; ; ) {
                    if (isCancelled()) {
                        return null;
                    }
                    int len = in.read(buf);
                    if (len == -1) {
                        break;
                    }
                    baos.write(buf, 0, len);
                    deferred.notify(Arguments.create(Progress.READING,
                            connection, HttpClient.this));
                }

                decompress(connection, baos, buf);

                if (isCancelled()) {
                    return null;
                }
                deferred.notify(Arguments.create(Progress.DONE, connection,
                        HttpClient.this));

                return baos.toByteArray();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        private void decompress(HttpURLConnection connection, ByteArrayOutputStream baos, byte[] buf) throws IOException {
            String contentEncoding = connection.getContentEncoding();
            if ("gzip".equalsIgnoreCase(contentEncoding)) {
                //解压
                deferred.notify(Arguments.create(Progress.DECOMPRESSING,
                        connection, HttpClient.this));
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                GZIPInputStream in = new GZIPInputStream(bais);

                baos.reset();
                for (; ; ) {
                    if (isCancelled()) {
                        return;
                    }
                    int len = in.read(buf);
                    if (len == -1) {
                        break;
                    }
                    baos.write(buf, 0, len);
                }
            }
        }

        @Override
        public void run() {
            byte[] data = readInCache(url);
            try {
                if (data == null) {
                    try {
                        data = readData();
                        addToCache(url, data);
                    } catch (Exception e) {
                        if (isFallbackToCache()) {
                            data = fallback(url);
                        }
                        if (data == null) {
                            throw e;
                        }
                    }
                }

                Arguments arguments = buildArguments(data);

                if (deferred.getState() == State.PENDING) {
                    deferred.resolved(arguments);
                }
            } catch (Exception e) {
                if (isCancelled()) {
                    return;
                }

                if (deferred.getState() == State.PENDING) {
                    Arguments arguments = new Arguments(e,
                            HttpClient.this);
                    deferred.reject(arguments);
                }
            } finally {
                notifyCompleted();
            }
        }

        private boolean isCancelled() {
            return deferred.getState() == State.CANCELED;
        }

        public void notifyCompleted() {
            call(callbackExecutor, HttpOption.COMPLETE_CALLBACK);
        }
    }

}
