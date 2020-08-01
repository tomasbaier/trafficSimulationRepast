package trafficSim;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Crossroad {

	ContinuousSpace<Object> space;
	Grid<Object> grid;
	
	public Crossroad(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
}
