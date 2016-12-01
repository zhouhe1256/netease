package netease.zh.com.neteasemaven.netease.util;

public abstract class StoppableThread implements Runnable {

	protected volatile boolean stopped = false;
	private Thread thread;

	public synchronized void start() {
		if (thread != null) {
			throw new IllegalStateException("already started");
		}

		stopped = false;
		thread = new Thread(this, getThreadName());
		thread.start();
	}

	protected String getThreadName() {
		return getClass().getSimpleName();
	}

	public boolean isStopped() {
		return stopped;
	}

	public synchronized void stop() {
		if (thread == null) {
			return;
		}

		stopped = true;
		while (thread.isAlive()) {
			thread.interrupt();
			try {
				thread.join(10);
			} catch (InterruptedException e) {
			}
		}
		thread = null;
	}
	
	public void asyncStop() {
		if (stopped) {
			return;
		}
		
		new Thread() {
			public void run() {
				StoppableThread.this.stop();
			}
		}.start();
	}

}
