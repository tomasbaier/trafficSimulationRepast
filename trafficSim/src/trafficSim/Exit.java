package trafficSim;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Exit {
	
	private Grid<Object> grid;
	private ContinuousSpace<Object> space;
	
	public Exit(Grid<Object> grid, ContinuousSpace<Object> space) {
		this.grid = grid;
		this.space = space;
	}

}
