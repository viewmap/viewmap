package attack;

import java.util.HashSet;
import java.util.LinkedList;

import distance.ShortestPathCalculator;
import graph.GraphAdapter;
import testroutine.Constants;

public class AttackSetter {

	private int ATTACK_TYPE;
	private GraphAdapter mAttackGraphAdapter;
	private LinkedList<HashSet<Integer>> attackerHistory;
	private HashSet<Integer> currentAttackerSet;

	private int numOfAttackers;
	private int TargetNode;
	private int[] TrustNodes;

	private LinkedList<Integer> TargetArea;
	private LinkedList<Integer> FakeArea;

	public AttackSetter(int ATTACK_TYPE, int[] TrustNodes, int TargetNode) {
		this.ATTACK_TYPE = ATTACK_TYPE;
		this.TrustNodes = TrustNodes;
		this.TargetNode = TargetNode;

		attackerHistory = new LinkedList<HashSet<Integer>>();
		FakeArea = new LinkedList<Integer>();
	}

	public void setTargetArea(LinkedList<Integer> targetArea) {
		TargetArea = targetArea;
	}

	public void createAttackGraph() {
		mAttackGraphAdapter = new GraphAdapter();
		mAttackGraphAdapter.createGraphFromFile(Constants.FILE_PATH);
	}

	public void createAttackGraph(String file_path) {
		mAttackGraphAdapter = new GraphAdapter();
		mAttackGraphAdapter.createGraphFromFile(file_path);
	}

	public void destroyAttackGraph() {
		mAttackGraphAdapter = null;
	}

	public void chooseAttackers(int percentages) {
		numOfAttackers = ((mAttackGraphAdapter.getNumVertices()) / 100) * percentages;

		HashSet<Integer> newSet;

		do {

			newSet = generateAttackerSet();

		} while (duplicateAttackerSet(newSet));

		attackerHistory.add(newSet);

		currentAttackerSet = newSet;

	}
	
	public void chooseOneAttacker() {
		numOfAttackers = 1;

		HashSet<Integer> newSet;

		do {

			newSet = generateAttackerSet();

		} while (duplicateAttackerSet(newSet));

		attackerHistory.add(newSet);

		currentAttackerSet = newSet;

	}

	public void chooseAttackers(int percentages, int distance) {
		numOfAttackers = ((mAttackGraphAdapter.getNumVertices()) / 100) * percentages;

		HashSet<Integer> newSet;

		do {

			newSet = generateAttackerSet(distance);

		} while (duplicateAttackerSet(newSet));

		attackerHistory.add(newSet);

		currentAttackerSet = newSet;

	}

	private HashSet<Integer> generateAttackerSet() {
		HashSet<Integer> newAttackerSet = new HashSet<Integer>();

		int attackerVid;

		while (newAttackerSet.size() != numOfAttackers) {

			attackerVid = (int) (Math.random() * (mAttackGraphAdapter.getNumVertices() - 1));

			if (containTrustNodes(attackerVid) || containTargetArea(attackerVid))
				continue;

			newAttackerSet.add(attackerVid);
		}

		return newAttackerSet;
	}

	private HashSet<Integer> generateAttackerSet(int distance) {
		HashSet<Integer> newAttackerSet = new HashSet<Integer>();

		ShortestPathCalculator mSC = new ShortestPathCalculator(mAttackGraphAdapter);

		LinkedList<Integer> randomBox = new LinkedList<Integer>();

		int attackerVid;

		int max = 0;

		// mSC.start(0, TrustNodes[0]);
		// System.out.println(mSC.getShortestDistance());

		mSC.init();

		for (int i = 0; i < mAttackGraphAdapter.getNumVertices(); i++) {

			mSC.start(TrustNodes[0], i);

			/*
			 * if(max < mSC.getShortestDistance(i)) max =
			 * mSC.getShortestDistance(i);
			 */
			if (distance - 4 <= mSC.getShortestDistance(i) && mSC.getShortestDistance(i) <= distance
					&& !(containTrustNodes(i) || containTargetArea(i))) {
				mSC.inverseFind();
				randomBox.add(i);
			}
		}

		if (numOfAttackers >= randomBox.size()) {
			for (int j = 0; j < randomBox.size(); j++) {
				newAttackerSet.add(randomBox.get(j));
			}
			// System.out.println("numOfAttackers >= randomBox.size()");
		}

		if (randomBox.size() == numOfAttackers + 1) {
			while (newAttackerSet.size() < numOfAttackers) {
				int idx = (int) (Math.random() * (randomBox.size() - 1));
				newAttackerSet.add(randomBox.get(idx));
			}
			// System.out.println("randomBox.size() == numOfAttackers + 1");
		}

		if (randomBox.size() >= numOfAttackers + 2) {
			while (newAttackerSet.size() < numOfAttackers) {
				int idx = (int) (Math.random() * (randomBox.size() - 1));
				newAttackerSet.add(randomBox.get(idx));
			}
			//System.out.println("randomBox.size() == numOfAttackers + 2");
		}

		while (newAttackerSet.size() < numOfAttackers) {

			attackerVid = (int) (Math.random() * (mAttackGraphAdapter.getNumVertices() - 1));

			if (containTrustNodes(attackerVid) || containTargetArea(attackerVid))
				continue;

			newAttackerSet.add(attackerVid);
		}

		//System.out.println(newAttackerSet.toString());

		// System.out.println(max);

		return newAttackerSet;
	}

	private boolean containTargetArea(int vid) {
		boolean result = false;

		for (int i = 0; i < TargetArea.size(); i++) {
			if (TargetArea.get(i).intValue() == vid) {
				result = true;
				break;
			}
		}

		return result;
	}

	private boolean containTrustNodes(int vid) {
		boolean result = false;

		for (int i = 0; i < TrustNodes.length; i++) {
			if (TrustNodes[i] == vid) {
				result = true;
				break;
			}
		}

		return result;
	}

	private boolean duplicateAttackerSet(HashSet<Integer> attackers) {
		boolean result = false;

		for (int i = 0; i < attackerHistory.size(); i++) {
			if (attackerHistory.get(i).containsAll(attackers)) {
				result = true;
				break;
			}
		}

		return result;
	}

	public LinkedList<Integer> getFakeArea() {
		return FakeArea;
	}

	public GraphAdapter getAttackGraphAdapter() {
		return mAttackGraphAdapter;
	}

	public void setAttackGraph(int param) {
		if (ATTACK_TYPE == Constants.NUM_FAKE_NODE_VIEWMAP_COLLUDING) {

			BigFakeAreaAttack BFAttack = new BigFakeAreaAttack(mAttackGraphAdapter, currentAttackerSet);

			FakeArea = null;
			FakeArea = new LinkedList<Integer>();

			BFAttack.setFakeArea(TargetArea, FakeArea, param);
			BFAttack.setGraphByAttackType(param);

		}
		
		if (ATTACK_TYPE == Constants.MASSED_VIEWMAP_COLLUDING) {
			
			MassedAttack MAttack = new MassedAttack(mAttackGraphAdapter, currentAttackerSet);

			FakeArea = null;
			FakeArea = new LinkedList<Integer>();

			MAttack.setFakeArea(TargetArea, FakeArea);
			MAttack.setGraphByAttackType(param);

		}

	}

	public void setAttackGraph(int param, int flag) {
		if (ATTACK_TYPE == Constants.NUM_FAKE_NODE_VIEWMAP_COLLUDING) {

			BigFakeAreaAttack BFAttack = new BigFakeAreaAttack(mAttackGraphAdapter, currentAttackerSet);

			FakeArea = null;
			FakeArea = new LinkedList<Integer>();

			BFAttack.setFakeArea(TargetArea, FakeArea, param);
			BFAttack.setGraphByAttackType(param);

		}
		
		if (ATTACK_TYPE == Constants.MASSED_VIEWMAP_COLLUDING){
			MassedAttack MAttack = new MassedAttack(mAttackGraphAdapter, currentAttackerSet);

			FakeArea = null;
			FakeArea = new LinkedList<Integer>();

			MAttack.setFakeArea(TargetArea, FakeArea);
			MAttack.setGraphByAttackType(param, flag);
		}
		

	}
	
	public void setAttackGraph() {
		if (ATTACK_TYPE == Constants.NORMAL_INDIVIDUAL) {
			NormalIndividualAttack NIAttack = new NormalIndividualAttack(mAttackGraphAdapter, currentAttackerSet);

			FakeArea = null;
			FakeArea = new LinkedList<Integer>();

			NIAttack.setFakeArea(FakeArea);
			NIAttack.setGraphByAttackType();

		}

		if (ATTACK_TYPE == Constants.NORMAL_COLLUDING) {
			NormalColludingAttack NCAttack = new NormalColludingAttack(mAttackGraphAdapter, currentAttackerSet);

			FakeArea = null;
			FakeArea = new LinkedList<Integer>();

			NCAttack.setFakeArea(TargetArea, FakeArea);
			NCAttack.setGraphByAttackType();
		}

		if (ATTACK_TYPE == Constants.VIEWMAP_INDIVIDUAL) {

		}

		if (ATTACK_TYPE == Constants.VIEWMAP_COLLUDING) {

			ViewmapColludingAttack VCAttack = new ViewmapColludingAttack(mAttackGraphAdapter, currentAttackerSet);

			FakeArea = null;
			FakeArea = new LinkedList<Integer>();

			VCAttack.setFakeArea(TargetArea, FakeArea);
			VCAttack.setGraphByAttackType();

		}
		if (ATTACK_TYPE == Constants.STRONG_VIEWMAP_COLLUDING) {

			StrongViewmapColludingAttack SVCAttack = new StrongViewmapColludingAttack(mAttackGraphAdapter,
					currentAttackerSet, TrustNodes, TargetNode);

			FakeArea = null;
			FakeArea = new LinkedList<Integer>();

			SVCAttack.setGraphByAttackType();
			SVCAttack.setFakeArea(TargetArea, FakeArea);

		}

		if (ATTACK_TYPE == Constants.STRONG_VIEWMAP_INDIVIDUAL) {

			StrongViewmapIndividualAttack SVIAttack = new StrongViewmapIndividualAttack(mAttackGraphAdapter,
					currentAttackerSet, TargetNode);

			FakeArea = null;
			FakeArea = new LinkedList<Integer>();

			SVIAttack.setFakeArea(FakeArea);
			SVIAttack.setGraphByAttackType();

		}

	}

}
