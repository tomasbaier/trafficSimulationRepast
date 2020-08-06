package trafficSim;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

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
	private List<GridPoint> instructions;
	private int direction;
	private int crossroadDir;
	private int crossroadDirSave;
	private int pauseTimer;
	private static final GridPoint[] directions = {new GridPoint(0, 1), new GridPoint(1, 0), new GridPoint(0, -1), new GridPoint(-1, 0)};
	
	//TESTING VARIABLES
	private GridPoint previousPoint;
	private GridPoint next;
	private int stepCounter, crossroadInterval, pauseInterval;
	private TrafficLight trafficLight;
	
	private GridPoint test;
	
	public Car(ContinuousSpace<Object> space, Grid<Object> grid, GridPoint startPos, TrafficLight trafficLight) {
		this.space = space;
		this.grid = grid;
		this.trafficLight = trafficLight;
		test = new GridPoint(0, 0);
		crossroadDir = 0;
		pauseTimer = 0;
		exit = null;
		previousPoint = startingPoint;
		instructions = new ArrayList<GridPoint>();
		next = null;
		direction = findInitialDirection(startPos);
		//System.out.println(directions[direction]);
		stepCounter = 0;
		crossroadInterval = 3;
		pauseInterval = 2;
	}
	
	//@ScheduledMethod(start = 1, interval = 1)
	public void drive() {
		
		GridPoint currentPos = grid.getLocation(this);
		GridPoint nextPosition = new GridPoint(currentPos.getX() + directions[direction].getX(), currentPos.getY() + directions[direction].getY());
		
		if(isCar(nextPosition) || isCar(test)) {
			return;
		}
		
		if(!instructions.isEmpty()) {
			if(!isCar(test)) moveByInstruction();
			return;
		}
		
		
		
		if(!isCrossroad(nextPosition, grid)) {
			grid.moveTo(this, nextPosition.getX(), nextPosition.getY());
			space.moveTo(this, nextPosition.getX(), nextPosition.getY());
		} else {
			if(trafficLight.getAllowedDir() == direction) {
				crossroadInstructions(currentPos);
				if(!isCar(test)) moveByInstruction();	
			}
			
		}
		
		if(isExit(currentPos)) {
			removeCar();
		}
		
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void drive2() {
		
		GridPoint currentPos = grid.getLocation(this);
		GridPoint nextPosition = new GridPoint(currentPos.getX() + directions[direction].getX(), currentPos.getY() + directions[direction].getY());
		
		if(isCar(nextPosition)) {
			return;
		}
		
		if(!instructions.isEmpty()) {
			moveByInstruction();
			return;
		}
		
		
		
		if(!isCrossroad(nextPosition, grid)) {
			grid.moveTo(this, nextPosition.getX(), nextPosition.getY());
			space.moveTo(this, nextPosition.getX(), nextPosition.getY());
		} else {
			if(trafficLight.getAllowedDir() == direction) {
				crossroadInstructions(currentPos);
				moveByInstruction();	
			}
			
		}
		
		if(isExit(currentPos)) {
			removeCar();
		}
		
	}

	/*
	@ScheduledMethod(start = 1, interval = 1)
	public void crossroadDirO() {
		stepCounter++;
		crossroadDir = (stepCounter / (crossroadInterval + pauseInterval)) % 4;
		if(stepCounter % (crossroadInterval + pauseInterval) < pauseInterval) crossroadDir = -1;
		//System.out.println("counter: " + stepCounter + "openDir: " + crossroadDir );
	}
	*/
	
	private List<Integer> allowedDirections(GridPoint currentPos) {
		List<Integer> result = new ArrayList<Integer>();
		int dirRight = (direction + 1) % 4;
		int dirStraight = direction; 
		int dirLeft = Math.floorMod(direction - 1, 4);
		GridPoint testRight = new GridPoint(currentPos.getX() + directions[direction].getX() + directions[dirRight].getX(), currentPos.getY() + directions[direction].getY() + directions[dirRight].getY());
		GridPoint testStraight = new GridPoint(currentPos.getX() + directions[direction].getX() * 3, currentPos.getY() + directions[direction].getY() * 3);
		GridPoint testLeft = new GridPoint(currentPos.getX() + directions[direction].getX() * 2 + directions[dirLeft].getX() * 2, currentPos.getY() + directions[direction].getY() * 2 + directions[dirLeft].getY() * 2);
		//System.out.println("Car position - " + grid.getLocation(this) + "testRight - " + testRight + " testStraight - " + testStraight + "testLeft" + testLeft);
		if(isRoad(testLeft)) result.add(dirLeft);
		if(isRoad(testStraight)) result.add(dirStraight);
		if(isRoad(testRight)) result.add(dirRight);
		return result;
	}
	
	private void crossroadInstructions(GridPoint currentPos) {
		List<Integer> allowedDirections = allowedDirections(currentPos);
		SimUtilities.shuffle(allowedDirections, RandomHelper.createUniform());
		int newDirection = allowedDirections.get(0);
		if(newDirection - direction == -1 || newDirection - direction == 3) {
			//Left
			instructions.add(directions[direction]);
			instructions.add(directions[direction]);
			instructions.add(directions[newDirection]);
			instructions.add(directions[newDirection]);
			test = new GridPoint(currentPos.getX(), currentPos.getY());
		} else if(newDirection == direction) {
			//Straight
			instructions.add(directions[newDirection]);
			instructions.add(directions[newDirection]);
			instructions.add(directions[newDirection]);
		} else if(newDirection - direction == 1 || newDirection - direction == -3) {
			//Right
			instructions.add(directions[direction]);
			instructions.add(directions[newDirection]);
		}
		for (GridPoint direction : instructions) {
			test = new GridPoint(test.getX() + direction.getX(), test.getY() + direction.getY());
			
		}
		direction = newDirection;
	}
	
	private void moveByInstruction() {
		test = new GridPoint(0,0);
		GridPoint dir = instructions.get(0);
		
		GridPoint currentPos = grid.getLocation(this);
		GridPoint nextPosition = new GridPoint(currentPos.getX() + dir.getX(), currentPos.getY() + dir.getY());

		instructions.remove(0);
		grid.moveTo(this, nextPosition.getX(), nextPosition.getY());
		space.moveTo(this, nextPosition.getX(), nextPosition.getY());		
	}
	
	private boolean isCar(GridPoint nextPos) {
		GridCellNgh<Car> nghCreator = new GridCellNgh<Car>(grid, nextPos, Car.class, 0, 0);
		List<GridCell<Car>> gridCells = nghCreator.getNeighborhood(true);
		for (GridCell<Car> gridCell : gridCells) {
			if(gridCell.size() > 0) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isExit(GridPoint currentPos) {
		GridCellNgh<Exit> nghCreator = new GridCellNgh<Exit>(grid, currentPos, Exit.class, 0, 0);
		List<GridCell<Exit>> gridCells = nghCreator.getNeighborhood(true);
		for (GridCell<Exit> gridCell : gridCells) {
			if(gridCell.size() > 0) {
				//System.out.println("POINT -" + gridCell.getPoint() + " SIZE - " + gridCell.size());
				return true;
			}
		}
		return false;
	}
	
	private boolean isRoad(GridPoint pt) {
		boolean result = false;
		GridCellNgh<Road> nghCreator = new GridCellNgh<Road>(grid, pt, Road.class, 0, 0);
		List<GridCell<Road>> gridCells = nghCreator.getNeighborhood(true);
		for (GridCell<Road> gridCell : gridCells) {
			//GridPoint currentPos = grid.getLocation(this);
			if(gridCell.size() > 0) {
				//System.out.println("POINT -" + gridCell.getPoint() + " SIZE - " + gridCell.size());
				result = true;
				//System.out.println(gridCell.getPoint());
			}
		}
		return result;
	}
	
	private GridPoint findInitialPoint() {
		GridPoint pt = null;
		GridCellNgh<Road> nghCreator = new GridCellNgh<Road>(grid, grid.getLocation(this), Road.class, 1, 1);
		List<GridCell<Road>> gridCells = nghCreator.getNeighborhood(true);
		for (GridCell<Road> gridCell : gridCells) {
			if(gridCell.size() == 1 && (gridCell.getPoint().getX() == grid.getLocation(this).getX() || gridCell.getPoint().getY() == grid.getLocation(this).getY())) {
				 pt = gridCell.getPoint();
			}
		}
		return pt;
	}
	
	private boolean nextPosCrossroad(Grid<Object> grid) {
		boolean result = false;
		GridPoint nextPos = new GridPoint(grid.getLocation(this).getX() + directions[direction].getX(), grid.getLocation(this).getY() + directions[direction].getY());
		if(StreamSupport.stream(grid.getObjectsAt(nextPos.getX(), nextPos.getY()).spliterator(), false).anyMatch(Crossroad -> Crossroad.equals(Crossroad))) {
			result = true;
		}
		System.out.println(result);
		return result;
	}
	
	private boolean isCrossroad(GridPoint pt, Grid<Object> grid) {
		boolean result = false;
		GridCellNgh<Crossroad> nghCreator = new GridCellNgh<Crossroad>(grid, pt, Crossroad.class, 0, 0);
		List<GridCell<Crossroad>> gridCells = nghCreator.getNeighborhood(true);
		for (GridCell<Crossroad> gridCell : gridCells) {
			GridPoint currentPos = grid.getLocation(this);
			if(gridCell.getPoint().getX() == currentPos.getX() || gridCell.getPoint().getY() == currentPos.getY()) {
				if(gridCell.size() == 1) {
					//System.out.println(gridCell.getPoint());
					result = true;
				}
			}
		}
		return result;
	}
	
	private int findInitialDirection(GridPoint startPos) {
		GridPoint pt = null;
		GridCellNgh<Road> nghCreator = new GridCellNgh<Road>(grid, startPos, Road.class, 1, 1);
		List<GridCell<Road>> gridCells = nghCreator.getNeighborhood(true);
		for (GridCell<Road> gridCell : gridCells) {
			if(gridCell.size() == 1 && (gridCell.getPoint().getX() == startPos.getX() || gridCell.getPoint().getY() == startPos.getY())) {
				 pt = gridCell.getPoint();
			}
		}
		GridPoint dir = new GridPoint(pt.getX() - startPos.getX(), pt.getY() - startPos.getY());
		int result = 0;
		if(dir.getY() == 1) {
			result = 0;
		} else if(dir.getX() == 1) {
			result = 1;
		} else if(dir.getY() == -1) {
			result = 2;
		} else if(dir.getX() == -1) {
			result = 3;
		}
		return result;
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
		boolean isTurn = true ;
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
			//grid.moveTo(this, (int)myPoint.getX(), (int)myPoint.getY());
		//} 
			
		if(pt.equals(exit)) {
			removeCar();
		}
	}
	
	private double calculateAngle(GridPoint pt) {
		NdPoint myPoint = space.getLocation(this);
		NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
		return SpatialMath.calcAngleFor2DMovement(space, myPoint, otherPoint);
		
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
