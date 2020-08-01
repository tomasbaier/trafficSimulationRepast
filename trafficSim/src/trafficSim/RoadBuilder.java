package trafficSim;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	public Context build(Context<Object> context) {
		
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("traffic network", context, true);
		netBuilder.buildNetwork();
		context.setId("trafficSim");
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.StrictBorders(), 50, 50);
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(new WrapAroundBorders(), new SimpleGridAdder<Object>(), true, 50 ,50));
		
		//PLACEHOLDER PLACING (NOT FINAL)
		placeRoads(new GridPoint(2, 10), new GridPoint(38, 42), space, grid, context);
		placeRoads(new GridPoint(6, 16), new GridPoint(45, 47), space, grid, context);
		placeRoads(new GridPoint(8, 7), new GridPoint(12, 30), space, grid, context);
		/*
		for(int i = 0; i < 5; i++) {
			placeRandomRoads(space, grid, context);
		}
		*/
			
		
		
		//Car placement
		//Randomizes the array with exits
		
		//System.out.println("CAR SPAWN: " + carSpawn);
		for(int i = 0; i < 5; i++) {
			SimUtilities.shuffle(exits, RandomHelper.createUniform());
			GridPoint carSpawn = exits.get(0);
			exits.remove(0);
			Car car = new Car(space, grid, carSpawn.getX(), carSpawn.getY(), exits);
			context.add(car);
			space.moveTo(car, carSpawn.getX(), carSpawn.getY());
			grid.moveTo(car, carSpawn.getX(), carSpawn.getY());
			context.add(car);
		}
		
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
	
	private void placeRoads(GridPoint startPoint, GridPoint exitPoint, ContinuousSpace<Object> space, Grid<Object> grid, Context<Object> context) {
		int x1 = startPoint.getX();
		int y1 = startPoint.getY();
		int x2 = exitPoint.getX();
		int y2 = exitPoint.getY();
		
		
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
			GridPoint newRoad = new GridPoint(i, y1);
			if(roads.contains(newRoad)) {
				Crossroad crossroad = new Crossroad(space, grid);
				context.add(crossroad);
				grid.moveTo(crossroad, i, y1);
				space.moveTo(crossroad, i, y1);
				context.add(crossroad);
			} else {
				roads.add(new GridPoint(i, y1));
			}
		}
		
		//Placement of horizontal road
		for(int i = y1; i <= y2; i++) {
			Road road = new Road(space, grid);
			context.add(road);
			grid.moveTo(road, x2, i);
			space.moveTo(road, x2, i);
			context.add(road);
			GridPoint newRoad = new GridPoint(x2, i + 1);
			if(roads.contains(newRoad)) {
				Crossroad crossroad = new Crossroad(space, grid);
				context.add(crossroad);
				grid.moveTo(crossroad, x2, i + 1);
				space.moveTo(crossroad, x2, i + 1);
				context.add(crossroad);
			} else {
				roads.add(new GridPoint(x2, i + 1));
			}
			
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
		//roads = new ArrayList<GridPoint>();
	}
}
