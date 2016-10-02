package testroutine;

import java.util.LinkedList;

import distance.DistanceProcessingUnit;
import distance.ShortestPathProcessingUnit;
import graph.GraphAdapter;

public class NodeSelector {
	
	private DistanceProcessingUnit mDistanceProcessingUnit;
	private ShortestPathProcessingUnit mShortestPathProcessingUnit;
	private GraphAdapter mGraphAdapter;
	private int TrustNode, TargetNode;
	private double TargetRadius;
	private LinkedList<LinkedList<Integer>> hopTable;
	private LinkedList<int[]> maxHopVids;
	
	public void init(GraphAdapter mGraphAdapter, double TargetRadius){
		mDistanceProcessingUnit = new DistanceProcessingUnit(mGraphAdapter);
		
		this.mGraphAdapter = mGraphAdapter;
		
		this.TargetRadius = TargetRadius;

	}
	
	public void destroyShortestPathPU(){
		mShortestPathProcessingUnit = null;
	}
	
	public void updateHopTable(){
		mShortestPathProcessingUnit.setHopTable(hopTable);
	}
	
	public void initHopTable(){
		
		mShortestPathProcessingUnit =  new ShortestPathProcessingUnit(mGraphAdapter);
		
		maxHopVids = new LinkedList<int[]>();
		
		hopTable = new LinkedList<LinkedList<Integer>>();
		for(int i = 0; i < mGraphAdapter.getNumVertices(); i++){
			hopTable.add(new LinkedList<Integer>());
		} 
	}
	
	public void updateDistanceTable(){
		mDistanceProcessingUnit.updateDistanceTable();
	}
	
	public void setTargetArea(){
		mDistanceProcessingUnit.setTargetArea(TargetNode, TargetRadius);
	}
	
	public LinkedList<Integer> getTargetArea(){
		return mDistanceProcessingUnit.getTargetArea();
	}
	
	public void chooseTrustNodeTargetNode(){
		
		double max = 0;
		
		int maxHops;
		
		int t1, t2;
		
		for(t1 = 0; t1 < mGraphAdapter.getNumVertices(); t1++){
			for(t2 = 0; t2 < mDistanceProcessingUnit.getDistanceTable().get(t1).size(); t2++){
				
				if(mDistanceProcessingUnit.getDistanceTable().get(t1).get(t2).doubleValue() >= max){
					TrustNode = t1; TargetNode = t1 + t2 + 1;
					max = mDistanceProcessingUnit.getDistanceTable().get(t1).get(t2).doubleValue();
				}
			}
		}
		
		System.out.println("max distance = "+max);
		System.out.println("TrustNode = "+TrustNode);
		System.out.println("TargetNode = "+TargetNode);
		
		LinkedList<Integer> neighborsOfMax = new LinkedList<Integer>();
		
		neighborsOfMax = mGraphAdapter.getGraph().get(TargetNode).getNeighborVids();
		
		maxHops = hopTable.get(TrustNode).get(TargetNode-TrustNode-1);
		
		System.out.println(TargetNode
				+" - "+mDistanceProcessingUnit.getDistanceTable().get(TrustNode).get(TargetNode-TrustNode-1).doubleValue()
				+" - "+hopTable.get(TrustNode).get(TargetNode-TrustNode-1).intValue());
		
		for(int i = 0; i < neighborsOfMax.size(); i++){
			int neighborVid = -1;
			int src = -1, dest = -1;
			neighborVid = neighborsOfMax.get(i).intValue();

			if(TrustNode > neighborsOfMax.get(i).intValue()){
				src = neighborVid; dest = TrustNode-neighborVid-1;
			}
			else{
				src = TrustNode; dest = neighborVid-TrustNode-1;
			}
			
			System.out.println(neighborsOfMax.get(i).intValue()
					+" - "+mDistanceProcessingUnit.getDistanceTable().get(src).get(dest).doubleValue()
					+" - "+hopTable.get(src).get(dest).intValue());
			
			if(hopTable.get(src).get(dest).intValue() > maxHops){
				TargetNode = neighborsOfMax.get(i).intValue();
				maxHops = hopTable.get(src).get(dest);
			}
		}
		System.out.println();
		if(TrustNode < TargetNode)
			System.out.println("max distance = "+mDistanceProcessingUnit.getDistanceTable().get(TrustNode).get(TargetNode - TrustNode -1));
		else
			System.out.println("max distance = "+mDistanceProcessingUnit.getDistanceTable().get(TargetNode).get(TrustNode - TargetNode -1));
		
		System.out.println("max hops = "+maxHops);
		System.out.println("TrustNode = "+TrustNode);
		System.out.println("TargetNode = "+TargetNode);
	}
	
	//for the Number of Trust Nodes Test
	public void setTargetNode(int TargetNode){
		this.TargetNode = TargetNode;
	}
	
	//for the Number of Trust Nodes Test
	public void setTargetAreaByInput(LinkedList<Integer> TargetArea){
		
	}
	
	
	public int getTrustNode(){
		return TrustNode;
	}
	
	public int getTargetNode(){
		return TargetNode;
	}
}
 