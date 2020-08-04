package trafficSim;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Entry {
	
	private Grid<Object> grid;
	private ContinuousSpace<Object> space;
	
	public Entry(Grid<Object> grid, ContinuousSpace<Object> space) {
		this.grid = grid;
		this.space = space;
	}

}
