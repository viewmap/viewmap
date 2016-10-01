package attack;

import java.util.HashSet;
import java.util.LinkedList;

import graph.GraphAdapter;

public class NormalIndividualAttack {
	
	private GraphAdapter mAttackGraphAdapter;
	
	private HashSet<Integer> attackerSet;
	private int[] attackerVidIntArray;
	
	private int numLegitimateVertices;
	private LinkedList<Integer> FakeArea;
	
	public NormalIndividualAttack(GraphAdapter mAttackGraphAdapter, HashSet<Integer> attackerSet){
		this.mAttackGraphAdapter = mAttackGraphAdapter;
		this.attackerSet = attackerSet;
		attackerVidIntArray = new int[attackerSet.size()];
		numLegitimateVertices = mAttackGraphAdapter.getNumVertices();
	}
	
	public void setGraphByAttackType(){
		
		parseAttackSet();
		
		addAttackEdges();
		
		markAttackerSign();
	}
	
	
	private void addAttackEdges(){
		int fakeNodeVid = mAttackGraphAdapter.getNumVertices();
		int i, j;
		
		int numOfFakeNodes = 4;
		
		for(i = 0; i < attackerSet.size(); i++){
			
			for(j = 0; j < numOfFakeNodes; j++){
				mAttackGraphAdapter.addNode(fakeNodeVid+j);
				FakeArea.add(new Integer(fakeNodeVid+j));
			}
			
			for(j = 0; j < numOfFakeNodes; j++){
				mAttackGraphAdapter.addEdge(attackerVidIntArray[i], fakeNodeVid+j);
				mAttackGraphAdapter.addEdge(fakeNodeVid+j, fakeNodeVid+((j+1)%numOfFakeNodes));

			}
			
			fakeNodeVid = fakeNodeVid + j;
		
		}
	}
	
	public void setFakeArea(LinkedList<Integer> FakeArea){
		this.FakeArea = FakeArea;
	}
	
	public void markAttackerSign(){
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
