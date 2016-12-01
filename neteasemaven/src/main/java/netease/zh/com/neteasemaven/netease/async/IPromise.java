package netease.zh.com.neteasemaven.netease.async;

public interface IPromise {
	State getState();

	IPromise done(ICallback callback);

	IPromise progress(ICallback callback);

	IPromise fail(ICallback callback);

	IPromise always(ICallback callback);
	
	IPromise cancel(ICallback callback);
	
	void cancel(Arguments arguments);
}
