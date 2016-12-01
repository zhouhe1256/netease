package netease.zh.com.neteasemaven.netease.async;

import org.json.JSONObject;

public abstract class ApiCallback implements ICallback {
    @Override
    public final void call(Arguments arguments) {
        JSONObject json = arguments.get(0);
        boolean success = json.optBoolean("success");
        if (success) {
            onSuccess(json);
        } else {
            onError(json);
        }
    }

    protected abstract void onSuccess(JSONObject json);

    protected void onError(JSONObject json) {
    }
}
