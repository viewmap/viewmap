package filter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import attack.AttackSetter;
import graph.GraphAdapter;
import graph.Vertice;
import testroutine.Constants;
import trustrank.TrustRankOperator;

public class GraphFilter {
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

	private HashSet<Integer> trustToTarget;

	private HashSet<Integer> targetToTrust;

	public void runTest() {

		System.out.println("<<FILTERING LEGISTIMATE VERTICES>>");

		ATTACK_TYPE = Constants.NUM_FAKE_NODE_VIEWMAP_COLLUDING;
		ATTACK_NAME = "NUM_FAKE_NODE_VIEWMAP_COLLUDING";

		int flag = -1;
		flag = Constants.VIEWMAP_FLAG;

		trustToTarget = new HashSet<Integer>();
		targetToTrust = new HashSet<Integer>();

		TargetNode = 40;
		TrustNodes = new int[1];
		TrustNodes[0] = 953;

		multiples = 1;

		int percentages = 5;

		System.out.println("ATTACKER_PERCENTAGE = " + percentages + "%...");

		mGraphAdapter = new GraphAdapter();
		mGraphAdapter.createGraphFromFile(Constants.TEST_FILE_PATH);

		TargetArea = null;
		TargetArea = new LinkedList<Integer>();

		TargetArea.add(new Integer(TargetNode));

		for (int k = 0; k < mGraphAdapter.getGraph().get(TargetNode).getNeighborVids().size(); k++) {
			TargetArea.add(new Integer(mGraphAdapter.getGraph().get(TargetNode).getNeighborVids().get(k).intValue()));
		}

		initTest();

		AttackSetter attackSetter = new AttackSetter(ATTACK_TYPE, TrustNodes, TargetNode);
		attackSetter.createAttackGraph(Constants.TEST_FILE_PATH);

		attackSetter.setTargetArea(TargetArea);

		attackSetter.chooseAttackers(percentages);

		//attackSetter.chooseOneAttacker();
		
		attackSetter.setAttackGraph(multiples, flag);

		FakeArea = attackSetter.getFakeArea();

		setConcernedArea();

		bfsTargetToTrust(attackSetter.getAttackGraphAdapter(),
				attackSetter.getAttackGraphAdapter().getGraph().get(TargetNode));

		setCleanVisited(attackSetter.getAttackGraphAdapter());

		bfsTrustToTarget(attackSetter.getAttackGraphAdapter(),
				attackSetter.getAttackGraphAdapter().getGraph().get(TrustNodes[0]));
		
		calculateIntersection();

		attackSetter.destroyAttackGraph();

		attackSetter = null;

	}

	public double getDistance(Vertice v1, Vertice v2) {
		double result = 0.0;

		double delX = v1.getX() - v2.getX();
		double delY = v1.getY() - v2.getY();

		result = Math.sqrt(Math.pow(delX, 2) + Math.pow(delY, 2));

		return result;
	}

	public void bfsTrustToTarget(GraphAdapter g, Vertice root) {
		
		int cnt = 0;
		// Since queue is a interface
		Queue<Vertice> queue = new LinkedList<Vertice>();
		LinkedList<Integer> m = new LinkedList<Integer>();
		if (root == null)
			return;

		// Adds to end of queue
		queue.add(root);

		while (!queue.isEmpty()) {
			// removes from front of queue
			Vertice r = queue.remove();
			
			r.setVisited(true);
			cnt++;
			
			// Visit child first before grandchild
			LinkedList<Integer> neighbor = r.getNeighborVids();

			for (int i = 0; i < neighbor.size(); i++) {

				Vertice n = g.getGraph().get(neighbor.get(i));
				
				if (n.getVisited() == false) {
					
					if (getDistance(r, g.getGraph().get(TargetNode)) > getDistance(n, g.getGraph().get(TargetNode))) {
						trustToTarget.add(neighbor.get(i));
						m.add(neighbor.get(i));
						queue.add(n);
						n.setVisited(true);
					}
					
				}
			}
		}

		System.out.println(cnt);

	}

	public void setCleanVisited(GraphAdapter g) {

		for (int i = 0; i < g.getNumVertices(); i++) {
			g.getGraph().get(i).setVisited(false);
		}
	}

	public void bfsTargetToTrust(GraphAdapter g, Vertice root) {
		
		int cnt = 0;
		// Since queue is a interface
		Queue<Vertice> queue = new LinkedList<Vertice>();
		LinkedList<Integer> m = new LinkedList<Integer>();

		if (root == null)
			return;

		// Adds to end of queue
		queue.add(root);

		while (!queue.isEmpty()) {
			// removes from front of queue
			Vertice r = queue.remove();

			r.setVisited(true);
			cnt++;
			
			// Visit child first before grandchild
			LinkedList<Integer> neighbor = r.getNeighborVids();

			for (int i = 0; i < neighbor.size(); i++) {

				Vertice n = g.getGraph().get(neighbor.get(i));

				if (n.getVisited() == false) {
					if (getDistance(r, g.getGraph().get(TrustNodes[0])) > getDistance(n, g.getGraph().get(TrustNodes[0]))) {
						targetToTrust.add(neighbor.get(i));
						m.add(neighbor.get(i));
						queue.add(n);
						n.setVisited(true);
					}
					
				}
			}
		}
		
		System.out.println(cnt);

	}

	private void initTest() {
		successrate = 0;
	}

	private void calculateIntersection() {
		trustToTarget.retainAll(targetToTrust);
		
		int[] result = new int[trustToTarget.size()];
		int lcnt = 0;
		int fcnt = 0;	
		
		String[] attackVidArrayTemp1 = trustToTarget.toString().split("\\[");
		String[] attackVidArrayTemp2 = attackVidArrayTemp1[1].split("\\]");
		String[] attackVidArray = attackVidArrayTemp2[0].split(", ");

		for (int i = 0; i < attackVidArray.length; i++) {
			result[i] = Integer.parseInt(attackVidArray[i]);
		}
		
		for (int i = 0; i < result.length; i++){
			if(result[i] < 1100){
				lcnt++;
			} else {
				fcnt++;
			}
		}
		
		System.out.println("lcnt = "+lcnt+", fcnt = "+fcnt);
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
}
