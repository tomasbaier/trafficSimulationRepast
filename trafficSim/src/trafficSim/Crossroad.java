package trafficSim;

import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Crossroad {

	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int dir;
	
	public Crossroad(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		dir = 0;
	}
	
	@ScheduledMethod(start = 1, interval = 15)
	private void changeDir() {
		dir = (dir + 1) % 4;
		System.out.println(dir);
	}
	
	public int getDir() {
		return dir;
	}
	
}
