package attack;

import java.util.HashSet;
import java.util.LinkedList;

import distance.ShortestPathCalculator;
import graph.GraphAdapter;

public class StrongViewmapIndividualAttack {
	private GraphAdapter mAttackGraphAdapter;

	private HashSet<Integer> attackerSet;
	private int[] attackerVidIntArray;

	private int numLegitimateVertices;
	private LinkedList<Integer> FakeArea;

	private int TargetNode;

	public StrongViewmapIndividualAttack(GraphAdapter mAttackGraphAdapter, HashSet<Integer> attackerSet,
			int TargetNode) {
		this.mAttackGraphAdapter = mAttackGraphAdapter;
		this.attackerSet = attackerSet;
		this.TargetNode = TargetNode;

		attackerVidIntArray = new int[attackerSet.size()];
		numLegitimateVertices = mAttackGraphAdapter.getNumVertices();
	}

	public void setGraphByAttackType() {

		parseAttackSet();

		addAdvancedAttackEdges();

		markAttackerSign();
	}

	private void addAttackEdges() {
		int fakeNodeVid = mAttackGraphAdapter.getNumVertices();
		int i, j;

		ShortestPathCalculator mSC = new ShortestPathCalculator(mAttackGraphAdapter);

		mSC.init();

		for (i = 0; i < attackerVidIntArray.length; i++) {
			mSC.start(TargetNode, attackerVidIntArray[i]);

			mAttackGraphAdapter.addNode(fakeNodeVid);

			mAttackGraphAdapter.addEdge(attackerVidIntArray[i], fakeNodeVid);

			fakeNodeVid++;

			for (j = 0; j < mSC.getShortestDistance(attackerVidIntArray[i]); j++) {
				mAttackGraphAdapter.addNode(fakeNodeVid);
				mAttackGraphAdapter.addEdge(fakeNodeVid - 1, fakeNodeVid);
				fakeNodeVid++;
			}

			FakeArea.add(new Integer(fakeNodeVid - 1));

		}
	}

	private void addAdvancedAttackEdges() {
		int fakeNodeVid = mAttackGraphAdapter.getNumVertices();
		int i, j, k;

		ShortestPathCalculator mSC = new ShortestPathCalculator(mAttackGraphAdapter);

		mSC.init();

		for (i = 0; i < attackerVidIntArray.length; i++) {
			mSC.start(TargetNode, attackerVidIntArray[i]);

			mAttackGraphAdapter.addNode(fakeNodeVid);

			mAttackGraphAdapter.addEdge(attackerVidIntArray[i], fakeNodeVid);

			int focusNodeVid = fakeNodeVid;

			fakeNodeVid++;

			for (j = 0; j < mSC.getShortestDistance(attackerVidIntArray[i]); j++) {
				mAttackGraphAdapter.addNode(fakeNodeVid);

				mAttackGraphAdapter.addEdge(focusNodeVid, fakeNodeVid);

				focusNodeVid = fakeNodeVid;

				fakeNodeVid++;

				for (k = 0; k < 3; k++) {
					mAttackGraphAdapter.addNode(fakeNodeVid);
					mAttackGraphAdapter.addEdge(fakeNodeVid, focusNodeVid);
					fakeNodeVid++;
				}

			}

			for (j = 0; j < mAttackGraphAdapter.getGraph().get(focusNodeVid).getNeighborVids().size(); j++) {
				FakeArea.add(mAttackGraphAdapter.getGraph().get(focusNodeVid).getNeighborVids().get(j));
			}

		}
	}

	public void setFakeArea(LinkedList<Integer> FakeArea) {
		this.FakeArea = FakeArea;
	}

	public void markAttackerSign() {
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
