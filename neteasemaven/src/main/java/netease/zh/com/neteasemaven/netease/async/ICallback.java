package netease.zh.com.neteasemaven.netease.async;

import java.util.ArrayList;
import java.util.List;

public interface ICallback {

	void call(Arguments arguments);

	public class Callbacks implements ICallback {
		private List<ICallback> callbacks = new ArrayList<ICallback>();

		public void add(ICallback callback) {
			callbacks.add(callback);
		}

		@Override
		public void call(Arguments arguments) {
			for (ICallback callback : callbacks) {
				callback.call(arguments);
			}
		}
	}
}
