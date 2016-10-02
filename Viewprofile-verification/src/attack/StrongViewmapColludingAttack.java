package attack;

import java.util.HashSet;
import java.util.LinkedList;

import distance.ShortestPathCalculator;
import graph.GraphAdapter;
import graph.Vertice;

public class StrongViewmapColludingAttack {
	private GraphAdapter mAttackGraphAdapter;

	private HashSet<Integer> attackerSet;
	private int[] attackerVidIntArray;

	private int numLegitimateVertices;
	private LinkedList<Integer> FakeArea;
	private int[] TrustNodes;

	private int TargetNode;

	private boolean[] checkSwitches;

	private boolean[] checkAddNode;

	private int newFakeNodeVid;

	private int[] fakeNodesDictionary;

	public StrongViewmapColludingAttack(GraphAdapter mAttackGraphAdapter, HashSet<Integer> attackerSet,
			int[] TrustNodes, int TargetNode) {
		this.mAttackGraphAdapter = mAttackGraphAdapter;
		this.attackerSet = attackerSet;
		this.TrustNodes = TrustNodes;
		this.TargetNode = TargetNode;

		attackerVidIntArray = new int[attackerSet.size()];
		numLegitimateVertices = mAttackGraphAdapter.getNumVertices();

		checkSwitches = new boolean[numLegitimateVertices];
		initCheckSwitches();

		checkAddNode = new boolean[numLegitimateVertices];
		initCheckAddNode();

		newFakeNodeVid = numLegitimateVertices;

		fakeNodesDictionary = new int[numLegitimateVertices];
		initFakeNodesDictionary();
	}

	private void initCheckSwitches() {
		for (int i = 0; i < checkSwitches.length; i++) {
			checkSwitches[i] = false;
		}
	}

	private void initCheckAddNode() {
		for (int i = 0; i < checkAddNode.length; i++) {
			checkAddNode[i] = false;
		}
	}

	private void initFakeNodesDictionary() {
		for (int i = 0; i < fakeNodesDictionary.length; i++) {
			fakeNodesDictionary[i] = -1;
		}
	}

	public void setGraphByAttackType() {

		parseAttackSet();

		//copyGraph();

		makeFakeGraph();

		addNormalAttackEdges();

		// addCopyAttackEdges();

		markAttackerSign();
	}

	public void setFakeArea(LinkedList<Integer> TargetArea, LinkedList<Integer> FakeArea) {
		this.FakeArea = FakeArea;
		for (int i = 0; i < TargetArea.size(); i++) {
			//System.out.println(TargetArea.get(i).intValue());
			//System.out.println(fakeNodesDictionary[TargetArea.get(i).intValue()]);

			FakeArea.add(new Integer(fakeNodesDictionary[TargetArea.get(i).intValue()]));

		}
	}

	private void makeFakeGraph() {

		setCheckSwitches();

		int edgesCnt = 0;
		
		int additionalNodeCnt = 0;

		for (int i = 0; i < numLegitimateVertices; i++) {
			if (checkSwitches[i] == true) {
				mAttackGraphAdapter.addNode(numLegitimateVertices + additionalNodeCnt);
				additionalNodeCnt++;
			}
		}

		for (int i = 0; i < numLegitimateVertices; i++) {
			if (checkSwitches[i] == true) {

				LinkedList<Integer> tempNeighbors = mAttackGraphAdapter.getGraph().get(i).getNeighborVids();

				for (int j = 0; j < tempNeighbors.size(); j++) {

					// System.out.println(fakeNodesDictionary[i]+","+fakeNodesDictionary[tempNeighbors.get(j).intValue()]);

					if (checkSwitches[tempNeighbors.get(j).intValue()] == true) {
						mAttackGraphAdapter.addEdge(fakeNodesDictionary[i],
								fakeNodesDictionary[tempNeighbors.get(j).intValue()]);
						
						edgesCnt++;
					}
				}
			}
		}
		
		//System.out.println("Fake Land Total Edges : "+edgesCnt);

	}

	private void setCheckSwitches() {

		for (int j = 0; j < attackerVidIntArray.length; j++) {

			ShortestPathCalculator mSC = new ShortestPathCalculator(mAttackGraphAdapter);

			mSC.init();
			mSC.start(attackerVidIntArray[j], TargetNode);
			checkDuplicatedFakeNodes(mSC.getPathNodes());
		}

	}

	private void checkDuplicatedFakeNodes(LinkedList<Integer> pathNodesList) {

		int[] pathNodes = new int[pathNodesList.size()];

		for (int i = 0; i < pathNodesList.size(); i++) {
			pathNodes[i] = pathNodesList.get(i).intValue();
			//System.out.print(pathNodes[i]+",");
		}
		//System.out.println();
		
		for (int i = 0; i < pathNodes.length; i++) {
			if (checkSwitches[pathNodes[i]] == false) {
				checkSwitches[pathNodes[i]] = true;

				fakeNodesDictionary[pathNodes[i]] = newFakeNodeVid;
				newFakeNodeVid++;
				// System.out.print(pathNodes[i]+":"+fakeNodesDictionary[pathNodes[i]]+",");
				// System.out.println();

				LinkedList<Integer> tempNeighbors = mAttackGraphAdapter.getGraph().get(pathNodes[i]).getNeighborVids();

				for (int j = 0; j < tempNeighbors.size(); j++) {

					if (checkSwitches[tempNeighbors.get(j).intValue()] == false) {
						checkSwitches[tempNeighbors.get(j).intValue()] = true;

						//if (pathNodes[i] == 509)
							//System.out.println("509-" + tempNeighbors.get(j).intValue());

						fakeNodesDictionary[tempNeighbors.get(j).intValue()] = newFakeNodeVid;
						// System.out.print(tempNeighbors.get(j).intValue()+":"+fakeNodesDictionary[tempNeighbors.get(j).intValue()]+",");
						newFakeNodeVid++;
					}
				}
				// System.out.println();

			}
			
			if (checkSwitches[pathNodes[i]] == true) {

				LinkedList<Integer> tempNeighbors = mAttackGraphAdapter.getGraph().get(pathNodes[i]).getNeighborVids();

				for (int j = 0; j < tempNeighbors.size(); j++) {

					if (checkSwitches[tempNeighbors.get(j).intValue()] == false) {
						checkSwitches[tempNeighbors.get(j).intValue()] = true;

						//if (pathNodes[i] == 509)
							//System.out.println("509-" + tempNeighbors.get(j).intValue());

						fakeNodesDictionary[tempNeighbors.get(j).intValue()] = newFakeNodeVid;
						// System.out.print(tempNeighbors.get(j).intValue()+":"+fakeNodesDictionary[tempNeighbors.get(j).intValue()]+",");
						newFakeNodeVid++;
					}
				}
				// System.out.println();

			}
			
		}
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

	private void addNormalAttackEdges() {

		for (int i = 0; i < attackerVidIntArray.length; i++) {

			mAttackGraphAdapter.addEdge(attackerVidIntArray[i], fakeNodesDictionary[attackerVidIntArray[i]]);

			LinkedList<Integer> attackNeighbors = mAttackGraphAdapter.getGraph()
					.get(fakeNodesDictionary[attackerVidIntArray[i]]).getNeighborVids();

			for (int k = 0; k < attackNeighbors.size(); k++) {
				mAttackGraphAdapter.addEdge(attackerVidIntArray[i], attackNeighbors.get(k).intValue());
			}

		}
	}

	private void addCopyAttackEdges() {
		int numCopyNodes = 100;

		int fakeNodeVid = mAttackGraphAdapter.getNumVertices();

		for (int i = 0; i < attackerVidIntArray.length; i++) {

			for (int j = 0; j < numCopyNodes; j++) {
				mAttackGraphAdapter.addNode(fakeNodeVid + j);

				// Copy - Main Map Nodes
				mAttackGraphAdapter.addEdge(attackerVidIntArray[i], fakeNodeVid + j);

				LinkedList<Integer> mainMapAttackNeighbors = mAttackGraphAdapter.getGraph().get(attackerVidIntArray[i])
						.getNeighborVids();

				for (int k = 0; k < mainMapAttackNeighbors.size(); k++) {
					mAttackGraphAdapter.addEdge(mainMapAttackNeighbors.get(k).intValue(), fakeNodeVid + j);
				}

				// Copy - Fake Map Nodes
				mAttackGraphAdapter.addEdge(numLegitimateVertices + attackerVidIntArray[i], fakeNodeVid + j);

				LinkedList<Integer> fakeMapAttackNeighbors = mAttackGraphAdapter.getGraph()
						.get(numLegitimateVertices + attackerVidIntArray[i]).getNeighborVids();

				for (int k = 0; k < fakeMapAttackNeighbors.size(); k++) {
					mAttackGraphAdapter.addEdge(fakeMapAttackNeighbors.get(k).intValue(), fakeNodeVid + j);
				}

			}

			fakeNodeVid = fakeNodeVid + numCopyNodes;
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
