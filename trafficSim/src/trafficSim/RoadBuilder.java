package trafficSim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
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
	private List<GridPoint> crossroads = new ArrayList<GridPoint>();
	private List<ExitStart> exitStarts = new ArrayList<ExitStart>();
	

	@Override
	public Context build(Context<Object> context) {
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		
		int gridDims = params.getInteger("grid_dimensions");
		
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("traffic network", context, true);
		netBuilder.buildNetwork();
		context.setId("trafficSim");
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.StrictBorders(), gridDims, gridDims);
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(new WrapAroundBorders(), new SimpleGridAdder<Object>(), true, gridDims ,gridDims));
		
		
		int exitCount = params.getInteger("exit_count");
		
		//createExits(20, grid, 4);
		createEntries(exitCount, grid, 6);
		//System.out.println(exits);
		
		/*
		for(int i = 0; i < exits.size(); i++) {
			Entry entry = new Entry(grid,space);
			context.add(entry);
			GridPoint pt = exits.get(i);
			//System.out.println("Generated exit point" + pt);
			grid.moveTo(entry, pt.getX(), pt.getY());
			space.moveTo(entry, pt.getX(), pt.getY());
			context.add(entry);	
		}
*/
		while(!exits.isEmpty()) {
			GridPoint start = exits.get(0);
			GridPoint end = exits.get(1);
			System.out.println("START - " + start + " END - " + end);
			generateRoadsAlt(start, end, grid, space, context);
			generateExitStarts(start, end, grid, space, context);
			exits.remove(start);
			exits.remove(end);
		}
		
		/*
		while(!exits.isEmpty()) {
			GridPoint start = exits.get(0);
			GridPoint end = exits.get(1);
			generateRoads(start, end, grid, space, context);
			exits.remove(start);
			exits.remove(end);
		}
		*/
		//filterEndPoints(starts, grid, space);
		//filterEndPoints(ends, grid, space);
		filterEndPointTest(exitStarts, grid, space);
		
		
		/*
		filterEndPointTest(starts);
		filterEndPointTest(ends);
		*/
		
		findCrossdroads(grid, space, context);
		
		//removeNotSuitable(grid);
		
		removeUnsuitableTomTest(exitStarts ,grid);
		
		//System.out.println(getNeighborhood(new GridPoint(10, 10)));
		
		for(int i = 0; i < exitStarts.size(); i++) {
			ExitStart exitStart = exitStarts.get(i);
			GridPoint startCoords = exitStart.getStart();
			GridPoint exitCoords = exitStart.getExit();
			Start start = new Start(grid, space);
			Exit exit = new Exit(grid, space);
			context.add(start);
			context.add(exit);
			grid.moveTo(start, startCoords.getX(), startCoords.getY());
			space.moveTo(start, startCoords.getX(), startCoords.getY());
			grid.moveTo(exit, exitCoords.getX(), exitCoords.getY());
			space.moveTo(exit, exitCoords.getX(), exitCoords.getY());
		}
		
		/*
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
		*/
		
		
		//System.out.println(crossroads);
		//generateExits(grid, space, context);
		
		
		int carCount = params.getInteger("car_count");
		
		int openInterval = params.getInteger("open_interval");
		int closedInterval = params.getInteger("closed_interval");
		TrafficLight trafficLight = new TrafficLight(openInterval, closedInterval);
		context.add(trafficLight);
		
		for(int i = 0; i < carCount; i++) {
			SimUtilities.shuffle(exitStarts, RandomHelper.createUniform());
			GridPoint carSpawn = exitStarts.get(0).getStart();
			Car car = new Car(space, grid, carSpawn, trafficLight);
			context.add(car);
			space.moveTo(car, carSpawn.getX(), carSpawn.getY());
			grid.moveTo(car, carSpawn.getX(), carSpawn.getY());
			context.add(car);
		}
		
		
		//System.out.println("EXITS - " + exits);
		
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
		roads = new ArrayList<GridPoint>();
		exitStarts = new ArrayList<ExitStart>();
		exits = new ArrayList<GridPoint>();
		starts = new ArrayList<GridPoint>();
		ends = new ArrayList<GridPoint>();
		return context;
	}
	
	private void createEntries(int amount, Grid<Object> grid, int distance) {
		GridPoint pt1 = null;
		GridPoint pt2 = null;
		while(exits.size() < amount * 2) {
			if(Math.random() < 0.5) {
				int randomY1 = (int)(Math.random() * (grid.getDimensions().getHeight() / 2) + 2);
				//int randomY1 = (int)(Math.random() * ((grid.getDimensions().getHeight() - plus) / 2) + plus);
				int testerX = (int)(Math.random() * grid.getDimensions().getWidth());
				int subtractor = testerX < (grid.getDimensions().getWidth() / 2) ? 2 : -2;
				int randomX = testerX + subtractor;
				//int randomX = (int)(Math.random() * (grid.getDimensions().getWidth() - plus)) + plus;
				int randomY2 = grid.getDimensions().getHeight() / 2 + ((int)(Math.random() * (grid.getDimensions().getHeight()) / 2) - 2);
				//int randomY2 = ((grid.getDimensions().getHeight() / 2)  - plus) + ((int)(Math.random() * (grid.getDimensions().getHeight() - plus) / 2) - plus);
				pt1 = new GridPoint(randomX, randomY1);
				pt2 = new GridPoint(randomX, randomY2);
			} else {
				int randomX1 = (int)(Math.random() * (grid.getDimensions().getWidth()) / 2 + 2);
				//int randomX1 = (int)(Math.random() * ((grid.getDimensions().getWidth() - plus) / 2) + plus);
				int testerY = (int)(Math.random() * grid.getDimensions().getHeight());
				int subtractor = testerY < (grid.getDimensions().getHeight() / 2) ? 2 : -2;
				int randomY = testerY + subtractor;
				//int randomY = (int)(Math.random() * (grid.getDimensions().getHeight() - plus) + plus);
				int randomX2 = grid.getDimensions().getWidth() / 2 + (int)(Math.random() * (grid.getDimensions().getWidth() / 2) - 2);
				pt1 = new GridPoint(randomX1, randomY);
				pt2 = new GridPoint(randomX2, randomY);
			}
			
			if(!validateAlt(pt1, exits, distance) && !validateAlt(pt2, exits, distance)) {
				exits.add(pt1);
				exits.add(pt2);
			}
		}
	}
	
	private void generateExitStarts(GridPoint startPT, GridPoint endPT, Grid<Object> grid, ContinuousSpace<Object> space, Context<Object> context) {
		if(startPT.getX() == endPT.getX()) {
			GridPoint start1 = new GridPoint(startPT.getX() + 1, startPT.getY() - 1);
			GridPoint exit1 = new GridPoint(startPT.getX(), startPT.getY() - 1);
			
			starts.add(start1);
			ends.add(exit1);
			
			exitStarts.add(new ExitStart(start1, exit1));
			
			GridPoint start2 = new GridPoint(endPT.getX(), endPT.getY() + 1);
			GridPoint exit2 = new GridPoint(endPT.getX() + 1, endPT.getY() + 1);
			
			starts.add(start2);
			ends.add(start2);		
			
			exitStarts.add(new ExitStart(start2, exit2));
		}
		if(startPT.getY() == endPT.getY()) {
			GridPoint start1 = new GridPoint(startPT.getX() - 1, startPT.getY());
			GridPoint exit1 = new GridPoint(startPT.getX() - 1, startPT.getY() + 1);
			
			starts.add(start1);
			ends.add(exit1);
			
			exitStarts.add(new ExitStart(start1, exit1));
			
			GridPoint start2 = new GridPoint(endPT.getX() + 1, endPT.getY() + 1);
			GridPoint exit2 = new GridPoint(endPT.getX() + 1, endPT.getY());
			
			starts.add(start2);
			ends.add(exit2);
			
			exitStarts.add(new ExitStart(start2, exit2));
		}
	}
	
	private void createEntriesY(int amount, Grid<Object> grid, int distance) {
		while(exits.size() < amount * 2) {
			System.out.println("TEST");
			int randomY1 = (int)(Math.random() * ((grid.getDimensions().getHeight()) / 2));
			int randomX = (int)(Math.random() * ((grid.getDimensions().getWidth()) / 2));
			int randomY2 = grid.getDimensions().getHeight() + (int)(Math.random() * ((grid.getDimensions().getHeight()) / 2));
			GridPoint pt1 = new GridPoint(randomX, randomY1);
			GridPoint pt2 = new GridPoint(randomX, randomY2);
			if(!validateAlt(pt1, exits, distance) && !validateAlt(pt2, exits, distance)) {
				exits.add(pt1);
				exits.add(pt2);
			}
		}
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
	
	private void generateRoadsAlt(GridPoint start, GridPoint end, Grid<Object> grid, ContinuousSpace<Object> space, Context<Object> context) {
		if(start.getX() == end.getX()) {
			//System.out.println("start.getX() == end.getX()");
			for(int i = start.getY(); i != end.getY() + 1; i ++) {
				roads.add(new GridPoint(start.getX(), i));
				roads.add(new GridPoint(start.getX(), i + 1));
				Road road = new Road(space, grid);
				Road road2 = new Road(space, grid);
				//System.out.println("ROAD - " + road);
				context.add(road);
				context.add(road2);
				//Placement of first road
				grid.moveTo(road, start.getX(), i);
				space.moveTo(road, start.getX(), i);
				
				grid.moveTo(road2, start.getX() + 1, i);
				space.moveTo(road2, start.getX() + 1, i);
				//Placement of road next to the one generated before
				context.add(road);
				context.add(road2);
			}
		}
		if(start.getY() == end.getY()) {
			//System.out.println("start.getY() == end.getY()");
			for(int i = start.getX(); i != end.getX() + 1; i++) {
				roads.add(new GridPoint(i, start.getY()));
				roads.add(new GridPoint(i + 1, start.getY()));
				
				Road road = new Road(space, grid);
				Road road2 = new Road(space, grid);
				
				context.add(road);
				context.add(road2);
				
				grid.moveTo(road, i, start.getY());
				space.moveTo(road, i, start.getY());
				
				grid.moveTo(road2, i , start.getY() + 1);
				space.moveTo(road2,i , start.getY() + 1);
				
				context.add(road);
				context.add(road2);
			}
		}
	}
	
	private void generateRoads(GridPoint start, GridPoint end, Grid<Object> grid, ContinuousSpace<Object> space, Context<Object> context) {
		//System.out.println("START - " + start + " END - " + end);
		int xDif = end.getX() - start.getX();
		int yDif = end.getY() - start.getY();
		
		int xStep = xDif < 0 ? -1 : 1;
		int yStep = yDif < 0 ? -1 : 1;
		
		System.out.println("START - " + start + " END - " + end);
		
		if(start.getX() == end.getX()) {
			if(start.getX() < end.getX()) {
				System.out.println("start.getX() < end.getX()");
				GridPoint startGen = new GridPoint(start.getX(), start.getY() + 1);
				GridPoint endGen = new GridPoint(start.getX(), start.getY());
				starts.add(startGen);
				ends.add(endGen);
				exitStarts.add(new ExitStart(startGen, endGen));
			} else {
				System.out.println("start.getX() > end.getX()");
				GridPoint startGen = new GridPoint(start.getX() + 1, start.getY());
				GridPoint endGen = new GridPoint(start.getX(), start.getY());
				starts.add(startGen);
				ends.add(endGen);
				exitStarts.add(new ExitStart(startGen, endGen));
			}
		} else {
			System.out.println("NORMAL START");
			if(xStep == -1) {
				GridPoint startGen = new GridPoint(start.getX() + 1, start.getY() + 1);
				GridPoint endGen = new GridPoint(start.getX() + 1, start.getY());
				starts.add(startGen);
				ends.add(endGen);
				exitStarts.add(new ExitStart(startGen, endGen));
			} else {
				GridPoint startGen = new GridPoint(start.getX() - 1, start.getY());
				GridPoint endGen = new GridPoint(start.getX() - 1, start.getY() + 1);
				starts.add(startGen);
				ends.add(endGen);
				exitStarts.add(new ExitStart(startGen, endGen));
			}
		}
		

		
		if(start.getY() == end.getY()) {
			if(start.getY() < end.getY()) {
				System.out.println("start.getY() < end.getY()");
				GridPoint startGen = new GridPoint(end.getX(), end.getY() + 1);
				GridPoint endGen = new GridPoint(end.getX(), end.getY());
				starts.add(startGen);
				ends.add(endGen);
				exitStarts.add(new ExitStart(startGen, endGen));
			} else {
				System.out.println("start.getY() > end.getY()");
				GridPoint startGen = new GridPoint(end.getX(), end.getY() + 1);
				GridPoint endGen = new GridPoint(end.getX(), end.getY());
				starts.add(startGen);
				ends.add(endGen);
				exitStarts.add(new ExitStart(startGen, endGen));
			}
			
		} else {
			System.out.println("NORMAL END");
			if(yStep == 1) {
				GridPoint startGen = new GridPoint(end.getX(), end.getY() + 1);
				GridPoint endGen = new GridPoint(end.getX() + 1, end.getY() + 1);
				starts.add(startGen);
				ends.add(endGen);
				exitStarts.add(new ExitStart(startGen, endGen));
			} else {
				GridPoint startGen = new GridPoint(end.getX() + 1, end.getY() - 1);
				GridPoint endGen = new GridPoint(end.getX(), end.getY() - 1);
				starts.add(startGen);
				ends.add(endGen);
				exitStarts.add(new ExitStart(startGen, endGen));
			}
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
	
	private void filterEndPointTest(List<ExitStart> pts, Grid<Object> grid, ContinuousSpace<Object> space) {
		List<ExitStart> localArray = new ArrayList<ExitStart>();
		for (int i = 0; i < pts.size(); i++) {
			GridPoint pt = pts.get(i).getStart();
			if(StreamSupport.stream(grid.getObjectsAt(pt.getX(), pt.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
				//System.out.println("removed");
				localArray.add(pts.get(i));
			}
		}
		pts.removeAll(localArray);
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
	
	
	private void findCrossdroads(Grid<Object> grid, ContinuousSpace<Object> space, Context<Object> context) {
		for(int x = 2; x < grid.getDimensions().getWidth() - 2; x++) {
			for(int y = 2; y < grid.getDimensions().getHeight() - 2; y++) {
				int amount = 0;
				GridPoint pt1 = new GridPoint(x - 1, y);
				GridPoint pt2 = new GridPoint(x, y - 1);
				GridPoint pt3 = new GridPoint(x + 2, y);
				GridPoint pt4 = new GridPoint(x, y + 2);
				if(StreamSupport.stream(grid.getObjectsAt(pt1.getX(), pt1.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
					if(StreamSupport.stream(grid.getObjectsAt(pt1.getX(), pt1.getY() + 1).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
						amount++;
					}
				}
				if(StreamSupport.stream(grid.getObjectsAt(pt2.getX(), pt2.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
					if(StreamSupport.stream(grid.getObjectsAt(pt2.getX() + 1, pt2.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
						amount++;
					}
				}
				if(StreamSupport.stream(grid.getObjectsAt(pt3.getX(), pt3.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
					if(StreamSupport.stream(grid.getObjectsAt(pt3.getX(), pt3.getY() + 1).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
						amount++;
					}
				}
				if(StreamSupport.stream(grid.getObjectsAt(pt4.getX(), pt4.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
					if(StreamSupport.stream(grid.getObjectsAt(pt4.getX() + 1, pt4.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
						amount++;
					}
				}
				if(amount >= 3) {
					for(int i = 0; i < 4; i++) {
						int xP = 0;
						int yP = 0;
						switch (i) {
						case 0:
							xP = 0;
							yP = 0;
							break;
						case 1:
							xP = 1;
							yP = 0;
							break;
						case 2:
							xP = 0;
							yP = 1;
							break;
						case 3:
							xP = 1;
							yP = 1;

						default:
							break;
						}
						crossroads.add(new GridPoint(x + xP, y + yP));
						Crossroad cross = new Crossroad(space, grid);
						context.add(cross);
						grid.moveTo(cross, x + xP, y + yP);
						space.moveTo(cross, x + xP, y + yP);
						context.add(cross);
					}
				}
			}
		}
	}
	
	private void findCrossdroadsTest(Grid<Object> grid, ContinuousSpace<Object> space, Context<Object> context) {
		for(int x = 2; x < grid.getDimensions().getWidth() - 2; x++) {
			for(int y = 2; y < grid.getDimensions().getHeight() - 2; y++) {
				int amount = 0;
				GridPoint pt1 = new GridPoint(x - 1, y);
				GridPoint pt2 = new GridPoint(x, y - 1);
				GridPoint pt3 = new GridPoint(x + 2, y);
				GridPoint pt4 = new GridPoint(x, y + 2);
				if(StreamSupport.stream(grid.getObjectsAt(pt1.getX(), pt1.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
					if(StreamSupport.stream(grid.getObjectsAt(pt1.getX(), pt1.getY() + 1).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
						amount++;
					}
				}
				if(StreamSupport.stream(grid.getObjectsAt(pt2.getX(), pt2.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
					if(StreamSupport.stream(grid.getObjectsAt(pt2.getX() + 1, pt2.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
						amount++;
					}
				}
				if(StreamSupport.stream(grid.getObjectsAt(pt3.getX(), pt3.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
					if(StreamSupport.stream(grid.getObjectsAt(pt3.getX(), pt3.getY() + 1).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
						amount++;
					}
				}
				if(StreamSupport.stream(grid.getObjectsAt(pt4.getX(), pt4.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
					if(StreamSupport.stream(grid.getObjectsAt(pt4.getX() + 1, pt4.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
						amount++;
					}
				}
				if(amount >= 3) {
					for(int i = 0; i < 4; i++) {
						int xP = 0;
						int yP = 0;
						switch (i) {
						case 0:
							xP = 0;
							yP = 0;
							break;
						case 1:
							xP = 1;
							yP = 0;
							break;
						case 2:
							xP = 0;
							yP = 1;
							break;
						case 3:
							xP = 1;
							yP = 1;

						default:
							break;
						}
						
						Crossroad cross = new Crossroad(space, grid);
						context.add(cross);
						grid.moveTo(cross, x + xP, y + yP);
						space.moveTo(cross, x + xP, y + yP);
						context.add(cross);
					}
				}
			}
		}
	}
	
	private List<GridPoint> getNeighborhood(GridPoint pt) {
		List<GridPoint> result = new ArrayList<GridPoint>();
		
		for(int j = pt.getY() - 1; j <= pt.getY() + 1; j++) {
			for(int i = pt.getX() - 1; i <= pt.getX() + 1; i++) {
				result.add(new GridPoint(i, j));
			}
		}
		return result;
	}
	
	/*
	private boolean existsAt(Object obj, GridPoint pt, Grid<Object> grid) {
		boolean result = StreamSupport.stream(grid.getObjectsAt(pt.getX(), pt.getY()).spliterator(), false).anyMatch(obj -> obj.equals(obj));
	}
	*/
	
	private Object[] ondraObjectsAt(GridPoint pt, Grid<Object> grid){
		Stream<Object> stream = StreamSupport.stream(grid.getObjectsAt(pt.getX(), pt.getY()).spliterator(), false);
		return stream.toArray();
		
	}
	
	private void removeUnsuitableTom(Grid<Object> grid) {
		List<GridPoint> localStarts = new ArrayList<GridPoint>();
		List<GridPoint> localEnds = new ArrayList<GridPoint>();
		for (int i = 0; i < starts.size(); i++) {
			GridCellNgh<Crossroad> nghCreator = new GridCellNgh<Crossroad>(grid, starts.get(i), Crossroad.class, 1, 1);
			List<GridCell<Crossroad>> gridCells = nghCreator.getNeighborhood(false);
			
			GridCellNgh<Road> nghCreator2 = new GridCellNgh<Road>(grid, starts.get(i), Road.class, 1, 1);
			List<GridCell<Road>> gridCells2 = nghCreator2.getNeighborhood(false);
			int amountOfCrossroads = 0;
			int amountOfRoads = 0;
			for (GridCell<Crossroad> gridCell : gridCells) {
				amountOfCrossroads += gridCell.size();
			}
			for(GridCell<Road> gridCell : gridCells2) {
				amountOfRoads += gridCell.size();
			}
			if(amountOfCrossroads > 0) {
				localStarts.add(starts.get(i));
			}
			if(amountOfRoads > 2) {
				localStarts.add(starts.get(i));
			}
		}
		for (int i = 0; i < ends.size(); i++) {
			GridCellNgh<Crossroad> nghCreator = new GridCellNgh<Crossroad>(grid, ends.get(i), Crossroad.class, 1, 1);
			List<GridCell<Crossroad>> gridCells = nghCreator.getNeighborhood(false);
			
			GridCellNgh<Road> nghCreator2 = new GridCellNgh<Road>(grid, ends.get(i), Road.class, 1, 1);
			List<GridCell<Road>> gridCells2 = nghCreator2.getNeighborhood(false);
			int amountOfCrossroads = 0;
			int amountOfRoads = 0;
			for (GridCell<Crossroad> gridCell : gridCells) {
				amountOfCrossroads += gridCell.size();
			}
			for(GridCell<Road> gridCell : gridCells2) {
				amountOfRoads += gridCell.size();
			}
			if(amountOfCrossroads > 0) {
				localEnds.add(ends.get(i));
			}
			if(amountOfRoads > 2) {
				localEnds.add(ends.get(i));
			}
		}
		starts.removeAll(localStarts);
		ends.removeAll(localEnds);
	}
	
	private void removeUnsuitableTomTest(List<ExitStart> endNodes ,Grid<Object> grid) {
		List<ExitStart> localExitStarts = new ArrayList<ExitStart>();
		for (int i = 0; i < endNodes.size(); i++) {
			GridCellNgh<Crossroad> nghCreator = new GridCellNgh<Crossroad>(grid, endNodes.get(i).getStart(), Crossroad.class, 1, 1);
			List<GridCell<Crossroad>> gridCells = nghCreator.getNeighborhood(false);
			
			GridCellNgh<Road> nghCreator2 = new GridCellNgh<Road>(grid, endNodes.get(i).getStart(), Road.class, 1, 1);
			List<GridCell<Road>> gridCells2 = nghCreator2.getNeighborhood(false);
			
			int amountOfCrossroads = 0;
			int amountOfRoads = 0;
			for (GridCell<Crossroad> gridCell : gridCells) {
				amountOfCrossroads += gridCell.size();
			}
			for(GridCell<Road> gridCell : gridCells2) {
				amountOfRoads += gridCell.size();
			}
			if(amountOfCrossroads > 0 || amountOfRoads > 2) {
				localExitStarts.add(endNodes.get(i));
				System.out.println("Amount of Crossroads - " + amountOfCrossroads + "Amount of Roads - " + amountOfRoads);
			}
		}
		/*
		for (int i = 0; i < ends.size(); i++) {
			GridCellNgh<Crossroad> nghCreator = new GridCellNgh<Crossroad>(grid, ends.get(i), Crossroad.class, 1, 1);
			List<GridCell<Crossroad>> gridCells = nghCreator.getNeighborhood(false);
			
			GridCellNgh<Road> nghCreator2 = new GridCellNgh<Road>(grid, ends.get(i), Road.class, 1, 1);
			List<GridCell<Road>> gridCells2 = nghCreator2.getNeighborhood(false);
			int amountOfCrossroads = 0;
			int amountOfRoads = 0;
			for (GridCell<Crossroad> gridCell : gridCells) {
				amountOfCrossroads += gridCell.size();
			}
			for(GridCell<Road> gridCell : gridCells2) {
				amountOfRoads += gridCell.size();
			}
			if(amountOfCrossroads > 0 || amountOfRoads > 2) {
				localEnds.add(ends.get(i));
				
			}
		}
		for (GridPoint end : localEnds) {
			
		}
		*/
		endNodes.removeAll(localExitStarts);
	}
	
	private void removeNotSuitable(Grid<Object> grid) {
		for(int i = starts.size() - 1; i <= 0; i--) {
			GridPoint start = starts.get(i);
			List<GridPoint> neighborhood = getNeighborhood(start);
			for (GridPoint neighbor : neighborhood) {
				for (GridPoint crossroad : crossroads) {
					if(crossroad.getX() == neighbor.getX() && crossroad.getY() == neighbor.getY()) {
						starts.remove(start);
						i++;
					}
				}
			}
		}
		for(int i = ends.size() - 1; i <= 0 ; i--) {
			GridPoint start = ends.get(i);
			List<GridPoint> neighborhood = getNeighborhood(start);
			for (GridPoint neighbor : neighborhood) {
				for (GridPoint crossroad : crossroads) {
					if(crossroad.getX() == neighbor.getX() && crossroad.getY() == neighbor.getY()) {
						ends.remove(start);
						i++;
					}
				}
			}
		}
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
