package testsets;

import java.util.LinkedList;

import attack.AttackSetter;
import distance.ShortestPathCalculator;
import graph.GraphAdapter;
import testroutine.Constants;
import trustrank.TrustRankOperator;

public class Final_Test_DistanceFromTrustNode {
	private int ATTACK_TYPE;

	private String ATTACK_NAME;
	private double TargetRadius;
	private int  TargetNode;

	private GraphAdapter mGraphAdapter;

	private LinkedList<Integer> TargetArea;
	private LinkedList<Integer> FakeArea;
	private LinkedList<Integer> concernedArea;

	private int successrate;

	private String tempResult;

	private int[] TrustNodes;

	private int multiples;
	private int distanceFromTrustNode;

	public void runTest() {

		System.out.println("<<DISTANCE FROM TRUST NODE TEST>>");

		ATTACK_TYPE = Constants.NUM_FAKE_NODE_VIEWMAP_COLLUDING;
		ATTACK_NAME = "NUM_FAKE_NODE_VIEWMAP_COLLUDING";

		TargetNode = 1286;
		TrustNodes = new int[1];
		TrustNodes[0] = 550;

		int percentages = 10;

		System.out.println("ATTACKER_PERCENTAGE = " + percentages + "%...");
		
		for (int i = 1; i < 2; i++) {
			switchMultiplesByIdx(i);

			for(int j = 3; j < 5; j++){
				switchDistanceByIdx(j);
				
				mGraphAdapter = new GraphAdapter();
				mGraphAdapter.createGraphFromFile(Constants._MIX_GRAPH_PATH);
				/*
				ShortestPathCalculator mSC = new ShortestPathCalculator(mGraphAdapter);

				mSC.init();
				mSC.start(TrustNodes[0], TargetNode);
				
				int max = 0;
				for(int t = 0; t < mGraphAdapter.getNumVertices(); t++){
					if(max < mSC.getShortestDistance(t)){
						max = mSC.getShortestDistance(t);
					}
				}
				
				System.out.println(mSC.getShortestDistance(TargetNode));
				*/
				
				TargetArea = null;
				TargetArea = new LinkedList<Integer>();

				TargetArea.add(new Integer(TargetNode));

				for (int k = 0; k < mGraphAdapter.getGraph().get(TargetNode).getNeighborVids().size(); k++) {
					TargetArea
							.add(new Integer(mGraphAdapter.getGraph().get(TargetNode).getNeighborVids().get(k).intValue()));
				}

				initTest();

				AttackSetter attackSetter = new AttackSetter(ATTACK_TYPE, TrustNodes, TargetNode);

				for (int testCount = 0; testCount < 100; testCount++) {

					attackSetter.createAttackGraph(Constants._MIX_GRAPH_PATH);

					attackSetter.setTargetArea(TargetArea);
					attackSetter.chooseAttackers(percentages, distanceFromTrustNode);

					attackSetter.setAttackGraph(multiples);
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
	
	private void switchDistanceByIdx(int idx) {
		switch (idx) {
		case 0:
			distanceFromTrustNode = 5;
			break;
		case 1:
			distanceFromTrustNode = 10;
			break;
		case 2:
			distanceFromTrustNode = 15;
			break;
		case 3:
			distanceFromTrustNode = 20;
			break;
		case 4:
			distanceFromTrustNode = 25;
			break;
		
		}

		System.out.println("\n<< "+(distanceFromTrustNode-4)+"<= DISTANCE <=" + distanceFromTrustNode + " >>\n");

	}
}
