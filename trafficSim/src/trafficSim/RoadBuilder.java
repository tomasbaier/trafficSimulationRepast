package trafficSim;

import java.util.ArrayList;
import java.util.List;
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
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.util.SimUtilities;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.SimpleGridAdder;

public class RoadBuilder implements ContextBuilder<Object> {
	
	private List<GridPoint> entries = new ArrayList<GridPoint>();
	private List<GridPoint> roads = new ArrayList<GridPoint>();
	private List<GridPoint> exits = new ArrayList<GridPoint>();
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

		createEntries(exitCount, grid, 6);
		
		while(!entries.isEmpty()) {
			GridPoint start = entries.get(0);
			GridPoint end = entries.get(1);
			generateRoads(start, end, grid, space, context);
			generateExitStarts(start, end, grid, space, context);
			entries.remove(start);
			entries.remove(end);
		}
		
		filterEndPointTest(exitStarts, grid, space);
		
		findCrossdroads(grid, space, context);

		removeUnsuitable(exitStarts ,grid);

		for(int i = 0; i < exitStarts.size(); i++) {
			ExitStart exitStart = exitStarts.get(i);
			GridPoint startCoords = exitStart.getStart();
			GridPoint exitCoords = exitStart.getExit();
			Start start = new Start();
			Exit exit = new Exit();
			context.add(start);
			context.add(exit);
			grid.moveTo(start, startCoords.getX(), startCoords.getY());
			space.moveTo(start, startCoords.getX(), startCoords.getY());
			grid.moveTo(exit, exitCoords.getX(), exitCoords.getY());
			space.moveTo(exit, exitCoords.getX(), exitCoords.getY());
		}
		
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
		
		roads = new ArrayList<GridPoint>();
		exitStarts = new ArrayList<ExitStart>();
		entries = new ArrayList<GridPoint>();
		starts = new ArrayList<GridPoint>();
		exits = new ArrayList<GridPoint>();
		return context;
	}
	
	//Tvorba entries mezi kterými se následnì tvoøí cesty(road)
	private void createEntries(int amount, Grid<Object> grid, int distance) {
		GridPoint pt1 = null;
		GridPoint pt2 = null;
		while(entries.size() < amount * 2) {
			if(Math.random() < 0.5) {
				int randomY1 = (int)(Math.random() * (grid.getDimensions().getHeight() / 2) + 2);
				int testerX = (int)(Math.random() * grid.getDimensions().getWidth());
				int subtractor = testerX < (grid.getDimensions().getWidth() / 2) ? 2 : -2;
				int randomX = testerX + subtractor;
				int randomY2 = grid.getDimensions().getHeight() / 2 + ((int)(Math.random() * (grid.getDimensions().getHeight()) / 2) - 2);
				pt1 = new GridPoint(randomX, randomY1);
				pt2 = new GridPoint(randomX, randomY2);
			} else {
				int randomX1 = (int)(Math.random() * (grid.getDimensions().getWidth()) / 2 + 2);
				int testerY = (int)(Math.random() * grid.getDimensions().getHeight());
				int subtractor = testerY < (grid.getDimensions().getHeight() / 2) ? 2 : -2;
				int randomY = testerY + subtractor;
				int randomX2 = grid.getDimensions().getWidth() / 2 + (int)(Math.random() * (grid.getDimensions().getWidth() / 2) - 2);
				pt1 = new GridPoint(randomX1, randomY);
				pt2 = new GridPoint(randomX2, randomY);
			}
			
			if(!validateAlt(pt1, entries, distance) && !validateAlt(pt2, entries, distance)) {
				entries.add(pt1);
				entries.add(pt2);
			}
		}
	}
	
	//Tvorba objektù exit+start podle orientace silnice
	private void generateExitStarts(GridPoint startPT, GridPoint endPT, Grid<Object> grid, ContinuousSpace<Object> space, Context<Object> context) {
		if(startPT.getX() == endPT.getX()) {
			GridPoint start1 = new GridPoint(startPT.getX() + 1, startPT.getY() - 1);
			GridPoint exit1 = new GridPoint(startPT.getX(), startPT.getY() - 1);
			
			starts.add(start1);
			exits.add(exit1);
			
			exitStarts.add(new ExitStart(start1, exit1));
			
			GridPoint start2 = new GridPoint(endPT.getX(), endPT.getY() + 1);
			GridPoint exit2 = new GridPoint(endPT.getX() + 1, endPT.getY() + 1);
			
			starts.add(start2);
			exits.add(start2);		
			
			exitStarts.add(new ExitStart(start2, exit2));
		}
		if(startPT.getY() == endPT.getY()) {
			GridPoint start1 = new GridPoint(startPT.getX() - 1, startPT.getY());
			GridPoint exit1 = new GridPoint(startPT.getX() - 1, startPT.getY() + 1);
			
			starts.add(start1);
			exits.add(exit1);
			
			exitStarts.add(new ExitStart(start1, exit1));
			
			GridPoint start2 = new GridPoint(endPT.getX() + 1, endPT.getY() + 1);
			GridPoint exit2 = new GridPoint(endPT.getX() + 1, endPT.getY());
			
			starts.add(start2);
			exits.add(exit2);
			
			exitStarts.add(new ExitStart(start2, exit2));
		}
	}

	//Generování silnic(road) mezi dvìma entries
	private void generateRoads(GridPoint start, GridPoint end, Grid<Object> grid, ContinuousSpace<Object> space, Context<Object> context) {
		if(start.getX() == end.getX()) {
			for(int i = start.getY(); i != end.getY() + 1; i ++) {
				roads.add(new GridPoint(start.getX(), i));
				roads.add(new GridPoint(start.getX(), i + 1));
				Road road = new Road();
				Road road2 = new Road();
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
			for(int i = start.getX(); i != end.getX() + 1; i++) {
				roads.add(new GridPoint(i, start.getY()));
				roads.add(new GridPoint(i + 1, start.getY()));
				
				Road road = new Road();
				Road road2 = new Road();
				
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

	//Filtrace z entries pokud leží na silnici
	private void filterEndPointTest(List<ExitStart> pts, Grid<Object> grid, ContinuousSpace<Object> space) {
		List<ExitStart> localArray = new ArrayList<ExitStart>();
		for (int i = 0; i < pts.size(); i++) {
			GridPoint pt = pts.get(i).getStart();
			if(StreamSupport.stream(grid.getObjectsAt(pt.getX(), pt.getY()).spliterator(), false).anyMatch(Road -> Road.equals(Road))) {
				localArray.add(pts.get(i));
			}
		}
		pts.removeAll(localArray);
	}

	//Validace entry, jestli nezasahuje do prostoru jiného entry
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
	
	//Nalezení køižovatek ve vygenerovaných silnicích
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
						Crossroad cross = new Crossroad();
						context.add(cross);
						grid.moveTo(cross, x + xP, y + yP);
						space.moveTo(cross, x + xP, y + yP);
						context.add(cross);
					}
				}
			}
		}
	}

	//Odstranení nevhodných start/exit
	private void removeUnsuitable(List<ExitStart> endNodes ,Grid<Object> grid) {
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
			}
		}
		endNodes.removeAll(localExitStarts);
	}
}
