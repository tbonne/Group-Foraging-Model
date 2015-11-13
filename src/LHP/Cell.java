package LHP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;

import tools.SimUtils;

public class Cell {
	
	//geometry
	Context context;
	Geography geog;
	Geometry geom;
	Coordinate coord; //centroid of cell
	
	//resources
	double resources,dbh;
	double maxResources;
	double regrowRate =Parameter.regrowthRate;
	ArrayList<Cell> visibleSites;
	
	//memory
	int remembered =0;
	
	//random generator
	private Random randomGenerator;
	
	//visit counts
	double visit=0;
	
	/********************************	
	 * 								*
	 *         Initialize cell		*
	 * 								*
	 *******************************/
	
	public Cell(Context con,double x,double y,double r,int gx, int gy){
		
		//set initial variables
		resources = r;
		dbh=r;
		maxResources = resources; //maintains initial conditions
		visibleSites = new ArrayList<Cell>();
		con.add(this);
		
		//place the cell on a gis landscape
		geog = (Geography)con.getProjection("geog");
		GeometryFactory fac = new GeometryFactory();
		Coordinate cTR = new Coordinate(x+(Parameter.cellSize/(2.0)),y+(Parameter.cellSize/(2.0)));//top right
		Coordinate cTL = new Coordinate(x-(Parameter.cellSize/(2.0)),y+(Parameter.cellSize/(2.0)));//top left
		Coordinate cBL = new Coordinate(x-(Parameter.cellSize/(2.0)),y-(Parameter.cellSize/(2.0)));//bottom left
		Coordinate cBR = new Coordinate(x+(Parameter.cellSize/(2.0)),y-(Parameter.cellSize/(2.0)));//botton right
		Coordinate[] boundingCoords = {cTR,cTL,cBL,cBR,cTR};
		LinearRing ring = fac.createLinearRing(boundingCoords);
		geom = fac.createPolygon(ring, null);
		geog.move(this, geom);//sets the polygon onto the geography surface
		this.setCoord(geom.getCentroid().getCoordinate());

		//assign a random generator to each cell
		randomGenerator = new Random();
		
	}
	
	public void setVisibleNeigh(){
		Iterable neigh = SimUtils.getObjectsWithin(this.getCoord(), Parameter.foodSearchRange, Cell.class);
		while (neigh.iterator().hasNext()){
			visibleSites.add((Cell)neigh.iterator().next());
		}
	}
	
	
	/********************************	
	 * 								*
	 *         Step methods			*
	 * 								*
	 *******************************/
	
	public void stepThreaded(){
		
		try{
		regrow();
		}catch (NullPointerException e){
			System.out.println("problem in the regrow method");
		}
		
	}
	
	private void regrow(){
		//check to see if resource is at max levels
		if (resources!=maxResources){
			//the resource will grow back until it's maximum level is reached
			if (resources<maxResources-regrowRate){
				resources = (resources+regrowRate);
			} else {
				resources = (maxResources);
			}
			//will degenerate if above max resource level 
			if(resources>maxResources+regrowRate){
				resources = (resources-regrowRate);
			}else if (resources>maxResources && resources<maxResources+regrowRate){
				resources = maxResources;
			}
		} else {
		}
	}
	
	public synchronized double eatMe(int bite){
		double biteSize =0;
		if (this.getResourceLevel() - bite > 0){
				setResourceLevel((int) (this.getResourceLevel() - bite));
				biteSize = bite;
		}else{
			biteSize = this.getResourceLevel();
			setResourceLevel(0);
		}
		
		ModelSetup.addToCellUpdateList(this);
		
		return biteSize;
	}
	
	

	/********************************	
	 * 								*
	 *         Get/set methods		*
	 * 								*
	 *******************************/
	
	public ArrayList<Cell> getVisibleNeigh(){
		return visibleSites;
	}
	public double getResourceLevel(){
		return resources;
	}
	public void setResourceLevel(double r){
		resources = r;
	}
	public double getMaxResourceLevel(){
		return maxResources;
	}
	public void setMaxResourceLevel(double rm){
		maxResources = rm;
	}
	public Coordinate getCoord(){
		return coord;
	}
	private void setCoord(Coordinate c){
		coord = c;
	}
	public void adjustRemembered(int i){
		remembered = remembered + i;
	}
	public int getRemembered(){
		return remembered;
	}
	public void setDBH(double d){
		dbh=d;
	}
	public double getDBH(){
		return dbh;
	}
	public double getVisitCount(){
		return visit;
	}
	public void addVisitCount(int i){
		visit = visit + i;
	}
}
