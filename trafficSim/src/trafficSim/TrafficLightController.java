package trafficSim;

import repast.simphony.engine.schedule.ScheduledMethod;

public class TrafficLight {
	
	private int allowedDir, openInterval, closedInterval, stepCounter;
	
	public TrafficLight(int openInterval, int closedInterval) {
		stepCounter = 0;
		this.openInterval = openInterval;
		this.closedInterval = closedInterval;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void crossroadDirO() {
		stepCounter++;
		allowedDir = (stepCounter / (openInterval + closedInterval)) % 4;
		if(stepCounter % (openInterval + closedInterval) < closedInterval) allowedDir = -1;
	}

	public int getAllowedDir() {
		return allowedDir;
	}
}
