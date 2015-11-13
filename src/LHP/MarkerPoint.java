package LHP;

import repast.simphony.context.Context;
import repast.simphony.space.gis.Geography;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class MarkerPoint {

	long id = 0;
	Geography geog;
	Coordinate coord;
	int month,groupID;
	Context context;
	
	
	/********************************	
	 * 								*
	 *         Initialize MP		*
	 * 								*
	 *******************************/
	
	
	public MarkerPoint(Context c,Geography g,Coordinate tag, long tagID, int m,int group) {
		geog=g;
		id = tagID;
		coord = tag;
		month=m;
		groupID=group;
		context=c;
	}
	
	public void createPoint(){
		GeometryFactory fac2 = new GeometryFactory();
		Point geom = fac2.createPoint(getCoord());
		geog.move(this, geom);
		context.add(this);
	}
	
	/********************************	
	 * 								*
	 *         Get/set methods		*
	 * 								*
	 *******************************/
	
	public void setPointID(int i){
		id = i;
	}
	public long getPointID(){
		return id;
	}
	public void setMonth(int i){
		month = i;
	}
	public long getMonth(){
		return id;
	}
	public int getDay(){
		return (int)id/26;
	}
	public Coordinate getCoord(){
		return coord;
	}
	public void setGroupID(int c){
		groupID = c;
	}
	public int getGroupID(){
		return groupID;
	}
}
