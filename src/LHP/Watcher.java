package LHP;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.rosuda.JRI.Rengine;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.ShapefileWriter;
import tools.GroupUtils;

public class Watcher {

	// variables to monitor
	int month;
	double runtime=0;
	final RunEnvironment runEnvironment;
	Executor executor;
	ArrayList<ArrayList<Double>> homeRange;
	ArrayList<ArrayList<MarkerPoint>> markerPoints;
	long markerPointTag;
	WritableSheet excelSheet_homeRange;
	WritableWorkbook workbook;
	ArrayList<ArrayList<Integer>> groupSize;
	public static long scrambleCount;



	public Watcher(Executor exe) {

		//Initialize watcher variables
		markerPoints = new ArrayList<ArrayList<MarkerPoint>>();
		markerPointTag = 0;
		month=0;
		homeRange = new ArrayList<ArrayList<Double>>();
		groupSize = new ArrayList<ArrayList<Integer>>();
		scrambleCount=0;
		executor = exe;
		runtime=0;

		//initialize arraylists to store variables
		for (int i=0;i<Parameter.rcGroups;i++){
			groupSize.add(new ArrayList<Integer>());
			homeRange.add(new ArrayList<Double>());
			markerPoints.add(new ArrayList<MarkerPoint>());
		}

		runEnvironment = RunEnvironment.getInstance();

		//creating a file to store the output of the counts
		try {
			File fileOut = new File("C:/Users/t-work/Dropbox/project_Agent_interaction_hetero_and_parallel/code2/LHP/data/MovementStats_par_"+Parameter.rcGroupSize+"_"+Parameter.envHetero+"_"+Parameter.groupProcess+"_"+Parameter.numbOfThreads +Parameter.randomSeed+"_"+System.currentTimeMillis()+ ".xls");
			WorkbookSettings wbSettings = new WorkbookSettings();
			wbSettings.setLocale(new Locale("en", "EN"));

			workbook = Workbook.createWorkbook(fileOut, wbSettings);
			workbook.createSheet("HomeRange", 0);
			excelSheet_homeRange = workbook.getSheet(0);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/********************************	
	 * 								*
	 *         Stepper				*
	 * 								*
	 *******************************/

	@ScheduledMethod(start=26, interval = 1,priority=0)
	public void step(){

		//	to do every tick
		markCenterOfGroup();

		//	to do every month
		if(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()%(26*30)==0){
			recordHomeRange();
			month++;
			System.out.println("Month - "+month);
		}

		//	to do at the end of the simulation
		if(RunEnvironment.getInstance().getCurrentSchedule().getTickCount()>=26*30*Parameter.endMonth){
			executor.shutdown();
			runtime = System.currentTimeMillis() - ModelSetup.timeRecord_start;
			System.out.println("total time " + (System.currentTimeMillis() - ModelSetup.timeRecord_start) + "  : SrambleCount - " + scrambleCount);
			recordData();				//record the data to an excel sheet and txt file
			RunEnvironment.getInstance().endAt(this.runEnvironment.getCurrentSchedule().getTickCount());
		}
	}


	/********************************************	
	 * 											*
	 *          Home range methods				*
	 * 											*
	 ********************************************/

	private void recordHomeRange(){

		//Use group centers (marker points)
		for (ArrayList<MarkerPoint> mp:markerPoints){
			int groupID = -1;
			//within one group: create a coordlist
			Coordinate[] coordList = new Coordinate[mp.size()];
			int count=0;
			for(MarkerPoint p: mp){
				coordList[count]=p.getCoord();
				count++;
				groupID=p.getGroupID();
				ModelSetup.getContext().remove(mp);
			}

			//calculate the convexhull
			ConvexHull convexHull = new ConvexHull(coordList,new GeometryFactory());
			double ch_Estimate = convexHull.getConvexHull().getArea();

			//add home range estimate to recorder
			homeRange.get(groupID).add(ch_Estimate);

			//clear for next month
			mp.clear();
		}
	}

	private void markCenterOfGroup(){


		for (Primate p : ModelSetup.primatesAll){

			//estimate center of the group
			Coordinate center = estimateCenter();

			//create point in the center
			MarkerPoint mp = new MarkerPoint(ModelSetup.getContext(),ModelSetup.getGeog(),center,markerPointTag++,month,p.getGroupID());

			//add point to the geog
			mp.createPoint();

			//add point to the arraylist
			markerPoints.get(p.getGroupID()).add(mp);
		}
	}


	public Coordinate estimateCenter(){
		ArrayList<Primate> primatesConsidered = ModelSetup.primatesAll;
		Coordinate coord = GroupUtils.getGroupCenter(primatesConsidered);
		return coord;
	}


	/********************************	
	 * 								*
	 *         Output data			*
	 * 								*
	 *******************************/


	private void recordData(){


		//calculate the mean and sd of all home ranges
		double avg_homeRange=0, sd_homeRange=0;

		int row=1,col=1; //for output to excel sheet

		try {

			//add home range estimate to excel output
			String title = "HomeRange estimates (monthly)";
			Label label;
			label = new Label(col, row,title);
			row++;
			excelSheet_homeRange.addCell(label);

			for (ArrayList<Double> hr: homeRange){
				for (Double d: hr){
					Number number;
					number = new Number(col,row,d);
					excelSheet_homeRange.addCell(number);
					avg_homeRange = avg_homeRange + d;
					row++;
				}
				row=2;
				col++;
			}
			workbook.write();
			workbook.close();

			
			//mean home range
			avg_homeRange = avg_homeRange / (double)(Parameter.rcGroups * Parameter.endMonth);
			//sd home range
			for (ArrayList<Double> hr: homeRange){
				for (Double d: hr){
					sd_homeRange = sd_homeRange + Math.pow(d-avg_homeRange,2);
				}
			}
			sd_homeRange = Math.pow(sd_homeRange/(double)(Parameter.rcGroups*Parameter.endMonth-1),0.5);


			//write to result file
			BufferedWriter writer2 = new BufferedWriter(new FileWriter("C:/Users/t-work/Dropbox/project_Agent_interaction_hetero_and_parallel/code2/LHP/data/Results_permanet.csv",true));
			writer2.newLine();
			writer2.append(Double.toString(avg_homeRange));
			writer2.append(",");
			writer2.append(Double.toString(sd_homeRange));
			writer2.append(",");
			writer2.append(Double.toString(Parameter.envHetero));
			writer2.append(",");
			writer2.append(Double.toString(Parameter.numbOfThreads));
			writer2.append(",");
			writer2.append(Long.toString(scrambleCount));
			writer2.append(",");
			writer2.append(Boolean.toString(Parameter.groupProcess));
			writer2.append(",");
			writer2.append(Double.toString(runtime));
			writer2.append(",");
			writer2.append(Double.toString(Parameter.rcGroupSize));
			writer2.append(",");
			writer2.append(Double.toString(Parameter.cellSize));
			writer2.append(",");
			writer2.append(Integer.toString(Parameter.randomSeed));


			writer2.flush();
			writer2.close();


		} catch (RowsExceededException e) {		e.printStackTrace();
		} catch (WriteException e) {			e.printStackTrace();
		} catch (IOException e){				e.printStackTrace();
		}

	}

	/********************************	
	 * 								*
	 *         misc methods			*
	 * 								*
	 *******************************/

	public synchronized static void addScrambleCount(){
		scrambleCount++;
	}
	public synchronized int getTickCount(){
		return (int) this.runEnvironment.getCurrentSchedule().getTickCount(); 
	}
	public static int[] convertIntegers(List<Integer> integers)
	{
		int[] ret = new int[integers.size()];
		for (int i=0; i < ret.length; i++)
		{
			ret[i] = integers.get(i).intValue();
		}
		return ret;
	}
	public static double[] convertLongToDouble(List<Long> integers)
	{
		double[] ret = new double[integers.size()];
		for (int i=0; i < ret.length; i++)
		{
			ret[i] = integers.get(i).doubleValue();
		}
		return ret;
	}
}
