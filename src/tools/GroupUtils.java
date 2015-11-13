package tools;

import java.util.ArrayList;

import LHP.Parameter;
import LHP.Primate;
import LHP.RedColobus;

import com.vividsolutions.jts.geom.Coordinate;

public final strictfp class GroupUtils {
	
	/* Method list:
	 * 		(1.0)	checkIfSafe() 			Are there >= desiered neighbours nearby
	 * 		(1.1)	checkIfSafe() 			Are there >= desiered neighbours nearby (from the point of view of a prospective destination)
	 * 		(2) 	myNearestNeighs() 		Calculates the center of my nearest neighbours
	 * 		(3) 	getGroupCenter() 		Calculates the geometric center of the group
	 * 		(4) 	getVisibleGroupMates() 	Updates a list of visible primates
	 */
	
	//(1.0)
	public static <T> boolean checkIfSafe(T agent, boolean ConsiderOnlyNeigh){ //checks to see if there is enough neighbours with safe radius

		boolean retval = false;
		int numbPrimatesNearBy=0;
		ArrayList<Primate> primatesInArea = new ArrayList<Primate>();

			//find primates within safe radius
			for(Primate p : ((Primate)agent).getGroupMates() ){
			if(p.getCoord().distance(((Primate)agent).getCoord())<Parameter.safeRadius)primatesInArea.add(p);
			}
			
			//count the number of group mates within my safe radius 
			try{
				for (Primate p : primatesInArea){
					if (((Primate) p).getGroupID()==((Primate)agent).getGroupID()&& ((Primate)p).getId()!=((Primate)agent).getId())numbPrimatesNearBy++;
				}
			}catch(NullPointerException e){
				//System.out.println("Warrning: no nearby neighbours found");
			}

			//if there is enough, then return value is true
			if(numbPrimatesNearBy>=((Primate)agent).getSafeNeighSize()){
				retval = true;
			}
			
		return retval;
	}
	
	//(1.1)
	public static <T> boolean checkIfSafe(T agent, Coordinate myFutureP, Class<T> clazz){ //checks to see if there is enough neighbours with safe radius from a cell site

		boolean retval = false;
		int numbPrimatesNearBy=0;
		ArrayList<Primate> primatesInArea = new ArrayList<Primate>();

			//find primates within safe radius
			for(Primate p : ((Primate)agent).getGroupMates() ){
			if(p.getCoord().distance(((Primate)agent).getCoord())<Parameter.safeRadius)primatesInArea.add(p);
			}
			
			//count the number of group mates within my safe radius
			try{
				for (Primate p : primatesInArea){
					if (((Primate) p).getGroupID()==((Primate)agent).getGroupID()&& ((Primate)p).getId()!=((Primate)agent).getId())numbPrimatesNearBy++;
				}
			}catch(NullPointerException e){
				//System.out.println("Warrning: no nearby neighbours found");
			}

			//if there is enough, then return value is true
			if(numbPrimatesNearBy>=((Primate)agent).getSafeNeighSize()){
				retval = true;
			}
			
			//migrating individual not considered safe until they find a group
			if(((Primate)agent).getId()==-1)retval=false;

		return retval;
	}
	
	
	//(2)
	public static <T> Coordinate myNearestNeighs(T agent){//calculates the center of a group of my nearest neighbours: used when primate is not considered safe
		ArrayList<Primate> closestP = new ArrayList<Primate>();
		double minDist=99999999;
		Coordinate c = null;
		Primate pClose=null;
		
		//get all visible neighbours
		ArrayList<Primate> allNeigh = ((Primate)agent).getGroupMates(); 
		
		//get the only the X closest neighbours: X = safeNeighSize
		if (allNeigh.size()>0 && allNeigh.size()>((Primate)agent).getSafeNeighSize()){
			
			while (closestP.size()<((Primate)agent).getSafeNeighSize() && allNeigh.size()>0){
				minDist = 9999999;
				pClose=null;
				
				//take subset of primate group
				for (Primate p: allNeigh){
					double dist = ((Primate)agent).getCoord().distance(p.getCoord());
					if (closestP.contains(p)==false && dist < minDist && p.getId()!=((Primate)agent).getId()){
						minDist = dist;
						pClose = p;
					}
				}

				allNeigh.remove(pClose);

				//add closest neighbour to group
				if (pClose!=null)closestP.add(pClose);
			}
			//else use all visible neighbours
		} else {
			closestP = allNeigh;
		}

		//find the center of this group (closestP)
		double xCoordAvg=0;
		double yCoordAvg=0;
		double numbPrimates=0;

		if (closestP.size()>0){
			
		//calculate their average x and y coordinates
		for (Primate p: closestP){
			if (p.getGroupID()==((Primate)agent).getGroupID()){
				xCoordAvg = xCoordAvg + p.getCoord().x;
				yCoordAvg = yCoordAvg + p.getCoord().y;
				numbPrimates++;
			}
		}

		xCoordAvg = xCoordAvg/numbPrimates;
		yCoordAvg = yCoordAvg/numbPrimates;

		//return the center coordinate
		c = new Coordinate(xCoordAvg,yCoordAvg);
		
		} else { //no nearby neighbours
			
		}
		
		
		if (c == null){
			//System.out.println("I can't find any near-by neigh! - groupUtils class, myNearestNeighs    -  " + ((Primate)agent).getGroupID());
			//c = new Coordinate(((Primate)agent).getCoord().x+((Primate)agent).getRand().nextDouble()*Parameter.maxDistancePerStep,((Primate)agent).getCoord().y+((Primate)agent).getRand().nextDouble()*Parameter.maxDistancePerStep);
		}

		return c;
	}
	
	//(3)
	public static <T> Coordinate getGroupCenter(Iterable<T> primates){
		
		//calculate group center
		double averageX=0;
		double averageY=0;
		int numberOfInd=0;
		
		for (T pri:primates){
			averageX = averageX + ((Primate)pri).getCoord().x;
			averageY = averageY + ((Primate)pri).getCoord().y;
			numberOfInd++;
		}
		
		averageX = averageX/numberOfInd;
		averageY = averageY/numberOfInd;
		
		Coordinate center = new Coordinate(averageX,averageY);
		
		return center;
		
	}
	
	//(4)
	public static ArrayList<Primate> getVisibleGroupMates(Primate primate){
		ArrayList<Primate> groupMates = new ArrayList<Primate>();
		ArrayList<Primate> aroundMe = SimUtils.copyIterable(SimUtils.getObjectsWithin(primate.getCoord(), Parameter.primateSearchRange, RedColobus.class));
		for (Primate p : aroundMe){
			if(p.getGroupID()==primate.getGroupID())groupMates.add(p);
		}
		return groupMates;
	}
	
	
}
