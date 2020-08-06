package trafficSim;

import repast.simphony.space.grid.GridPoint;

public class ExitStart {
	
	private GridPoint exit;
	private GridPoint start;
	
	public ExitStart(GridPoint start, GridPoint exit) {
		this.exit = exit;
		this.start = start;
	}

	public GridPoint getExit() {
		return exit;
	}

	public void setExit(GridPoint exit) {
		this.exit = exit;
	}

	public GridPoint getStart() {
		return start;
	}

	public void setStart(GridPoint start) {
		this.start = start;
	}

}
