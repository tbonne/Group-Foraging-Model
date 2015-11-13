package LHP;

import java.util.ArrayList;

import tools.GroupUtils;
import tools.SimUtils;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Primate {

	//primate level variables
	Coordinate coordinate;
	ArrayList<Primate> groupMates;
	ArrayList<Cell> visualPatches;
	Cell myPatch;
	int groupId;
	int id;
	double energy;
	boolean hungry;
	int safeNeighSize;
	RandomHelper rand;
	Coordinate destination=null;


	/************************************************
	 * 							           			*
	 * All primate will have the following methods 	*
	 * 												*
	 ************************************************/
	
	//update sensory variables
	public void getInputs(){

	}

	//Action phase
	public void behaviouralResponse(){

	}

	//energy update
	public void energyUpdate(){

	}


	/****************************
	 * 							*
	 * Get and set parameters	*
	 * 							*
	 ****************************/

	public void safeNeighSizeUp(int visiblePrimates){
		if (safeNeighSize+1<visiblePrimates)safeNeighSize=safeNeighSize+1;
	}
	public void safeNeighSizeDown(){
		if (safeNeighSize-1>=0)safeNeighSize=safeNeighSize-1;
	}
	public int getGroupID(){
		return groupId;
	}
	public void setGroupID(int id){
		groupId = id;
	}
	public int getId(){
		return id;
	}
	public int getSafeNeighSize(){
		return safeNeighSize;
	}
	public void setSafeNeighSize(int d){
		safeNeighSize = d;
	}
	public double getEnergy(){
		return energy;
	}
	public void setEnergy(double d){
		energy = (int)d;
	}
	public void setCoord(Coordinate c){
		coordinate = c;
	}
	public Coordinate getCoord(){
		return coordinate;
	}
	public Cell getMyPatch(){
		return myPatch;		
	}
	public void setMyPatch(Cell c){
		myPatch = c;
	}
	public ArrayList<Primate> getGroupMates(){
		return groupMates;
	}
	public ArrayList<Cell> getVisualPatches(){
		return visualPatches;
	}
	public RandomHelper getRand() {
		return rand;
	}
	public void setRand(RandomHelper rand) {
		rand = rand;
	}
}
