package trafficSim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.continuous.SimpleCartesianAdder;
import repast.simphony.space.continuous.StrictBorders;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.util.SimUtilities;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.space.grid.SimpleGridAdder;

public class RoadBuilder implements ContextBuilder<Object> {
	
	private List<GridPoint> exits = new ArrayList<GridPoint>();
	private List<GridPoint> roads = new ArrayList<GridPoint>();
	private List<Integer> usedX = new ArrayList<Integer>();
	private List<Integer> usedY = new ArrayList<Integer>();
	private List<GridPoint> ends = new ArrayList<GridPoint>();
	private List<GridPoint> starts = new ArrayList<GridPoint>();
	

	@Override
	public Context build(Context<Object> context) {
		
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("traffic network", context, true);
		netBuilder.buildNetwork();
		context.setId("trafficSim");
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.StrictBorders(), 50, 50);
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(new WrapAroundBorders(), new SimpleGridAdder<Object>(), true, 50 ,50));
		
		createExits(10, grid, 4);
		/*
		

		for(int i = 0; i < exits.size(); i++) {
			ExitStart exit = new ExitStart(space, grid);
			context.add(exit);
			GridPoint pt = exits.get(i);
			//System.out.println("Generated exit point" + pt);
			grid.moveTo(exit, pt.getX(), pt.getY());
			space.moveTo(exit, pt.getX(), pt.getY());
			context.add(exit);	
		}
			 */	
		while(!exits.isEmpty()) {
			GridPoint start = exits.get(0);
			GridPoint end = exits.get(1);
			generateRoads(start, end, grid, space, context);
			exits.remove(start);
			exits.remove(end);
		}
		filterEndPoints(starts, grid, space);
		filterEndPoints(ends, grid, space);
		/*
		filterEndPointTest(starts);
		filterEndPointTest(ends);
		*/
		
		for(int i = 0; i < starts.size(); i++) {
			GridPoint startCoords = starts.get(i);
			Start start = new Start(grid, space);
			context.add(start);
			grid.moveTo(start, startCoords.getX(), startCoords.getY());
			space.moveTo(start, startCoords.getX(), startCoords.getY());
			context.add(start);
		}
		for(int i = 0; i < ends.size(); i++) {
			GridPoint exitCoords = ends.get(i);
			Exit exit = new Exit(grid, space);
			context.add(exit);
			grid.moveTo(exit, exitCoords.getX(), exitCoords.getY());
			space.moveTo(exit, exitCoords.getX(), exitCoords.getY());
			context.add(exit);
		}
		//generateExits(grid, space, context);
		
		/*
		for(int i = 0; i < 5; i++) {
			//SimUtilities.shuffle(exits, RandomHelper.createUniform());
			GridPoint carSpawn = exits.get(i);
			Car car = new Car(space, grid, carSpawn.getX(), carSpawn.getY(), exits);
			context.add(car);
			space.moveTo(car, carSpawn.getX(), carSpawn.getY());
			grid.moveTo(car, carSpawn.getX(), carSpawn.getY());
			context.add(car);
		}
		*/
		
		System.out.println("EXITS - " + exits);
		
		//System.out.println(availableX);
		//System.out.println(availableY);

		
		//PLACEHOLDER PLACING (NOT FINAL)
		//placeRoads(new GridPoint(2, 10), new GridPoint(38, 42), space, grid, context);
		//placeRoads(new GridPoint(6, 16), new GridPoint(45, 47), space, grid, context);
		//placeRoads(new GridPoint(8, 7), new GridPoint(12, 30), space, grid, context);
		
		
		/*
		for(int i = 0; i < 5; i++) {
			placeRandomRoads(space, grid, context);
		}
		*/
			
		
		
		//Car placement
		//Randomizes the array with exits
		
		//System.out.println("CAR SPAWN: " + carSpawn);
		
		
		
		
		
		
		/*
		for(int i = 3; i < 48; i++) {
			
			Road road = new Road(space, grid);
			Road road2 = new Road(space, grid);
			context.add(road);
			context.add(road2);
			space.moveTo(road, i, 25);
			space.moveTo(road2, 25, i);
			grid.moveTo(road, i, 25);
			grid.moveTo(road2, 25, i);
			context.add(road);
			context.add(road2);
			
			if(i == 3 || i == 47) {
				ExitStart exitStart = new ExitStart(space, grid);
				context.add(exitStart);
				space.moveTo(exitStart, i, 25);
				grid.moveTo(exitStart, i, 25);
				exits.add(new GridPoint(i, 25));
				context.add(exitStart);
			}
			//placeExits(space, grid, context);
			if(i == 3) {
				Car car = new Car(space, grid, 48, 25, exits);
				context.add(car);
				space.moveTo(car, i, 25);
				grid.moveTo(car, i, 25);
				context.add(car);
			}
		}
		*/
		exits = new ArrayList<GridPoint>();
		starts = new ArrayList<GridPoint>();
		ends = new ArrayList<GridPoint>();
		return context;
	}
	
	private void placeRandomRoads(ContinuousSpace<Object> space, Grid<Object> grid, Context<Object> context) {
		int xDim = (int)(grid.getDimensions().getWidth() / 2);
		int yDim = (int)(grid.getDimensions().getHeight() / 2);
		int xRandomNumStart = (int)(Math.random() * xDim);
		int yRandomNumStart = (int)(Math.random() * yDim);
		
		int xRandomNumExit = xDim + (int)(Math.random() * xDim);
		int yRandomNumExit = yDim + (int)(Math.random() * yDim);
		
		int x1 = xRandomNumStart;
		int y1 = yRandomNumStart;
		int x2 = xRandomNumExit;
		int y2 = yRandomNumExit;
		
		
		/*
		int x1 = startPoint.getX();
		int y1 = startPoint.getY();
		int x2 = exitPoint.getX();
		int y2 = exitPoint.getY();
		*/
		
		//Placement of vertical road
		for(int i = x1; i <= x2; i++) {
			Road road = new Road(space, grid);
			context.add(road);
			grid.moveTo(road, i, y1);
			space.moveTo(road, i, y1);
			context.add(road);
		}
		
		//Placement of horizontal road
		for(int i = y1; i <= y2; i++) {
			Road road = new Road(space, grid);
			context.add(road);
			grid.moveTo(road, x2, i);
			space.moveTo(road, x2, i);
			context.add(road);
		}
		
		//Placement of exits on the start and the end of the road
		ExitStart start = new ExitStart(space, grid);
		context.add(start);
		grid.moveTo(start, x1, y1);
		space.moveTo(start, x1, y1);
		context.add(start);
		exits.add(new GridPoint(x1, y1));
		
		ExitStart exit = new ExitStart(space, grid);
		context.add(exit);
		grid.moveTo(exit, x2, y2);
		space.moveTo(exit, x2, y2);
		context.add(exit);
		exits.add(new GridPoint(x2, y2));
	}
	
	private void createExits(int amount ,Grid<Object> grid, int distance) {
		//for(int i = 0; i < amount; i++) {
		while(exits.size() < amount * 2) {
			int randomX = (int)(Math.random() * (grid.getDimensions().getWidth() - 4) + 2);
			int randomY = (int)(Math.random() * (grid.getDimensions().getHeight() - 4) + 2);

			GridPoint pt = new GridPoint(randomX, randomY);
			if(!validateAlt(pt, exits, distance)) {
				exits.add(pt);
			}	
		}
		//System.out.println("Used X - " + usedX);
		//System.out.println("Used Y - " + usedY);
		usedX = new ArrayList<Integer>();
		usedY = new ArrayList<Integer>();
	}
	
	private void generateRoads(GridPoint start, GridPoint end, Grid<Object> grid, ContinuousSpace<Object> space, Context<Object> context) {
		//System.out.println("START - " + start + " END - " + end);
		int xDif = end.getX() - start.getX();
		int yDif = end.getY() - start.getY();
		
		int xStep = xDif < 0 ? -1 : 1;
		int yStep = yDif < 0 ? -1 : 1;
		
		if(xStep == -1) {
			GridPoint startGen = new GridPoint(start.getX() + 1, start.getY() + 1);
			GridPoint endGen = new GridPoint(start.getX() + 1, start.getY());
			starts.add(startGen);
			ends.add(endGen);
		} else {
			GridPoint startGen = new GridPoint(start.getX() - 1, start.getY());
			GridPoint endGen = new GridPoint(start.getX() - 1, start.getY() + 1);
			starts.add(startGen);
			ends.add(endGen);
		}
		
		if(yStep == 1) {
			GridPoint startGen = new GridPoint(end.getX(), end.getY() + 1);
			GridPoint endGen = new GridPoint(end.getX() + 1, end.getY() + 1);
			starts.add(startGen);
			ends.add(endGen);
		} else {
			GridPoint startGen = new GridPoint(end.getX() + 1, end.getY() - 1);
			GridPoint endGen = new GridPoint(end.getX(), end.getY() - 1);
			starts.add(startGen);
			ends.add(endGen);
		}
		for(int i = start.getX(); i != end.getX(); i += xStep) {
			if(!StreamSupport.stream(grid.getObjectsAt(i, start.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
				roads.add(new GridPoint(i, start.getY()));
				Road road = new Road(space, grid);
				context.add(road);
				//Placement of first road
				grid.moveTo(road, i, start.getY());
				space.moveTo(road, i, start.getY());
				//Placement of road next to the one generated before
				context.add(road);
			}
			if(!StreamSupport.stream(grid.getObjectsAt(i, start.getY() + 1).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
				Road road2 = new Road(space, grid);
				context.add(road2);
				grid.moveTo(road2, i, start.getY() + 1);
				space.moveTo(road2, i, start.getY() + 1);
				context.add(road2);
			}
		}
		if(start.getY() == end.getY()) {
			return;
		}
		for(int i = start.getY() + (yStep == -1 ? 1 : 0); i != end.getY() + yStep; i += yStep) {
			if(!StreamSupport.stream(grid.getObjectsAt(end.getX(), i).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
				roads.add(new GridPoint(end.getX(), i));
				Road road = new Road(space, grid);
				context.add(road);
				//Placement of first road
				grid.moveTo(road, end.getX(), i);
				space.moveTo(road, end.getX(), i);
				//Placement of road next to the one generated before
				context.add(road);
			}
			if(!StreamSupport.stream(grid.getObjectsAt(end.getX() + 1, i).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
				Road road2 = new Road(space, grid);
				context.add(road2);
				grid.moveTo(road2, end.getX() + 1, i);
				space.moveTo(road2, end.getX() + 1, i);
				context.add(road2);
			}
		}
		
		
	}
	
	private void filterEndPoints(List<GridPoint> pts, Grid<Object> grid, ContinuousSpace<Object> space) {
		List<GridPoint> localArray = new ArrayList<GridPoint>();
		for (int i = 0; i < pts.size(); i++) {
			GridPoint pt = pts.get(i);
			if(StreamSupport.stream(grid.getObjectsAt(pt.getX(), pt.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
				//System.out.println("removed");
				localArray.add(pt);
			}
		}
		pts.removeAll(localArray);
	}
	
	private void filterEndPointTest(List<GridPoint> pts) {
		for (int i = 0; i < pts.size(); i++) {
			GridPoint pt = pts.get(i);
			if(roads.contains(pt)) {
				pts.remove(pt);
			}
		}
	}
	
	private void generateExits(Grid<Object> grid, ContinuousSpace<Object> space, Context<Object> context) {
		
		for (GridPoint pt : roads) {
			int surroundingRoads = getNeighborRoads(pt, grid);
			if(surroundingRoads == 1) {
				exits.add(new GridPoint(pt.getX(), pt.getY()));
				ExitStart exit = new ExitStart(space, grid);
				context.add(exit);
				grid.moveTo(exit, pt.getX(), pt.getY());
				space.moveTo(exit, pt.getX(), pt.getY());
				context.add(exit);
			}
			if(surroundingRoads > 2) {
				Crossroad cross = new Crossroad(space, grid);
				context.add(cross);
				grid.moveTo(cross, pt.getX(), pt.getY());
				space.moveTo(cross, pt.getX(), pt.getY());
				context.add(cross);
			}
			//System.out.println(getNeighborRoads(pt, grid));
		}
	}
	
	/*
	private boolean validatePoint(int distance, GridPoint pt) {
		boolean isValid = false;
		List<Integer> toRemoveX = new ArrayList<Integer>();
		List<Integer> toRemoveY = new ArrayList<Integer>();
		if(!availableX.contains(pt.getX()) || !availableY.contains(pt.getY())) {
			return false;
		}
		if(availableX.contains(pt.getX()) && availableY.contains(pt.getY())) {
			isValid = true;
			for(int i = -distance; i <= distance; i++) {
				if(i != 0) {
						toRemoveX.add(pt.getX() + i);
						toRemoveY.add(pt.getY() + i);
				}
			}
		} else {
			isValid = false;
		}
		if(isValid) {
			System.out.println("X to remove - " + toRemoveX);
			System.out.println("Y to remove - " + toRemoveY);
			System.out.println("all X points - " + availableX);
			System.out.println("all Y points - " + availableY);
			availableX.removeAll(toRemoveX);
			availableY.removeAll(toRemoveY);
			System.out.println("all X points after remove - " + availableX);
			System.out.println("all Y points after remove - " + availableY);
			return true;
		} else {
			return false;
		}
		
	}
	*/
	
	private boolean validatePoint(int distance, GridPoint pt) {
		if(!usedX.contains(pt.getX()) || !usedY.contains(pt.getY())){
			for(int i = -distance; i <= distance; i++) {
				if(i != 0) {
					if(!usedX.contains(pt.getX() + i)) {
						usedX.add(pt.getX() + i);	
					}
					if(!usedY.contains(pt.getY() + i)) {
						usedY.add(pt.getY() + i);
					}				
				}
				System.out.println("usedX inside validate - " + usedX);
				System.out.println("usedY inside validate - " + usedY);
			}
			return true;
		} else {
			return false;
		}
	}
	
	private boolean validateAlt(GridPoint pt, List<GridPoint> exits, int distance) {
		boolean result = false;
		for (GridPoint exit : exits) {
			if((pt.getX() == exit.getX() && pt.getY() == exit.getY())) {
				return true;
			}
			for(int i = 1; i <= distance; i++) {
				int fx1 = exit.getX() + i;
				int fx2 = exit.getX() - i;
				int fy1 = exit.getY() + i;
				int fy2 = exit.getY() - i;
				if(pt.getX() == fx1 || pt.getX() == fx2 || pt.getY() == fy1 || pt.getY() == fy2) {
					result = true;
					break;
				}
				
			}
		}
		return result;
	}
	
	private int getNeighborRoads(GridPoint pt, Grid<Object> grid) {
		int amount = 0;
		if(StreamSupport.stream(grid.getObjectsAt(pt.getX() + 1, pt.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
			amount++;
		}
		if(StreamSupport.stream(grid.getObjectsAt(pt.getX() - 1, pt.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
			amount++;
		}
		if(StreamSupport.stream(grid.getObjectsAt(pt.getX(), pt.getY() + 1).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
			amount++;
		}
		if(StreamSupport.stream(grid.getObjectsAt(pt.getX(), pt.getY() - 1).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
			amount++;
		}
		return amount;
	}
	
	/*
	private void createAxis(Grid<Object> grid) {
		int min = 2;
		int max = grid.getDimensions().getWidth() - 2;
		//availableX = new ArrayList<Integer>();
		//availableY = new ArrayList<Integer>();
		for(int i = min; i < max; i++) {
			availableX.add(i);
			availableY.add(i);
		}
		SimUtilities.shuffle(availableX, RandomHelper.createUniform());
		SimUtilities.shuffle(availableY, RandomHelper.createUniform());
	}
	*/
}
