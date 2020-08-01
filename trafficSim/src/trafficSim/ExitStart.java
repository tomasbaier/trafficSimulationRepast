package trafficSim;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class ExitStart {
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	public ExitStart(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}

}
