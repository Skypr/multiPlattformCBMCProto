package utils;

public class Interrupter extends Thread {
	private long timeOut;
	private Thread toInterrupt;
	private boolean disabled = false;

	public Interrupter(Thread toInterrupt, long timeOut) {
		this.timeOut = timeOut;
		this.toInterrupt = toInterrupt;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(timeOut);
			if (!disabled) {
				toInterrupt.interrupt();
			}
		} catch (InterruptedException e) {
			
		}
	}

	public void disable() {
		disabled = true;
		//in case that the timeOut interrupted right with the finishing of the process
		//we clear the interrupt flag, so that it can't do harm later.
		toInterrupt.isInterrupted();
	}

}
