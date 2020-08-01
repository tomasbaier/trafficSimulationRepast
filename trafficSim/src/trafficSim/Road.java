package trafficSim;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Road {
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	public Road(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
}
