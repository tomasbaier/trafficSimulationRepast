package trafficSim;

import repast.simphony.space.continuous.ContinuousSpace;

import repast.simphony.space.grid.Grid;

public class Start {
	
	private Grid<Object> grid;
	private ContinuousSpace<Object> space;
	
	public Start(Grid<Object> grid, ContinuousSpace<Object> space) {
		this.grid = grid;
		this.space = space;
	}

}
