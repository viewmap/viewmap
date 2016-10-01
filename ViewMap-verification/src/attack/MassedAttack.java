package attack;

import java.util.HashSet;
import java.util.LinkedList;

import graph.GraphAdapter;
import graph.Vertice;

public class MassedAttack {
	private GraphAdapter mAttackGraphAdapter;

	private HashSet<Integer> attackerSet;
	private int[] attackerVidIntArray;

	private int numLegitimateVertices;
	private LinkedList<Integer> FakeArea;

	public MassedAttack(GraphAdapter mAttackGraphAdapter, HashSet<Integer> attackerSet) {
		this.mAttackGraphAdapter = mAttackGraphAdapter;
		this.attackerSet = attackerSet;
		attackerVidIntArray = new int[attackerSet.size()];
		numLegitimateVertices = mAttackGraphAdapter.getNumVertices();
	}

	public void setGraphByAttackType(int numOfDummyNode) {

		parseAttackSet();

		copyGraph();

		addCopyAttackEdges(numOfDummyNode);

		markAttackerSign();
	}

	public void setGraphByAttackType(int numOfDummyNode, int multiples) {
		parseAttackSet();

		multipleNodes(multiples);

		addCopyAttackEdges(numOfDummyNode);

		markAttackerSign();
	}

	private void multipleNodes(int multiples) {
		int num_vertices = mAttackGraphAdapter.getNumVertices();

		for (int i = 0; i < num_vertices * multiples; i++) {
			int newVid = num_vertices + i;

			mAttackGraphAdapter.addNode(newVid);
			mAttackGraphAdapter.getGraph().get(newVid)
					.setX(mAttackGraphAdapter.getGraph().get(newVid - num_vertices).getX());
			mAttackGraphAdapter.getGraph().get(newVid)
					.setY(mAttackGraphAdapter.getGraph().get(newVid - num_vertices).getY());
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

		for (int i = 0; i < multiples - 1; i++) {
			/*
			 * for (int j = 0; j < num_vertices; j++) { for (int vid :
			 * mAttackGraphAdapter.getGraph().get(numLegitimateVertices * (i +
			 * 1) + j).getNeighborVids()) { mAttackGraphAdapter.addEdge(vid,
			 * numLegitimateVertices * (i + 2) + j); } }
			 * 
			 * for (int j = 0; j < num_vertices; j++) { for (int vid :
			 * mAttackGraphAdapter.getGraph().get(numLegitimateVertices * (i +
			 * 2) + j).getNeighborVids()) { mAttackGraphAdapter.addEdge(vid,
			 * numLegitimateVertices * (i + 1) + j); } }
			 */
			for (int j = 0; j < num_vertices; j++) {
				if (j % 10 == 0)
					mAttackGraphAdapter.addEdge(numLegitimateVertices * (i + 1) + j,
							numLegitimateVertices * (i + 2) + j);
			}
		}

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

	private void addCopyAttackEdges(int numOfDummyNode) {
		int numCopyNodes = numOfDummyNode;

		int fakeNodeVid = mAttackGraphAdapter.getNumVertices();

		for (int i = 0; i < attackerVidIntArray.length; i++) {
			int offset = 0;

			mAttackGraphAdapter.addEdge(numLegitimateVertices + attackerVidIntArray[i], attackerVidIntArray[i]);

			for (int j = 0; j < numCopyNodes; j++) {
				mAttackGraphAdapter.addNode(fakeNodeVid + offset);
				mAttackGraphAdapter.addEdge(attackerVidIntArray[i], fakeNodeVid + offset);

				LinkedList<Integer> mainMapAttackNeighbors = mAttackGraphAdapter.getGraph().get(attackerVidIntArray[i])
						.getNeighborVids();

				for (int k = 0; k < mainMapAttackNeighbors.size(); k++) {
					if (mainMapAttackNeighbors.get(k).intValue() < numLegitimateVertices) {
						mAttackGraphAdapter.addEdge(mainMapAttackNeighbors.get(k).intValue(), fakeNodeVid + offset);
					}
				}

				offset++;

				mAttackGraphAdapter.addNode(fakeNodeVid + offset);
				mAttackGraphAdapter.addEdge(numLegitimateVertices + attackerVidIntArray[i], fakeNodeVid + offset);

				LinkedList<Integer> fakeMapAttackNeighbors = mAttackGraphAdapter.getGraph()
						.get(numLegitimateVertices + attackerVidIntArray[i]).getNeighborVids();

				for (int k = 0; k < fakeMapAttackNeighbors.size(); k++) {
					if (numLegitimateVertices <= fakeMapAttackNeighbors.get(k).intValue()
							&& fakeMapAttackNeighbors.get(k).intValue() < numLegitimateVertices * 2) {
						mAttackGraphAdapter.addEdge(fakeMapAttackNeighbors.get(k).intValue(), fakeNodeVid + offset);
					}
				}

				fakeMapAttackNeighbors = mAttackGraphAdapter.getGraph().get(fakeNodeVid + offset).getNeighborVids();

				/*
				 * int q = -1;
				 * 
				 * if (fakeMapAttackNeighbors.size() / 5 > 0) q =
				 * fakeMapAttackNeighbors.size() / 5; else q =
				 * fakeMapAttackNeighbors.size();
				 * 
				 * for (int k = 0; k < q; k++) { if (numLegitimateVertices <=
				 * fakeMapAttackNeighbors.get(k).intValue() &&
				 * fakeMapAttackNeighbors.get(k).intValue() <
				 * numLegitimateVertices * 2) {
				 * mAttackGraphAdapter.addEdge(fakeMapAttackNeighbors.get(k).
				 * intValue(), (fakeNodeVid + offset - 1)); } }
				 */
				offset++;

				mAttackGraphAdapter.addEdge(fakeNodeVid + (offset - 1), fakeNodeVid + (offset - 2));

			}

			fakeNodeVid = fakeNodeVid + numCopyNodes * 2;

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
