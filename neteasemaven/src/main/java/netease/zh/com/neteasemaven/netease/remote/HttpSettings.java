package netease.zh.com.neteasemaven.netease.remote;



import java.util.HashMap;
import java.util.Map;

import netease.zh.com.neteasemaven.netease.async.ICallback;


public class HttpSettings {

    private final Map<HttpOption, Object> options = new HashMap<HttpOption, Object>();

    public HttpSettings() {
    }

    public HttpSettings(HttpSettings value) {
        options.putAll(value.options);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(HttpOption name) {
        return get(name, null);
    }

    public <T> T get(HttpOption name, T defaultValue) {
        T value = (T) options.get(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public void set(HttpOption name, Object value) {
        options.put(name, value);
    }

    public boolean has(HttpOption name) {
        return options.containsKey(name);
    }

    public int getInt(HttpOption name) {
        Object value = options.get(name);
        if (value == null) {
            throw new IllegalArgumentException("no option for " + name);
        }

        return ((Number) value).intValue();
    }

    public void removeAllCallbacks() {
        removeCallbacks(HttpOption.ALWAYS_CALLBACK);
        removeCallbacks(HttpOption.START_CALLBACK);
        removeCallbacks(HttpOption.DONE_CALLBACK);
        removeCallbacks(HttpOption.FAIL_CALLBACK);
        removeCallbacks(HttpOption.PROGRESS_CALLBACK);
        removeCallbacks(HttpOption.COMPLETE_CALLBACK);
    }

    public void removeCallbacks(HttpOption callbackName) {
        options.remove(callbackName);
    }

    public void addCallback(HttpOption callbackName, ICallback callback) {
        ICallback.Callbacks callbacks = (ICallback.Callbacks) options.get(callbackName);
        if (callbacks == null) {
            callbacks = new ICallback.Callbacks();
            options.put(callbackName, callbacks);
        }
        callbacks.add(callback);
    }

    public ICallback getCallback(HttpOption name) {
        return get(name);
    }

    public String getString(HttpOption name) {
        return get(name);
    }

    private Map<String, Object> ensureParams() {
        Map<String, Object> params = get(HttpOption.PARAMS);
        if (params == null) {
            params = new HashMap<String, Object>();
            set(HttpOption.PARAMS, params);
        }
        return params;
    }

    public void param(String name, Object value) {
        ensureParams().put(name, value);
    }

    public Map<String, Object> getParams() {
        return ensureParams();
    }

}
