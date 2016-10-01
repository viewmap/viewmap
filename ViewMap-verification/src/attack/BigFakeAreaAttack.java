package attack;

import java.util.HashSet;
import java.util.LinkedList;

import graph.GraphAdapter;
import graph.Vertice;
import testroutine.Constants;

public class BigFakeAreaAttack {
	private GraphAdapter mAttackGraphAdapter;

	private HashSet<Integer> attackerSet;
	private int[] attackerVidIntArray;

	private int numLegitimateVertices;
	private LinkedList<Integer> FakeArea;

	public BigFakeAreaAttack(GraphAdapter mAttackGraphAdapter, HashSet<Integer> attackerSet) {
		this.mAttackGraphAdapter = mAttackGraphAdapter;
		this.attackerSet = attackerSet;
		attackerVidIntArray = new int[attackerSet.size()];
		numLegitimateVertices = mAttackGraphAdapter.getNumVertices();
	}

	public void setGraphByAttackType(int multiples, int flag) {

		parseAttackSet();

		copyGraph(multiples);

		if (flag == Constants.NORMAL_FLAG)
			advancedAddNormalAttackEdges();

		if (flag == Constants.VIEWMAP_FLAG)
			advancedAddAttackEdges(multiples);

		markAttackerSign();
	}

	public void setGraphByAttackType(int multiples) {

		parseAttackSet();

		//copyGraph(multiples);

		//copyGraph();
		
		multipleNodes(multiples);
		
		//advancedAddAttackEdges(multiples);

		fixAddAttackEdges();
				
		markAttackerSign();
	}

	public void setFakeArea(LinkedList<Integer> TargetArea, LinkedList<Integer> FakeArea, int multiples) {
		this.FakeArea = FakeArea;
		for (int m = 0; m < multiples; m++) {
			for (int i = 0; i < TargetArea.size(); i++)
				FakeArea.add(new Integer(TargetArea.get(i).intValue() + numLegitimateVertices * (m + 1)));
		}

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

	private void copyGraph(int multiples) {
		int num_vertices = mAttackGraphAdapter.getNumVertices();

		for (int i = 0; i < num_vertices * multiples; i++) {
			int newVid = num_vertices + i;

			mAttackGraphAdapter.addNode(newVid);
			mAttackGraphAdapter.getGraph().get(newVid).setX(mAttackGraphAdapter.getGraph().get(newVid-num_vertices).getX());
			mAttackGraphAdapter.getGraph().get(newVid).setY(mAttackGraphAdapter.getGraph().get(newVid-num_vertices).getY());
		}

		for (int i = 0; i < num_vertices * multiples; i++) {

			Vertice vertice = mAttackGraphAdapter.getGraph().get(i % num_vertices);
			int newVid1 = num_vertices + i;

			for (int j = 0; j < vertice.getNeighborVids().size(); j++) {

				int newVid2 = num_vertices * ((num_vertices + i) / num_vertices)
						+ vertice.getNeighborVids().get(j).intValue();
				mAttackGraphAdapter.addEdge(newVid1, newVid2);
			}
		}
	}
	
	private void multipleNodes(int multiples){
		int num_vertices = mAttackGraphAdapter.getNumVertices();

		for (int i = 0; i < num_vertices * multiples; i++) {
			int newVid = num_vertices + i;

			mAttackGraphAdapter.addNode(newVid);
			mAttackGraphAdapter.getGraph().get(newVid).setX(mAttackGraphAdapter.getGraph().get(newVid-num_vertices).getX());
			mAttackGraphAdapter.getGraph().get(newVid).setY(mAttackGraphAdapter.getGraph().get(newVid-num_vertices).getY());
		}

		for (int i = 0; i < num_vertices * multiples; i++) {

			Vertice vertice = mAttackGraphAdapter.getGraph().get(i % num_vertices);
			int newVid1 = num_vertices + i;

			for (int j = 0; j < vertice.getNeighborVids().size(); j++) {

				int newVid2 = num_vertices * ((num_vertices + i) / num_vertices)
						+ vertice.getNeighborVids().get(j).intValue();
				mAttackGraphAdapter.addEdge(newVid1, newVid2);
			}
		}
		
		for (int i = 0; i < multiples - 1; i++){
			for(int j = 0; j < num_vertices; j++){
				mAttackGraphAdapter.addEdge(numLegitimateVertices*(i+1)+j, numLegitimateVertices*(i+2)+j);
			}
		}
		
	}
	
	private void fixAddAttackEdges() {
			for (int i = 0; i < attackerVidIntArray.length; i++) {
				mAttackGraphAdapter.addEdge(attackerVidIntArray[i],
						numLegitimateVertices + attackerVidIntArray[i]);

				LinkedList<Integer> attackNeighbors = mAttackGraphAdapter.getGraph()
						.get(numLegitimateVertices + attackerVidIntArray[i]).getNeighborVids();

				if ((attackNeighbors.size()/5) > 0) {
					for (int k = 0; k < attackNeighbors.size()/5; k++) {
						mAttackGraphAdapter.addEdge(attackerVidIntArray[i], attackNeighbors.get(k).intValue());
					}
				} else {
					for (int k = 0; k < attackNeighbors.size(); k++) {
						mAttackGraphAdapter.addEdge(attackerVidIntArray[i], attackNeighbors.get(k).intValue());
					}
				}
			}
		

	}
	
	private void advancedAddAttackEdges(int multiples) {

		for (int m = 0; m < multiples; m++) {
			for (int i = 0; i < attackerVidIntArray.length; i++) {
				mAttackGraphAdapter.addEdge(attackerVidIntArray[i],
						numLegitimateVertices * (m + 1) + attackerVidIntArray[i]);

				LinkedList<Integer> attackNeighbors = mAttackGraphAdapter.getGraph()
						.get(numLegitimateVertices * (m + 1) + attackerVidIntArray[i]).getNeighborVids();

				if ((attackNeighbors.size() / 3) > 0) {
					for (int k = 0; k < attackNeighbors.size() / 3; k++) {
						mAttackGraphAdapter.addEdge(attackerVidIntArray[i], attackNeighbors.get(k).intValue());
					}
				} else {
					for (int k = 0; k < attackNeighbors.size(); k++) {
						mAttackGraphAdapter.addEdge(attackerVidIntArray[i], attackNeighbors.get(k).intValue());
					}
				}
			}
		}

	}

	private void advancedAddNormalAttackEdges() {

		for (int i = 0; i < attackerVidIntArray.length; i++) {
			for (int k = 0; k < FakeArea.size(); k++) {
				mAttackGraphAdapter.addEdge(attackerVidIntArray[i], FakeArea.get(k).intValue());
			}

		}

	}

	private void markAttackerSign() {
		for (int j = 0; j < attackerVidIntArray.length; j++) {
			mAttackGraphAdapter.getGraph().get(attackerVidIntArray[j]).assignAttacker(true);
		}

		for (int k = numLegitimateVertices; k < mAttackGraphAdapter.getNumVertices(); k++) {
			mAttackGraphAdapter.getGraph().get(k).assignAttacker(true);
		}
	}

	private void parseAttackSet() {
		String[] attackVidArrayTemp1 = attackerSet.toString().split("\\[");
		String[] attackVidArrayTemp2 = attackVidArrayTemp1[1].split("\\]");
		String[] attackVidArray = attackVidArrayTemp2[0].split(", ");

		for (int i = 0; i < attackVidArray.length; i++) {
			attackerVidIntArray[i] = Integer.parseInt(attackVidArray[i]);
		}
	}
}
