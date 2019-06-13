package model;

import java.util.TimerTask;

public class AlarmSoundTimerTask extends TimerTask {
	Thread thread;

	public AlarmSoundTimerTask(Thread thread) {
		this.thread = thread;
	}

	@Override
	public void run() {
		thread.start();		
	}

}
