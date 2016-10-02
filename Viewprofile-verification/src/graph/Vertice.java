package graph;

import java.util.*;

public class Vertice {
	private int verticeId;
	private LinkedList<Integer> neighborVids;

	private boolean attacker = false;
	private double trustScore = 0.0;
	
	private double x;
	private double y;
	
	private boolean visited = false;

	public Vertice(int vid) {
		verticeId = vid;
		
		initNeighborVids();
		
	}
	
	public boolean getVisited(){
		return visited;
	}
	
	public void setVisited(boolean visited){
		this.visited = visited;
	}
	
	public void setX(double x){
		this.x = x;
	}
	
	public void setY(double y){
		this.y = y;
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}

	public double getTrustScore(){
		return trustScore;
	}

	public void assignAttacker(boolean answer){
		attacker = answer;
	}
	
	public void assignTrustScore(double trustScore){
		this.trustScore = trustScore;
	}
	
	public boolean isAttacker(){
		return attacker;
	}
	
	public LinkedList<Integer> getNeighborVids(){
		return neighborVids;
	}
	
	private void initNeighborVids(){
		neighborVids = new LinkedList<Integer>();
	}

	public void addNeighbor(int vid){
		boolean result = false;
		
		for(int i = 0; i < neighborVids.size(); i++){
			if(neighborVids.get(i).intValue() == vid){
				result = true;
				break;
			}
		}
		
		if(!result)
			neighborVids.add(new Integer(vid));
	}
}
