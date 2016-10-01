package tree;

import java.util.LinkedList;

public class VehicleNode {
	
	double distanceFromPredLoc;
	
	int time;
	int vid;
	double probabilty = 0.0;
	int prevVid;
	int doublePrevVid;
	LinkedList<VehicleNode> children;
	boolean noneInCandidates = false;
	
	private double x;
	private double y;
	
	public void setNoneInCandidates(boolean b){
		noneInCandidates = b;
	}
	
	public boolean getNoneInCandidates(){
		return noneInCandidates;
	}
	
	public void setVid(int vid){
		this.vid = vid;
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public void setLocation(double x, double y){
		this.x = x; this.y = y;
	}	
	
	public void setTime(int time){
		this.time = time;
	}
	
	public int getTime(){
		return time;
	}
	
	public VehicleNode(int vid){
		this.vid = vid;
		children = new LinkedList<VehicleNode>();
	}
	
	public void setDistanceFromPred(double distance){
		distanceFromPredLoc = distance;
	}
	
	public double getDistanceFromPred(){
		return distanceFromPredLoc;
	}
	
	public LinkedList<VehicleNode> getChildren(){
		return children;
	}
	
	public int getVid(){
		return vid;
	}
	
	public int getPrevVid(){
		return prevVid;
	}
	
	public void setPrevVid(int prevVid){
		this.prevVid = prevVid; 
	}
	
	public int getDoublePrevVid(){
		return doublePrevVid;
	}
	
	public void setDoublePrevVid(int doublePrevVid){
		this.doublePrevVid = doublePrevVid; 
	}
	
	public double getProbability(){
		return probabilty;
	}
	
	public void setProbability(double probability){
		this.probabilty = probability;
	}
	
	public void addChild(VehicleNode child){
		children.add(child);
	}
	
}	
