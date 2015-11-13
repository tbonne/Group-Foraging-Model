package LHP;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import repast.simphony.engine.environment.RunEnvironment;
import tools.ForagingUtils;
import tools.GroupUtils;
import tools.SimUtils;
import tools.MoveUtils;
import com.vividsolutions.jts.geom.Coordinate;

public class RedColobus extends Primate{
	
	
	
	/****************************
	 * 							*
	 * Building a red colobus 	*
	 * 							*
	 * *************************/
	
	
	//initialize a red colobus agent
	public RedColobus(int group, int ID, Coordinate c, int groupSize){
		this.id = ID;
		this.coordinate = c;
		this.safeNeighSize = ThreadLocalRandom.current().nextInt(groupSize-5, groupSize);
		this.energy = ThreadLocalRandom.current().nextInt(Parameter.targetEnergy_mean-5, Parameter.targetEnergy_mean);
		this.hungry = false;
		groupMates = new ArrayList<Primate>();
		destination = null;
	}
	
	
	/********************************************
	 * 									        *
	 * Sensory update (internal + external) 	*
	 * 									        *
	 * ******************************************/
		
	public void getInputs(){
		
		//update where the agent is on the geography and grid landscapes
		this.coordinate = ModelSetup.getAgentGeometry(this).getCoordinates()[0];

		//identify who is near me
		this.groupMates = GroupUtils.getVisibleGroupMates(this);

		//identify nearby visual patches
		if(this.myPatch!=null)this.visualPatches = myPatch.getVisibleNeigh();

		//identify whether i'm hungry or not
		if(Parameter.targetEnergy_mean > this.getEnergy()){
			this.hungry = true;
		} else {
			this.hungry = false;
		}

		//Choose destination based on current context
		//check safety
		if(GroupUtils.checkIfSafe(this,true)==false){

			//move towards safety
			destination = GroupUtils.myNearestNeighs(this);

		} else {
			
			//check if hungry
			if(this.hungry){

				//move towards food
				destination = ForagingUtils.chooseFeedingSite(this, RedColobus.class,true).getCoord();

			} else {
				//rest
			}
		}

	}

	/****************************
	 * 							*
	 * Behavioural response 	*
	 * 							*
	 * *************************/


	public void behaviouralResponse(){

		//check safety
		if(destination!=null){

			//move towards safety
			move(destination,true);

		} else {
				//rest
		}
		
		destination = null;
	}
	
	
	/************************************
	 * 					         		*
	 * 		Energy update				*
	 * 						        	*
	 * **********************************/
	
	public void energyUpdate(){
		
		//Am i hungry
		if(this.hungry==true){
			try {
				double bite = myPatch.eatMe(Parameter.rcBiteSize);
				energy = energy + bite;

				//experiencing scramble competition
				if(bite<Parameter.rcBiteSize){ 
					Watcher.addScrambleCount();
					safeNeighSizeDown();
				}
				
			}catch(NullPointerException np){
				System.out.println("can't eat, not in a resource");
			}
			
		}else{
			safeNeighSizeUp(this.groupMates.size());
		}

		//constant energy loss per step
		this.energy = this.energy-Parameter.rcEnergyLoss;

	}

	/************************************
	 * 					         		*
	 * 		Move method 				*
	 * 						        	*
	 * **********************************/
	
	private void move(Coordinate destination,boolean isCellSite){
		MoveUtils.moveTo((Primate)this, destination,isCellSite);
		this.setCoord(ModelSetup.getAgentGeometry(this).getCoordinates()[0]);
		this.myPatch = SimUtils.getCellAtThisCoord(this.getCoord(),true); //new patch
		this.myPatch.addVisitCount(1);
	}
}