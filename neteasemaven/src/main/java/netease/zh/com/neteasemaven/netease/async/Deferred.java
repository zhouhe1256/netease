package netease.zh.com.neteasemaven.netease.async;

public class Deferred implements IPromise {

	private volatile State state = State.PENDING;
    private volatile Arguments argumentsForState;

	private final ICallbackExecutor callbackExecutor;

	private final ICallback.Callbacks doneCallbacks = new ICallback.Callbacks();
	private final ICallback.Callbacks progressCallbacks = new ICallback.Callbacks();
	private final ICallback.Callbacks failCallbacks = new ICallback.Callbacks();
	private final ICallback.Callbacks alwaysCallbacks = new ICallback.Callbacks();
	private final ICallback.Callbacks cancelCallbacks = new ICallback.Callbacks();
	
	private ICancellable cancellable;

	public Deferred() {
		this(new ICallbackExecutor.CurrentThreadExecutor());
	}

	public Deferred(ICallbackExecutor callbackExecutor) {
		this.callbackExecutor = callbackExecutor;
	}
	
	public void setCancellable(ICancellable cancellable) {
		this.cancellable = cancellable;
	}

	public State getState() {
		return state;
	}

	private void execute(Arguments arguments, ICallback... callbacks) {
		for (ICallback callback : callbacks) {
			callbackExecutor.run(callback, arguments);
		}
	}

	public synchronized void notify(Arguments arguments) {
		execute(arguments, progressCallbacks);
	}

	public synchronized void resolved(Arguments arguments) {
		if (state != State.PENDING) {
			throw new IllegalStateException("state must be PENDING");
		}

		state = State.RESOLVED;
        argumentsForState = arguments;
		execute(arguments, doneCallbacks, alwaysCallbacks);
	}

	public synchronized void reject(Arguments arguments) {
		if (state != State.PENDING) {
			throw new IllegalStateException("state must be PENDING");
		}

		state = State.REJECTED;
        argumentsForState = arguments;
		execute(arguments, failCallbacks, alwaysCallbacks);
	}
	
	public synchronized void cancel(Arguments arguments) {
		if (state != State.PENDING) {
			//ignore
			return;
		}
		
		state = State.CANCELED;
        argumentsForState = arguments;
		if (cancellable != null) {
			cancellable.doCancel(arguments);
		}
		execute(arguments, cancelCallbacks, alwaysCallbacks);
	}

	public synchronized Deferred done(ICallback callback) {
		doneCallbacks.add(callback);

        if (state == State.RESOLVED) {
            execute(argumentsForState, callback);
        }
		return this;
	}

	public synchronized Deferred progress(ICallback callback) {
		progressCallbacks.add(callback);
		return this;
	}

	public synchronized Deferred fail(ICallback callback) {
		failCallbacks.add(callback);

        if (state == State.REJECTED) {
            execute(argumentsForState, callback);
        }
		return this;
	}

	public synchronized Deferred always(ICallback callback) {
		alwaysCallbacks.add(callback);

        if (state != State.PENDING) {
            execute(argumentsForState, callback);
        }
		return this;
	}
	
	public synchronized Deferred cancel(ICallback callback) {
		cancelCallbacks.add(callback);

        if (state == State.CANCELED) {
            execute(argumentsForState, callback);
        }
		return this;
	}

}
