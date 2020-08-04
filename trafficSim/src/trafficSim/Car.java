package trafficSim;

import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.query.space.grid.GridWithin;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

public class Car {
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private GridPoint startingPoint;
	private GridPoint exit;
	private List<GridPoint> exits;
	
	//TESTING VARIABLES
	private GridPoint previousPoint;
	private GridPoint next;
	
	public Car(ContinuousSpace<Object> space, Grid<Object> grid, int x, int y, List<GridPoint> exits) {
		this.space = space;
		this.grid = grid;
		this.exits = exits;
		startingPoint = new GridPoint(x, y);
		exit = null;
		previousPoint = startingPoint;
		next = null;
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void drive() {
		/*
		GridPoint currentLocation = grid.getLocation(this);
		GridCellNgh<Road> nghCreator = new GridCellNgh<Road>(grid, currentLocation, Road.class, 1, 1);
		List<GridCell<Road>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle ( gridCells , RandomHelper.getUniform ());
		*/
		//System.out.println("GridCellSize: " + gridCells.size());
		
		SimUtilities.shuffle(exits, RandomHelper.createUniform());
		exit = exits.get(0);
		if(exit.equals(grid.getLocation(this))) {
			exit = exits.get(1);
		}
		/*
		for (GridPoint currentExit : exits) {
			if(!currentExit.equals(grid.getLocation(this))) {
				exit = currentExit;
			}
		}
		*/
		
		//driveTo(exit);
		//randomDriveTo();
		customRandomDrive();
	}
	
	public void randomDriveTo() {
		GridPoint pt = grid.getLocation(this);
		
		GridCellNgh<Road> nghCreator = new GridCellNgh<Road>(grid, pt, Road.class, 1, 1);
		List<GridCell<Road>> gridCells = nghCreator.getNeighborhood(false);
		SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
		
		List<Object> objects = new ArrayList<Object>();
		GridPoint goal = null;
		System.out.println("______________________________________________");
		for (GridCell<Road> gridCell : gridCells) {
			//System.out.println("________________________________");
			next = new GridPoint(gridCell.getPoint().getX(), gridCell.getPoint().getY());
			//System.out.println("SIZE: " + gridCell.size());
			for(Object obj : grid.getObjectsAt(pt.getX(), pt.getY())) {		
				//objects.add(obj);
				//System.out.println("Coords - X: " + gridCell.getPoint().getX() + " Y: " + gridCell.getPoint().getY() );
				//System.out.println("INSIDE RANDOM DRIVE - " + objects.size() + " ," + objects.get(0));
				if(obj instanceof Road) {
					//System.out.println("Previous Point - X: " + previousPoint.getX() + " Y: " + previousPoint.getY());
					//System.out.println("Next Point - X: " + next.getX() + " Y: " + next.getY());
					if(!previousPoint.equals(next) && (pt.getX() == next.getX() || pt.getY() == next.getY())) {
						System.out.println("Current Point - X: " + pt);
						System.out.println("Previous Point - X: " + previousPoint);
						System.out.println("Next Point - X: " + next);
						System.out.println("_____________________________________");
						previousPoint = next;
						goal = next;
						//System.out.println("TEST - X: " + previousPoint.getX() + " Y: " + previousPoint.getY());
						//System.out.println("NEXT - X: " + next.getX() + " Y: " + next.getY());
						//System.out.println("NEXT - X: " + next.getX() + " Y: " + next.getY());				
					}
				}
			}
		}
		driveTo(goal);
		}
	
	public void customRandomDrive() {
		GridPoint pt = grid.getLocation(this);
		GridPoint goal = null;
		List<GridPoint> roads = getNeighborRoads(pt);
		int amount = roads.size();
		//System.out.println("Roads size" + amount);
		//System.out.println("Current point: " + pt + "Previous point: " + previousPoint);
		/*
		for (GridPoint gridPoint : roads) {
			System.out.println("Coords before shuffle: " + gridPoint);
		}
		*/
		SimUtilities.shuffle(roads, RandomHelper.createUniform());
		/*for (GridPoint gridPoint : roads) {
			System.out.println("Coords after shuffle: " + gridPoint);
		}
		*/
		//System.out.println(roads.size());
		for(int i = 0; i < roads.size(); i++) {
			GridPoint point = roads.get(i);
			if(amount == 1) {
				previousPoint = pt;
			}
			if(!point.equals(previousPoint)) {
				goal = roads.get(i);
				previousPoint = grid.getLocation(this);				
				//System.out.println("INSIDE");
				break;
			}
		}
		
		/*
		for(int i = 0; i < roads.size(); i++) {
			int random = (int)(Math.random() * roads.size());
			System.out.println("Selection: " + random);
			GridPoint randomPoint = roads.get(random);
			if(!randomPoint.equals(previousPoint)) {
				goal = roads.get(random);
				previousPoint = grid.getLocation(this);
				break;
			}
		}
		*/
		//System.out.println("GOAL: " + goal);
		//System.out.println("_____________________________");
		driveTo(goal);
	}
	
	private void driveTest() {
		
	}
	
	private List<GridPoint> getNeighborRoads(GridPoint pt){
		List<GridPoint> roads = new ArrayList<GridPoint>();
		for (Object obj : grid.getObjectsAt(pt.getX() + 1, pt.getY())) {
			if(obj instanceof Road) {
				roads.add(new GridPoint(pt.getX() + 1, pt.getY()));
			}
		}
		for (Object obj : grid.getObjectsAt(pt.getX() - 1, pt.getY())) {
			if(obj instanceof Road) {
				roads.add(new GridPoint(pt.getX() - 1, pt.getY()));
			}
		}
		for (Object obj : grid.getObjectsAt(pt.getX(), pt.getY() + 1)) {
			if(obj instanceof Road) {
				roads.add(new GridPoint(pt.getX(), pt.getY() + 1));
			}
		}
		for (Object obj : grid.getObjectsAt(pt.getX(), pt.getY() - 1)) {
			if(obj instanceof Road) {
				roads.add(new GridPoint(pt.getX(), pt.getY() - 1));
			}
		}
		return roads;
	}
	
	private boolean checkIfTurn(GridPoint currentPoint, GridPoint checkingPoint) {
		boolean isTurn = false;
		int x = currentPoint.getX() - checkingPoint.getX();
 		int y = currentPoint.getX() - checkingPoint.getX();
 		GridPoint pointToCheck = new GridPoint(2 * x, 2 * y);
 		for (Object obj : grid.getObjectsAt(pointToCheck.getX(), pointToCheck.getY())) {
			if(obj instanceof Road) {
				isTurn = true;
			} else {
				isTurn = false;
			}
		}
 		return isTurn;
	}
	
	public void driveTo(GridPoint pt) {
		//if(!pt.equals(grid.getLocation(this))) {
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
			space.moveByVector(this, 1, angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int)myPoint.getX(), (int)myPoint.getY());
		//} 
		if(pt.equals(exit)) {
			removeCar();
		}
	}
	
	public void removeCar() {
		repast.simphony.context.Context<Object> context = ContextUtils.getContext(this);
		context.remove(this);
		/*
		GridPoint pt = grid.getLocation(this);
		List<Object> cars = new ArrayList<Object>();
		for(Object obj : grid.getObjectsAt(pt.getX(), pt.getY())) {
			if(obj instanceof Car) {
				cars.add(obj);
			}
		}
		if()
		*/
	}

}
