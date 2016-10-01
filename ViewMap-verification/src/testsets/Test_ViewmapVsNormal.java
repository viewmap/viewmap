package testsets;

import java.util.LinkedList;

import attack.AttackSetter;
import distance.DistanceProcessingUnit;
import graph.GraphAdapter;
import testroutine.Constants;
import testroutine.NodeSelector;
import trustrank.TrustRankOperator;

public class Test_ViewmapVsNormal {

	private int ATTACK_TYPE;

	private String ATTACK_NAME;
	private double TargetRadius;
	private int TrustNode, TargetNode;
	
	private GraphAdapter mGraphAdapter;

	private LinkedList<Integer> TargetArea;
	private LinkedList<Integer> FakeArea;
	private LinkedList<Integer> concernedArea;
	
	private int[] TrustNodes;
	private int successrate;
	
	private String tempResult;
	
	public void runTest() {

		for (int i = 1; i < 2; i++) {
			switchByIdx(i);
			
			mGraphAdapter = new GraphAdapter();
			mGraphAdapter.createGraphFromFile(Constants.FILE_PATH);
			
			TargetRadius = 200.0;
			
			NodeSelector nodeSelector = new NodeSelector();
			nodeSelector.init(mGraphAdapter, TargetRadius);
			nodeSelector.updateDistanceTable();
			
			nodeSelector.initHopTable();
			nodeSelector.updateHopTable();
			nodeSelector.destroyShortestPathPU();
			
			nodeSelector.chooseTrustNodeTargetNode();
			
			TrustNodes = new int[1];
			
			TargetArea = null;
			nodeSelector.setTargetArea();
			TargetArea = nodeSelector.getTargetArea();
			TrustNodes[0] = nodeSelector.getTrustNode();
			TargetNode = nodeSelector.getTargetNode();
			
			for(int percentages = 5; percentages < 31; percentages=percentages + 5){
				
				System.out.println("ATTACKER_PERCENTAGE = "+percentages+"%...");
				
				initTest();
							
				AttackSetter attackSetter = new AttackSetter(ATTACK_TYPE, TrustNodes, TargetNode);
				
				for(int testCount = 1; testCount < 101; testCount++){
									
					attackSetter.createAttackGraph();
					
					attackSetter.setTargetArea(TargetArea);
					attackSetter.chooseAttackers(percentages);
					
					attackSetter.setAttackGraph();
					FakeArea = attackSetter.getFakeArea();
					
					setConcernedArea();
					
					TrustRankOperator trustRanker = new TrustRankOperator(attackSetter.getAttackGraphAdapter());
					trustRanker.createTransitionTable();
					trustRanker.setDecayFactor(0.85);
					
					int[] TrustNodes = new int[1];
					TrustNodes[0] = TrustNode;
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

	private void initTest(){
		successrate = 0;
	}
	
	private void calSuccessRateInEachTest(GraphAdapter mAttackGraphAdapter){
		double max = 0.0;
		int max_vid = -1;
		
		for(int i = 0; i < concernedArea.size(); i++){
			int vid = concernedArea.get(i).intValue();
			double tScore = mAttackGraphAdapter.getGraph().get(vid).getTrustScore();
			
			//System.out.println("vid : "+vid + ", tScore :"+tScore);
			
			if(tScore >= max){
				max = tScore; max_vid = vid;
			}
		}
		
		if(!mAttackGraphAdapter.getGraph().get(max_vid).isAttacker()){
			successrate++;
			tempResult = "t";
		}
		else
			tempResult = "f";
	}
	
	private void showTestResult(){
		System.out.println("success rate(%) : "+successrate+"%");
	}
	
	private void setConcernedArea(){
		concernedArea = null;
		concernedArea = new LinkedList<Integer>();
		
		for(int i = 0; i < TargetArea.size(); i++){
			concernedArea.add(new Integer(TargetArea.get(i).intValue()));
		}
		
		for(int j = 0; j < FakeArea.size(); j++){
			concernedArea.add(new Integer(FakeArea.get(j).intValue()));
		}
		
		/*System.out.print("Concerned Area List : ");
		
		for(int i = 0; i < concernedArea.size(); i++){
				System.out.print(concernedArea.get(i).intValue()+", ");
		}
		System.out.println();
		*/
	}
	
	private void switchByIdx(int idx) {
		switch (idx) {
		case 0:
			ATTACK_TYPE = Constants.VIEWMAP_INDIVIDUAL;
			ATTACK_NAME = "VIEWMAP_INDIVIDUAL";
			break;
		case 1:
			ATTACK_TYPE = Constants.VIEWMAP_COLLUDING;
			ATTACK_NAME = "VIEWMAP_COLLUDING";
			break;
		case 2:
			ATTACK_TYPE = Constants.NORMAL_INDIVIDUAL;
			ATTACK_NAME = "NORMAL_INDIVIDUAL";
			break;
		case 3:
			ATTACK_TYPE = Constants.NORMAL_COLLUDING;
			ATTACK_NAME = "NORMAL_COLLUDING";
			break;
		}
		
		System.out.println("\n<<ATTACK_TYPE - "+ATTACK_NAME+" >>\n");
		
	}

}
