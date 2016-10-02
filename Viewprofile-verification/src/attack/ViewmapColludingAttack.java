package attack;

import java.util.HashSet;
import java.util.LinkedList;

import graph.GraphAdapter;
import graph.Vertice;

public class ViewmapColludingAttack {
	private GraphAdapter mAttackGraphAdapter;
	
	private HashSet<Integer> attackerSet;
	private int[] attackerVidIntArray;
	
	private int numLegitimateVertices;
	private LinkedList<Integer> FakeArea;
	
	public ViewmapColludingAttack(GraphAdapter mAttackGraphAdapter, HashSet<Integer> attackerSet){
		this.mAttackGraphAdapter = mAttackGraphAdapter;
		this.attackerSet = attackerSet;
		attackerVidIntArray = new int[attackerSet.size()];
		numLegitimateVertices = mAttackGraphAdapter.getNumVertices();
	}
	
	public void setGraphByAttackType(){
		
		parseAttackSet();
		
		copyGraph();
				
		advancedAddAttackEdges();
		
		//addAttackEdges();
		
		//addOneEdge();
		//addCopyAttackEdges();
		markAttackerSign();
	}
	
	public void setFakeArea(LinkedList<Integer> TargetArea, LinkedList<Integer> FakeArea){
		this.FakeArea = FakeArea;
		for(int i = 0; i < TargetArea.size(); i++)
			FakeArea.add(new Integer(TargetArea.get(i).intValue()+numLegitimateVertices));
	}
	
	private void copyGraph(){
		int num_vertices = mAttackGraphAdapter.getNumVertices();
		
		for(int i = 0; i < num_vertices; i++){
			int newVid = num_vertices + i;
			
			mAttackGraphAdapter.addNode(newVid);
			
		}
		
		for(int i = 0; i < num_vertices; i++){
			
			Vertice vertice = mAttackGraphAdapter.getGraph().get(i);
			int newVid1 = num_vertices + i;
			
			for(int j = 0; j < vertice.getNeighborVids().size(); j++){
				
				int newVid2 = num_vertices + vertice.getNeighborVids().get(j).intValue();
				mAttackGraphAdapter.addEdge(newVid1, newVid2);
			}
		}
	}
	

	private void advancedAddAttackEdges(){
		
		for(int i = 0; i < attackerVidIntArray.length; i++){
			mAttackGraphAdapter.addEdge(attackerVidIntArray[i], numLegitimateVertices+attackerVidIntArray[i]);
		
			LinkedList<Integer> attackNeighbors = mAttackGraphAdapter.getGraph().
					get(numLegitimateVertices+attackerVidIntArray[i]).getNeighborVids();
			
			for(int k = 0; k < attackNeighbors.size()/3; k++){
				mAttackGraphAdapter.addEdge(attackerVidIntArray[i], attackNeighbors.get(k).intValue());
			}
		
		}
	}
	
	private void addOneEdge(){
		for(int i = 0; i < attackerVidIntArray.length; i++){
			mAttackGraphAdapter.addEdge(attackerVidIntArray[i], numLegitimateVertices+attackerVidIntArray[i]);
		}
	}
	
	private void addCopyAttackEdges() {
		int numCopyNodes = 1000;

		int fakeNodeVid = mAttackGraphAdapter.getNumVertices();
		
		for (int i = 0; i < attackerVidIntArray.length; i++) {
			
			//System.out.println(attackerVidIntArray.length);

			
			int offset = 0;
			
			mAttackGraphAdapter.addEdge(numLegitimateVertices + attackerVidIntArray[i], attackerVidIntArray[i]);
			
			for (int j = 0; j < numCopyNodes; j++) {
				mAttackGraphAdapter.addNode(fakeNodeVid + offset);
				mAttackGraphAdapter.addEdge(attackerVidIntArray[i], fakeNodeVid + offset);
				
				LinkedList<Integer> mainMapAttackNeighbors = mAttackGraphAdapter.getGraph().get(attackerVidIntArray[i])
						.getNeighborVids();

				for (int k = 0; k < mainMapAttackNeighbors.size(); k++) {
					if(mainMapAttackNeighbors.get(k).intValue() < numLegitimateVertices)
						mAttackGraphAdapter.addEdge(mainMapAttackNeighbors.get(k).intValue(), fakeNodeVid + offset);
				}
				
				offset++;

				mAttackGraphAdapter.addNode(fakeNodeVid + offset);
				mAttackGraphAdapter.addEdge(numLegitimateVertices + attackerVidIntArray[i], fakeNodeVid + offset);

				LinkedList<Integer> fakeMapAttackNeighbors = mAttackGraphAdapter.getGraph()
						.get(numLegitimateVertices + attackerVidIntArray[i]).getNeighborVids();

				for (int k = 0; k < fakeMapAttackNeighbors.size(); k++) {
					if(fakeMapAttackNeighbors.get(k).intValue() < numLegitimateVertices)
						mAttackGraphAdapter.addEdge(fakeMapAttackNeighbors.get(k).intValue(), fakeNodeVid + offset);
				}
				
				offset++;
				
				mAttackGraphAdapter.addEdge(fakeNodeVid + (offset - 1), fakeNodeVid + (offset - 2));
				//System.out.print(j+",");
			}

			fakeNodeVid = fakeNodeVid + numCopyNodes;

		}
		//System.out.println(attackerVidIntArray.length);

	}
	
	private void addAttackEdges(){
		int numCopyNodes = 10;
		
		int fakeNodeVid = mAttackGraphAdapter.getNumVertices();
		
		for(int i = 0; i < attackerVidIntArray.length; i++){
						
			for(int j = 0; j < numCopyNodes; j++){
				mAttackGraphAdapter.addNode(fakeNodeVid+j);
				
				mAttackGraphAdapter.addEdge(attackerVidIntArray[i], fakeNodeVid+j);
				mAttackGraphAdapter.addEdge(numLegitimateVertices+attackerVidIntArray[i], fakeNodeVid+j);
				
				LinkedList<Integer> attackNeighbors = mAttackGraphAdapter.getGraph().
						get(numLegitimateVertices+attackerVidIntArray[i]).getNeighborVids();
				
				//2771 - 4000, 3789 - 4000, ...
				for(int k = 0; k < attackNeighbors.size(); k++){
					mAttackGraphAdapter.addEdge(attackNeighbors.get(k).intValue(), fakeNodeVid+j);
				}
				
			}
			
			fakeNodeVid = fakeNodeVid + numCopyNodes;
		}
		
	}
	
	private void markAttackerSign(){
		for(int j = 0; j < attackerVidIntArray.length; j++){
			mAttackGraphAdapter.getGraph().get(attackerVidIntArray[j]).assignAttacker(true);
		}
		
		for(int k = numLegitimateVertices; k < mAttackGraphAdapter.getNumVertices(); k++){
			mAttackGraphAdapter.getGraph().get(k).assignAttacker(true);
		}
	}
	
	private void parseAttackSet(){
		String[] attackVidArrayTemp1 = attackerSet.toString().split("\\[");
		String[] attackVidArrayTemp2 = attackVidArrayTemp1[1].split("\\]");
		String[] attackVidArray = attackVidArrayTemp2[0].split(", ");
		
		for(int i = 0; i < attackVidArray.length; i++){
			attackerVidIntArray[i] = Integer.parseInt(attackVidArray[i]);
		}
	}
}
