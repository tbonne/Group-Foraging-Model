package LHP;

import cern.jet.random.Normal;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;

public class Parameter {
	
	final static Parameters p = RunEnvironment.getInstance().getParameters();
	
	//repast simphony
	public final static String agent_context = "cellContext";
	public final static String geog = "geog";
	public static int randomSeed = 1847620; 			//adjusted in constructor
	public static int numbOfThreads = 7;				//multipule of # of cores (e.g. 8 * 2 = 16)
	public static boolean groupProcess = true;			//sub-groups of agents processed separately, or all agents passed through each separate process
	
	//Watcher agent (for observations)
	public final static int stepsPerDay = 26;			//each step represents 30 min, for a total of 13hours of active time per day
	public final static int endMonth = 3;		 		// will run the simulation until it has completed this many months (1 month = 30 days = 780 steps)
	
	//resources
	public final static double regrowthRate = 1.25; 	//calibrated to fit homerange estimes of observed RC group in study area
	public final static int foodTotal = 435600;       	//calibrated to fit homerange estimes of observed RC group in study area
	public static int cellSize = 30;					//size of the resource grids
	public static double envHetero = 1;					//heterogeneity of the food landscape
	
	//primate: capabilities
	public final static int foodSearchRange = 100;			//distance (meter) at which individuals can see resource grids
	public final static int primateSearchRange = 200;		//distance (meter) at which individuals can see other individuals
	public final static int safeRadius = 50;				//distance (meter) at which individuals gain safety from other individuals
	public final static int maxDistancePerStep = 100; 		//distance (meter) that an individual can travel within one time-step
	
	//Primate: energetics
	public final static int targetEnergy_mean = 100;		//Target energy an individual tries to maintain
	public final static double rcDaySpentFeeding = 0.43;	//proportion of the day individuals feed, used to calibrate intake rate of individuals
	public final static int rcBiteSize =  (int)(targetEnergy_mean / (stepsPerDay * 0.43) );			//energy gained from each feeding (set to meet rcDaySpentFeeding)
	public final static int rcEnergyLoss = (int)(targetEnergy_mean / stepsPerDay);					//energy lost per time step
	
	//Primates: group dynamics 
	public final static int rcGroups = 1;					//number of groups 
	public static int rcGroupSize = 50;
	

	//Constructor: used to set values from batch runs or the GUI
	public Parameter(){
		randomSeed = (Integer)p.getValue("randomSeed");
		
		numbOfThreads = (Integer)p.getValue("numbThreads");
		envHetero = (Double)p.getValue("envHetero");
		groupProcess = (Boolean)p.getValue("grouped");
		rcGroupSize = (Integer)p.getValue("groupSize");
		cellSize = (Integer)p.getValue("cellSize");
	}
}
