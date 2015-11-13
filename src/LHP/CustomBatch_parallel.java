package LHP;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class CustomBatch_parallel {

	
	public static void main(String[] args){
		
		//convert to doubles
		Double[] r= new Double[args.length];
		for (int i=0; i< args.length;i++){
			r[i]=Double.parseDouble(args[i]);
		}
		
		//make file
		generateFile(r);
		
	}
	
	private static void generateFile(Double[] params){
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter("C:/Users/tbonne1/Dropbox/project_Agent_interaction_hetero_and_parallel/par/batchParams.xml",false));
			writer.append("<?xml version=\"1.0\"?>");
			writer.append("<sweep runs=\"1\">");
			
			writer.append("<parameter name=\"randomSeed\" type=\"constant\" constant_type=\"int\" value=\"1234\"/>");
			
			writer.append("<parameter name=\"groupSize\" type=\"constant\" constant_type=\"int\" value=\"50");
			//writer.append(Long.toString(Math.round(params[0])));
			writer.append("\"/>");
			
			writer.append("<parameter name=\"numbThreads\" type=\"constant\" constant_type=\"int\" value=\"");
			writer.append(Long.toString(Math.round(params[1])));
			writer.append("\"/>");
			
			writer.append("<parameter name=\"grouped\" type=\"constant\" constant_type=\"boolean\" value=\"TRUE");
			/*
			if(params[2]==0){
				writer.append("FALSE");
			} else {
				writer.append("TRUE");
			}*/
			
			writer.append("\"/>");
			
			writer.append("<parameter name=\"envHetero\" type=\"constant\" constant_type=\"double\" value=\"");
			writer.append(Double.toString(Math.round(params[2])));
			writer.append("\"/>");
		
			writer.append("<parameter name=\"cellSize\" type=\"constant\" constant_type=\"int\" value=\"");
			writer.append(Long.toString(Math.round(params[0])));
			writer.append("\"/>");
			
			writer.append("</sweep>");
			
			writer.flush();
		    writer.close();
		
		} catch (IOException e){				e.printStackTrace();
		}
	}
}
