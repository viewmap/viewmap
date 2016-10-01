package distance;

import java.util.LinkedList;

import graph.GraphAdapter;
import graph.Vertice;

public class DistanceProcessingUnit {
	
	private static int INF = 1000000;
	
	private GraphAdapter mGraphAdapter;
	
	private LinkedList<LinkedList<Double>> distanceTable;
	
	private LinkedList<Integer> targetArea;
	
	public DistanceProcessingUnit(GraphAdapter mGraphAdapter){
		this.mGraphAdapter = mGraphAdapter;
		initDistanceTable();
	}
	
	public LinkedList<LinkedList<Double>> getDistanceTable(){
		return distanceTable;
	}
	
	private void initDistanceTable(){
		distanceTable = new LinkedList<LinkedList<Double>>();
		for(int i = 0; i < mGraphAdapter.getNumVertices(); i++){
			distanceTable.add(new LinkedList<Double>());
		}
		targetArea = new LinkedList<Integer>();
	}
	
	public void setTargetArea(int target, double radius){
		targetArea.add(target);
		
		for(int i = 0; i < target; i++){
			if(distanceTable.get(i).get(target-i-1).doubleValue() <= radius){
				targetArea.add(new Integer(i));
			}
		}
		for(int i = 0; i < distanceTable.get(target).size(); i++){
			if(distanceTable.get(target).get(i).doubleValue() <= radius){
				targetArea.add(new Integer(target + i + 1));
			}
		}
		
	}
	
	public LinkedList<Integer> getTargetArea(){
		return targetArea;
	}
	
	public void updateDistanceTable(){
		mGraphAdapter.getGraph();
		for(int i = 0; i < mGraphAdapter.getNumVertices(); i++){
			for(int j = i; j < mGraphAdapter.getNumVertices(); j++){
				if(i == j)
					continue;
				
				Vertice v1, v2;
				
				v1 = mGraphAdapter.getGraph().get(i);
				v2 = mGraphAdapter.getGraph().get(j);
				
								
				double delX = (v1.getX()-v2.getX());
				double delY = (v1.getY()-v2.getY());
				
				double distance = Math.sqrt(Math.pow(delX, 2)+Math.pow(delY, 2));
				
				distanceTable.get(i).add(new Double(distance));
			}
		}
	}

}
