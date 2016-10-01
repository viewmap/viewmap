package testsets;

import java.util.LinkedList;

import attack.AttackSetter;
import graph.GraphAdapter;
import testroutine.Constants;
import trustrank.TrustRankOperator;

public class Test_C_MassedAttack {
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

	private int multiples;

	public void runTest() {

		System.out.println("<<MASSED_GRAPH_TEST>>");

		ATTACK_TYPE = Constants.MASSED_VIEWMAP_COLLUDING;
		ATTACK_NAME = "MASSED_VIEWMAP_COLLUDING";

		TargetNode = 5;
		TrustNodes = new int[1];
		TrustNodes[0] = 369;

		for (int i = 0; i < 5; i++) {
			switchByIdx(i);

			mGraphAdapter = new GraphAdapter();
			mGraphAdapter.createGraphFromFile(Constants.TEST_FILE_PATH);
			/*
			 * TargetRadius = 200.0;
			 * 
			 * NodeSelector nodeSelector = new NodeSelector();
			 * nodeSelector.init(mGraphAdapter, TargetRadius);
			 * 
			 * nodeSelector.updateDistanceTable();
			 * 
			 * nodeSelector.initHopTable(); nodeSelector.updateHopTable();
			 * nodeSelector.destroyShortestPathPU();
			 * 
			 * 
			 * nodeSelector.setTargetNode(TargetNode);
			 * 
			 * TargetArea = null;
			 * 
			 * nodeSelector.setTargetArea(); TargetArea =
			 * nodeSelector.getTargetArea();
			 */

			TargetArea = null;
			TargetArea = new LinkedList<Integer>();

			TargetArea.add(new Integer(TargetNode));

			for (int k = 0; k < mGraphAdapter.getGraph().get(TargetNode).getNeighborVids().size(); k++) {
				TargetArea
						.add(new Integer(mGraphAdapter.getGraph().get(TargetNode).getNeighborVids().get(k).intValue()));
			}

			int percentages = 50;

			System.out.println("ATTACKER_PERCENTAGE = " + percentages + "%...");

			initTest();

			AttackSetter attackSetter = new AttackSetter(ATTACK_TYPE, TrustNodes, TargetNode);

			for (int testCount = 0; testCount < 100; testCount++) {

				attackSetter.createAttackGraph(Constants.TEST_FILE_PATH);

				attackSetter.setTargetArea(TargetArea);
				attackSetter.chooseOneAttacker();

				// System.out.println("complete");

				attackSetter.setAttackGraph(percentages);
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

	private void switchByIdx(int idx) {
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

		//System.out.println("\n<< " + (multiples * 100) + "% Fake Nodes >>\n");

	}
}
