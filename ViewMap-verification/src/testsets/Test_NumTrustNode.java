package testsets;

import java.util.LinkedList;

import attack.AttackSetter;
import graph.GraphAdapter;
import testroutine.Constants;
import testroutine.NodeSelector;
import trustrank.TrustRankOperator;

public class Test_NumTrustNode {
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

	private String mFILE_PATH;

	private int[] TrustNodes;

	public void runTest() {

		mFILE_PATH = Constants.TRUST_NUM_FILE_PATH;

		ATTACK_TYPE = Constants.VIEWMAP_COLLUDING;
		ATTACK_NAME = "VIEWMAP_COLLUDING";
		System.out.println("\n<<ATTACK_TYPE - " + ATTACK_NAME + " >>\n");

		for (int i = 2; i < 5; i++) {
			switchByIdx(i);

			mGraphAdapter = new GraphAdapter();
			mGraphAdapter.createGraphFromFile(mFILE_PATH);

			TargetRadius = 200.0;

			NodeSelector nodeSelector = new NodeSelector();
			nodeSelector.init(mGraphAdapter, TargetRadius);
			nodeSelector.updateDistanceTable();

			nodeSelector.initHopTable();
			nodeSelector.updateHopTable();
			nodeSelector.destroyShortestPathPU();

			nodeSelector.setTargetNode(TargetNode);

			TargetArea = null;
			nodeSelector.setTargetArea();
			TargetArea = nodeSelector.getTargetArea();

			for (int percentages = 60; percentages < 81; percentages = percentages + 10) {

				System.out.println("ATTACKER_PERCENTAGE = " + percentages + "%...");

				initTest();

				AttackSetter attackSetter = new AttackSetter(ATTACK_TYPE, TrustNodes, TargetNode);

				for (int testCount = 1; testCount < 101; testCount++) {

					attackSetter.createAttackGraph(mFILE_PATH);

					attackSetter.setTargetArea(TargetArea);
					attackSetter.chooseAttackers(percentages);

					attackSetter.setAttackGraph();
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

	private void switchByIdx(int idx) {
		TargetNode = 949;
		TrustNodes = new int[idx+2];
		
		switch (idx) {
		case 0:
			TrustNodes[0] = 1752; TrustNodes[1] = 1735;
			break;
		case 1:
			TrustNodes[0] = 1752; TrustNodes[1] = 1328;
			TrustNodes[2] = 1012;
			break;
		case 2:
			TrustNodes[0] = 1752; TrustNodes[1] = 1278;
			TrustNodes[2] = 374; TrustNodes[3] = 990;
			break;
		case 3:
			TrustNodes[0] = 1752; TrustNodes[1] = 543;
			TrustNodes[2] = 1328; TrustNodes[3] = 516;
			TrustNodes[4] = 1224;
			break;
		case 4:
			TrustNodes[0] = 1752; TrustNodes[1] = 543;
			TrustNodes[2] = 1328; TrustNodes[3] = 1735;
			TrustNodes[4] = 1012; TrustNodes[5] = 1224;
			break;
		}

		System.out.println("\n<<THE # OF TRUST NODES - " + (idx + 2) + " >>\n");

	}
}
