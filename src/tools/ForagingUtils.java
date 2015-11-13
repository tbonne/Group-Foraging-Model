package tools;

import LHP.Cell;
import LHP.Parameter;
import LHP.Primate;
import tools.GroupUtils;

import com.vividsolutions.jts.geom.Coordinate;


public final strictfp class ForagingUtils {

	/* Method list:
	 * 		(1)	choosefeedingSite()							- compares visual and remembered sites, chooses best one
	 * 		(2) findBestUnknownFoodSource()					- finds best visual site
	 * 		(3)	chooseFoodSourceTowardsMyMigrationGoal()	- finds best site on way to my chosen site
	 */



	//(1)
	//Compares and finds the best resource site, either from memory or from visible sites.
	public static <T extends Primate> Cell chooseFeedingSite(T agent, Class<T> clazz,boolean topo){

		//System.out.println("choosing feeding site");
		Cell bestNearMe = findBestUnknownFoodSource(((Primate)agent));//find best nearby food source

		if(GroupUtils.checkIfSafe(agent,bestNearMe.getCoord(),clazz)==true){
			return bestNearMe;
		}else{
			Cell resTowardsGoal = chooseFoodSourceTowardsMyMigrationGoal(((Primate)agent),bestNearMe.getCoord(),true);
			if(resTowardsGoal!=null){  
				return resTowardsGoal;
			}else{
				return ((Primate)agent).getMyPatch();
			}
		}
	}

	//(2)
	//finds the best resource that the agent can "see"
	public static Cell findBestUnknownFoodSource(Primate primate){//find the best unknown resource; all resources within search radius

		double dist = 99999;
		double resourceWeight = 99999;
		double minResourceWeight = 9999;
		Cell bestCloseSite = null;

		//Calculate each resources food weight and choose the lowest one (weight = distance / food)
		for (Cell r:primate.getVisualPatches()){

			dist = primate.getCoord().distance(r.getCoord());
			if (dist<Parameter.cellSize)dist=Parameter.cellSize;

			resourceWeight=dist/r.getResourceLevel();
			if(resourceWeight<minResourceWeight){
				minResourceWeight = resourceWeight;
				bestCloseSite = r;
			}
		}

		return bestCloseSite;
	}


	//(3)
	public static Cell chooseFoodSourceTowardsMyMigrationGoal(Primate agent,Coordinate bestRes, boolean considerFood){//in case the primate has a goal to migrate towards; chose best site in that direction

		Cell towardsMyGoal=null;
		boolean found=false;

		double dist=9999,newDist=9999,resourceWeight=9999;
		double minResourceWeight=99999;
		Cell bestCloseSite=null;

		Iterable<Cell> primateFood = agent.getVisualPatches();
		int count = 0;

		//choose the "best" site; distance vs food + is safe
		for (Cell r:primateFood){


			if(agent.getCoord().distance(r.getCoord())<=Parameter.foodSearchRange){
				count++;
				dist = agent.getCoord().distance(bestRes);
				newDist = r.getCoord().distance(bestRes);

				if (agent.getCoord().distance(r.getCoord()) <= Parameter.foodSearchRange && dist>newDist){ 
					resourceWeight=(r.getCoord().distance(bestRes))/r.getResourceLevel();//choose safe site that is the closest to the migration goal\
					if(considerFood==false)resourceWeight=r.getCoord().distance(bestRes);
					if(resourceWeight<minResourceWeight){
						minResourceWeight = resourceWeight;
						bestCloseSite = r;
						found=true;
					}
				}
			}
		}
		if (found == true)towardsMyGoal=bestCloseSite;//return site; if a acceptable site has been chosen


		if(towardsMyGoal==null){
			towardsMyGoal = chooseFoodSourceTowardsMyMigrationGoal(agent,bestRes,false); //simply closest safe site towards my target
		}

		return towardsMyGoal;
	}


}
