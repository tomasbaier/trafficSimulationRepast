package trafficSim;

import java.util.List;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class Road {
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private GridPoint coords;
	private List<GridPoint> surroundingRoads;
	
	public Road(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		this.coords = coords;
		this.surroundingRoads = surroundingRoads;
	}
}
