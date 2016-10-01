package io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import tree.VehicleNode;

// If the number of vehicles is n, Vehicle ID is sequentially allocated starting from 1 to n
// Time range is from 0 to (totalTime - 1)
	

public class VMDataManager {
	private int numVehicles;
	private int totalTime;
	private LinkedList<LinkedList<VehicleNode>> VehicleTrackTable;
		
	public VMDataManager(){
		VehicleTrackTable = new LinkedList<LinkedList<VehicleNode>>();
	}
	
	//getter - VehicleTrackTable, the # of Vehicles, the # of time
	public LinkedList<LinkedList<VehicleNode>> getTrackTable(){
		return VehicleTrackTable;
	}
	
	public int getNumVehicles(){
		return numVehicles;
	}
	
	public int getTotalTime(){
		return totalTime;
	}
	
	//search and retrieve vehicle with time and vehicle ID
	public VehicleNode getVehicle(int time, int vid){
		LinkedList<VehicleNode> trackTable = VehicleTrackTable.get(time);
		
		return trackTable.stream().filter(v -> v.getVid() == vid).findFirst().get();
	}
	
	//search and retrieve a list of vehicles with time
	public LinkedList<VehicleNode> getTrackTableByTime(int time){
		return VehicleTrackTable.get(time);
	}
	
	
	public boolean checkInsertable(int time){
		return (time >= 59)&&(((time + 1) % 60 == 0)||(time % 60 == 0)||((time - 1) % 60 == 0));
	}
	
	//read an input file and create total VehicleTrackTable
	//input file format		-	10 	(the # of vehicles)
	//						-	600	(simulation time)
	//						-	time	locationX	locationY	vehicle id (divided by "\t")
	public void createTrackTable(String filename){
		try {
			BufferedReader fileReader = new BufferedReader(new FileReader(filename));
			String buff;
			
			try {
				
				buff = fileReader.readLine();
				if(buff != null){
					numVehicles = Integer.parseInt(buff);
				}
				else
					System.out.println("number of vehicles read error - first line reading error");
				
				buff = fileReader.readLine();
				if(buff != null){
					totalTime = Integer.parseInt(buff);
				}
				else
					System.out.println("totalTime read error - first line reading error");
				
				//create empty vehicle lists
				for(int i = 0; i < totalTime; i++){
					VehicleTrackTable.add(new LinkedList<VehicleNode>());
				}
				
				while((buff = fileReader.readLine()) != null){					
					String[] tokensSpiltedByTab = buff.split("\t");
					
					if(!checkInsertable(Integer.parseInt(tokensSpiltedByTab[0])))
						continue;
						
					VehicleNode newVehicle = new VehicleNode(Integer.parseInt(tokensSpiltedByTab[1]));
					
					newVehicle.setLocation(Double.parseDouble(tokensSpiltedByTab[2]), Double.parseDouble(tokensSpiltedByTab[3]));
		
					VehicleTrackTable.get(Integer.parseInt(tokensSpiltedByTab[0])).add(newVehicle);
				
				}
												
			} catch (IOException e) {
				System.out.println("file read error - error in reading file");
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("file read error - File Not Found");
			e.printStackTrace();
		}		
	}
}
