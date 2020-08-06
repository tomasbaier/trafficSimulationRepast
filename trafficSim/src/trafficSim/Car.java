package trafficSim;

import java.util.ArrayList;
import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

public class Car {
	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private List<GridPoint> instructions;
	private int direction;
	private static final GridPoint[] directions = {new GridPoint(0, 1), new GridPoint(1, 0), new GridPoint(0, -1), new GridPoint(-1, 0)};
	private TrafficLight trafficLight;
	
	private GridPoint test;
	
	public Car(ContinuousSpace<Object> space, Grid<Object> grid, GridPoint startPos, TrafficLight trafficLight) {
		this.space = space;
		this.grid = grid;
		this.trafficLight = trafficLight;
		test = new GridPoint(0, 0);
		instructions = new ArrayList<GridPoint>();
		direction = findInitialDirection(startPos);
		//System.out.println(directions[direction]);
	}
	
	//Metoda pohybu auta
	@ScheduledMethod(start = 1, interval = 1)
	public void drive() {
		
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
			if(gridCell.size() > 0) {
				result = true;
			}
		}
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
	
	public void removeCar() {
		repast.simphony.context.Context<Object> context = ContextUtils.getContext(this);
		context.remove(this);
	}

}
