package attack;

import java.util.HashSet;
import java.util.LinkedList;

import graph.GraphAdapter;
import graph.Vertice;

public class NormalColludingAttack {
	private GraphAdapter mAttackGraphAdapter;

	private HashSet<Integer> attackerSet;
	private int[] attackerVidIntArray;

	private int numLegitimateVertices;
	private LinkedList<Integer> FakeArea;

	public NormalColludingAttack(GraphAdapter mAttackGraphAdapter, HashSet<Integer> attackerSet) {
		this.mAttackGraphAdapter = mAttackGraphAdapter;
		this.attackerSet = attackerSet;
		attackerVidIntArray = new int[attackerSet.size()];
		numLegitimateVertices = mAttackGraphAdapter.getNumVertices();
	}

	public void setGraphByAttackType() {

		parseAttackSet();

		copyGraph();

		addAttackEdges();

		markAttackerSign();
	}

	public void setFakeArea(LinkedList<Integer> TargetArea, LinkedList<Integer> FakeArea) {
		this.FakeArea = FakeArea;
		for (int i = 0; i < TargetArea.size(); i++)
			FakeArea.add(new Integer(TargetArea.get(i).intValue() + numLegitimateVertices));
	}

	private void copyGraph() {
		int num_vertices = mAttackGraphAdapter.getNumVertices();

		for (int i = 0; i < num_vertices; i++) {
			int newVid = num_vertices + i;

			mAttackGraphAdapter.addNode(newVid);

		}

		for (int i = 0; i < num_vertices; i++) {

			Vertice vertice = mAttackGraphAdapter.getGraph().get(i);
			int newVid1 = num_vertices + i;

			for (int j = 0; j < vertice.getNeighborVids().size(); j++) {

				int newVid2 = num_vertices + vertice.getNeighborVids().get(j).intValue();
				mAttackGraphAdapter.addEdge(newVid1, newVid2);
			}
		}
	}

	private void addAttackEdges() {

		for (int i = 0; i < attackerVidIntArray.length; i++) {
			mAttackGraphAdapter.addEdge(attackerVidIntArray[i], FakeArea.get(0).intValue());

			LinkedList<Integer> attackNeighbors = mAttackGraphAdapter.getGraph()
					.get(FakeArea.get(0).intValue()).getNeighborVids();

			for (int k = 0; k < attackNeighbors.size(); k++) {
				mAttackGraphAdapter.addEdge(attackerVidIntArray[i], attackNeighbors.get(k).intValue());
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
