package LHP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import jsc.distributions.Lognormal;
import jsc.distributions.Pareto;

import cern.jet.random.Exponential;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactory;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import tools.SimUtils;

//This file builds the model: creating the environment and populating it with primate hosts and parasites
public class ModelSetup implements ContextBuilder<Object>{

	private static Context mainContext;
	private static Geography geog;
	private static int cellsAdded;
	private static double resAdded;
	public static int primatesAdded;
	public static ArrayList<Primate> primatesAll;
	public static Iterable<Primate> primateIterator;
	public static ArrayList<Cell> cellsToUpdate;
	public static ArrayList<Cell> removeCellsToUpdate;
	public static ArrayList<Cell> allCells;
	public static double timeRecord;
	public static double timeRecord_start;

	public Context<Object> build(Context<Object> context){

		System.out.println("Running a parallel host foraging model");
		
		/********************************
		 * 								*
		 * initialize model parameters	*
		 * 								*
		 * ******************************/
		
		cellsAdded = 0;
		resAdded=0;
		primatesAdded = 0;
		primatesAll= new ArrayList<Primate>();
		mainContext = null;
		geog=null;
		cellsToUpdate = new ArrayList<Cell>();
		removeCellsToUpdate = new ArrayList<Cell>();
		allCells = new ArrayList<Cell>();
		Parameter parameter = new Parameter();
		mainContext = context; //static link to context
		timeRecord = System.currentTimeMillis();
		timeRecord_start = System.currentTimeMillis();

		/****************************
		 * 							*
		 * Building the landscape	*
		 * 							*
		 * *************************/

		//Create Geometry factory; used to create gis shapes (points=primates; polygons=resources)
		GeometryFactory fac = new GeometryFactory();

		//x and y dims of the map file
		int xdim = 2000/Parameter.cellSize;
		int ydim = 2000/Parameter.cellSize;
		double xcoordStart = 0;
		double ycoord= 0;
		double xcoord= 0;

		//Create Geography/GIS 
		GeographyParameters<Object> params= new GeographyParameters<Object>();
		GeographyFactory factory = GeographyFactoryFinder.createGeographyFactory(null);
		geog = factory.createGeography(Parameter.geog, context, params);
		//geog.setCRS("EPSG:32636"); //WGS 84 / UTM zone 36N

		//Add Resources to the environment *****************************************//
		Lognormal lognorm = new Lognormal(100,Parameter.envHetero);
		
		for (int i = 0; i < ydim; ++i) {
			for (int j = 0; j < xdim; ++j) {

					double food = lognorm.random();
					
					final Cell cell = new Cell(context,xcoord,ycoord,food,j,i);
					resAdded=resAdded+food;
					cellsAdded++;
					allCells.add(cell);

				//shift ycoord by cell size value
				xcoord=xcoord+Parameter.cellSize;
			}
			//Set ycoord back to the start and shift xcoord up by cell size value
			xcoord=xcoordStart;
			ycoord = ycoord-Parameter.cellSize;
		}
		

		//controls amount of resources in each run (same amount) 
		setTotalResources(cellsAdded);

		//to simplify the model all cells record the visible neighbours (within visible range for a primate)
		for (Cell c: allCells){
			c.setVisibleNeigh();
		}

		System.out.println("done adding grids (" + cellsAdded + " size "+ Parameter.cellSize + "): AND threads = " + Parameter.numbOfThreads + "  env.hetero = "+ Parameter.envHetero );

		/************************************
		 * 							        *
		 * Adding hosts to the landscape	*
		 * 							        *
		 * *********************************/

		//adding red colobus hosts to the landscape
		for (int i = 0; i<Parameter.rcGroups;i++){

			//keep track of groups being added
			ArrayList<Primate> group = new ArrayList<Primate>();

			//center of group (fixed)
			double xCenter =1000;// 750;
			double yCenter = -1000;//-750;
			
			//select the number of RC in this group (fixed)
			int groupSize = Parameter.rcGroupSize;

			//add individuals
			for (int j = 0; j < groupSize; j++){
				Coordinate coord = new Coordinate(xCenter+0.005+ThreadLocalRandom.current().nextDouble(-(30),(30)), yCenter-0.005+ThreadLocalRandom.current().nextDouble(-30,(30)));
				RedColobus rc = new RedColobus(i,primatesAdded++,coord,groupSize);
				context.add(rc);
				primatesAll.add(rc);
				group.add(rc);
				Point geom = fac.createPoint(coord);
				geog.move(rc, geom);
				rc.myPatch = SimUtils.getCellAtThisCoord(coord);
			}
		}

		/************************************
		 * 							        *
		 * create the scheduling			*
		 * 							        *
		 * *********************************/
		
		//executor takes care of the processing of the schedule
		Executor executor = new Executor();
		createSchedule(executor);
		
		/************************************
		 * 							        *
		 * Adding watcher agent				*
		 * 							        *
		 * *********************************/

		//watcher agent records all output
		Watcher w = new Watcher(executor);
		context.add(w);


		return context;

	}
	
	
	/************************************
	 * 							        *
	 * Supporting methods				*
	 * 							        *
	 * *********************************/
	

	private void createSchedule(Executor executor){

		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();

		if(Parameter.groupProcess==true){ //subgroups of agents passed through each separate process, then next subgroup processed

			ScheduleParameters agentStepParamsPrimate = ScheduleParameters.createRepeating(1, 1, 6); //start, interval, priority (high number = higher priority)
			schedule.schedule(agentStepParamsPrimate,executor,"processPrimates");

		} else {  //all agents passed through separate process, one process at a time

			ScheduleParameters agentStepParamsPrimate = ScheduleParameters.createRepeating(1, 1, 6); //start, interval, priority (high number = higher priority)
			schedule.schedule(agentStepParamsPrimate,executor,"getInputs");

			ScheduleParameters agentStepParamsPrimateBehaviour = ScheduleParameters.createRepeating(1, 1, 5); //start, interval, priority (high number = higher priority)
			schedule.schedule(agentStepParamsPrimateBehaviour,executor,"behaviour");

			ScheduleParameters agentStepParamsPrimateEnergy = ScheduleParameters.createRepeating(1, 1, 4); //start, interval, priority (high number = higher priority)
			schedule.schedule(agentStepParamsPrimateEnergy,executor,"energyUpdate");

		}
		
		ScheduleParameters agentStepParams = ScheduleParameters.createRepeating(1, 1, 2);
		schedule.schedule(agentStepParams,executor,"envUpdate");
	}
	

	//used to update only the cells which have been modified, or are growing back
	@ScheduledMethod(start=1, interval = 1,priority=1)
	public static void removeCellsUpdated(){
		for(Cell c : removeCellsToUpdate){
			cellsToUpdate.remove(c);
		}
		removeCellsToUpdate.clear();
	}
	
	//used to maintain constant total food values for each model run
	private void setTotalResources(int numbCells){
		//calculate the percent difference between the total resource level now and the target level
		double targetRes = Parameter.foodTotal;
		double perDiff = (double)resAdded / (double)targetRes;
		double newTotal=0;

		//Divide each resource by the percent difference to make the total equal the target resource amount
		for (Cell c : getAllCellAgents()){
			c.setMaxResourceLevel(((double)c.getMaxResourceLevel()/(double)perDiff));
			c.setResourceLevel(((double)c.getResourceLevel()/(double)perDiff));
			newTotal = newTotal + c.getResourceLevel();
		}
	}
	

	public static void stopSim(Exception ex, Class<?> clazz) {
		ISchedule sched = RunEnvironment.getInstance().getCurrentSchedule();
		sched.setFinishing(true);
		sched.executeEndActions();
		//LOGGER.log(Level.SEVERE, "ContextManager has been told to stop by " + clazz.getName(), ex);
	}

	public static Iterable<Cell> getAllCellAgents() {
		return allCells;
	}

	public static Iterable<Primate> getAllPrimateAgents(){
		Collections.shuffle(primatesAll, new Random(System.currentTimeMillis()));
		Iterable<Primate> agents = primatesAll;
		return agents;
	}

	public static Context<Cell> getContext() {
		return ModelSetup.mainContext;
	}
	public static Geography<Primate> getGeog(){
		return geog;
	}
	public synchronized static void addToCellUpdateList(Cell c){  //need to read up on the synchronized implimentation
		if(cellsToUpdate.contains(c)==false)cellsToUpdate.add(c);
	}
	public synchronized static void removeCellToUpdate(Cell c){
		removeCellsToUpdate.add(c);
	}
	public static ArrayList<Cell> getCellsToUpdate(){
		return cellsToUpdate;
	}
	public static <T> Geometry getAgentGeometry(T agent) {
		return getGeog().getGeometry(agent);
	}
}
