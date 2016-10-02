package testsets;

import java.util.LinkedList;

import attack.AttackSetter;
import graph.GraphAdapter;
import testroutine.Constants;
import trustrank.TrustRankOperator;

public class Final_Test_New_Massed_Attack {
	private int ATTACK_TYPE;

	private String ATTACK_NAME;
	private double TargetRadius;
	private int TrustNode, TargetNode;

	private GraphAdapter mGraphAdapter;

	private LinkedList<Integer> TargetArea;
	private LinkedList<Integer> FakeArea;
	private LinkedList<Integer> concernedArea;

	private int successrate;

	private String tempResult;

	private int[] TrustNodes;

	private int numOfDummyNodes;

	private int multiples;
	
	public void runTest() {

		System.out.println("<<MASSED ATTACK TEST>>");

		ATTACK_TYPE = Constants.MASSED_VIEWMAP_COLLUDING;
		ATTACK_NAME = "MASSED_VIEWMAP_COLLUDING";

		TargetNode = 1286;
		TrustNodes = new int[1];
		TrustNodes[0] = 550;
		
		int percentages = 1;

		System.out.println("ATTACKER_PERCENTAGE = " + percentages + "%...");

		for (int i = 1; i < 5; i++) {
			switchMultiplesByIdx(i);

			for (int j = 1; j < 5; j++) {
				switchNumOfDummyNodesByIdx(j);

				mGraphAdapter = new GraphAdapter();
				mGraphAdapter.createGraphFromFile(Constants._MIX_GRAPH_PATH);

				TargetArea = null;
				TargetArea = new LinkedList<Integer>();

				TargetArea.add(new Integer(TargetNode));

				for (int k = 0; k < mGraphAdapter.getGraph().get(TargetNode).getNeighborVids().size(); k++) {
					TargetArea.add(
							new Integer(mGraphAdapter.getGraph().get(TargetNode).getNeighborVids().get(k).intValue()));
				}

				initTest();

				AttackSetter attackSetter = new AttackSetter(ATTACK_TYPE, TrustNodes, TargetNode);

				for (int testCount = 0; testCount < 100; testCount++) {

					attackSetter.createAttackGraph(Constants._MIX_GRAPH_PATH);

					attackSetter.setTargetArea(TargetArea);
					attackSetter.chooseAttackers(percentages);

					attackSetter.setAttackGraph(numOfDummyNodes, multiples);
					FakeArea = attackSetter.getFakeArea();

					setConcernedArea();

					TrustRankOperator trustRanker = new TrustRankOperator(attackSetter.getAttackGraphAdapter());
					trustRanker.createTransitionTable();
					trustRanker.setDecayFactor(0.85);

					trustRanker.trustRank(TrustNodes);

					calSuccessRateInEachTest(attackSetter.getAttackGraphAdapter());

					attackSetter.destroyAttackGraph();

					System.out.print(tempResult);
				}

				attackSetter = null;

				System.out.println();

				showTestResult();

			}

		}

	}

	private void initTest() {
		successrate = 0;
	}

	private void calSuccessRateInEachTest(GraphAdapter mAttackGraphAdapter) {
		double max = 0.0;
		int max_vid = -1;

		for (int i = 0; i < concernedArea.size(); i++) {
			int vid = concernedArea.get(i).intValue();
			double tScore = mAttackGraphAdapter.getGraph().get(vid).getTrustScore();

			if (tScore >= max) {
				max = tScore;
				max_vid = vid;
			}
		}

		if (!mAttackGraphAdapter.getGraph().get(max_vid).isAttacker()) {
			successrate++;
			tempResult = "t";
		} else
			tempResult = "f";
	}

	private void showTestResult() {
		System.out.println("success rate(%) : " + successrate + "%");
	}

	private void setConcernedArea() {
		concernedArea = null;
		concernedArea = new LinkedList<Integer>();

		for (int i = 0; i < TargetArea.size(); i++) {
			concernedArea.add(new Integer(TargetArea.get(i).intValue()));
		}

		for (int j = 0; j < FakeArea.size(); j++) {
			concernedArea.add(new Integer(FakeArea.get(j).intValue()));
		}
	}

	private void switchMultiplesByIdx(int idx) {
		switch (idx) {
		case 0:
			multiples = 1;
			break;
		case 1:
			multiples = 2;
			break;
		case 2:
			multiples = 3;
			break;
		case 3:
			multiples = 4;
			break;
		case 4:
			multiples = 5;
			break;

		}

		System.out.println("\n<< " + (multiples * 100) + "% Fake Nodes >>\n");

	}

	private void switchNumOfDummyNodesByIdx(int idx) {
		switch (idx) {
		case 0:
			numOfDummyNodes = 50;
			break;
		case 1:
			numOfDummyNodes = 100;
			break;
		case 2:
			numOfDummyNodes = 150;
			break;
		case 3:
			numOfDummyNodes = 200;
			break;
		case 4:
			numOfDummyNodes = 250;
			break;

		}

		System.out.println("\n<< " + "The # of Dummy Nodes - " + numOfDummyNodes + " >>\n");

	}
}
