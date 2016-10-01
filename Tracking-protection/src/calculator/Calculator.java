package calculator;

import java.util.LinkedList;

import candidate.VMCandidateSelector;
import io.VMDataManager;
import tree.VehicleNode;
import utility.Constants;
import utility.DupRemover;
import utility.MathRoutine;

public class Calculator {

	public VMDataManager mDataManager;
	public VMCandidateSelector mCandidateSelector;
	public LinkedList<LinkedList<VehicleNode>> vehicleTrackingTree;
	public double[][] entropiesBuffer;
	public double[][] probabilitieBuffer;

	public Calculator(VMDataManager mDataManager) {
		this.mDataManager = mDataManager;
		this.mCandidateSelector = new VMCandidateSelector(mDataManager);

		initMeanArray();
		
		initVehicleTrackingTree();
	}

	public void initMeanArray(){
		entropiesBuffer = new double[mDataManager.getNumVehicles()][mDataManager.getTotalTime()];
		probabilitieBuffer = new double[mDataManager.getNumVehicles()][mDataManager.getTotalTime()];
	}
	
	public void initVehicleTrackingTree() {
		vehicleTrackingTree = new LinkedList<LinkedList<VehicleNode>>();

		for (int i = 0; i < mDataManager.getTotalTime(); i++) {
			vehicleTrackingTree.add(new LinkedList<VehicleNode>());
		}
	}
	
	public void updateMeanArray(int vid){
		MathRoutine mMathRoutine = new MathRoutine();
		
		for (int t = Constants.START_TIME; t < mDataManager.getTotalTime(); t++) {
			
			double entropy = 0.0;
			
			for (int i = 0; i < vehicleTrackingTree.get(t).size(); i++) {
				double curProb = vehicleTrackingTree.get(t).get(i).getProbability();
				entropy = entropy + (-1) * curProb * mMathRoutine.log2(curProb);
			}
			
			entropiesBuffer[vid - 1][t] = entropy;
		}
		
		for (int t = Constants.START_TIME; t < mDataManager.getTotalTime(); t++) {

			boolean exist = false;
			for (int j = 0; j < vehicleTrackingTree.get(t).size(); j++) {
				if (vehicleTrackingTree.get(t).get(j).getVid() == vid) {
					exist = true;
					break;
				}
			}

			if (!exist)
				probabilitieBuffer[vid - 1][t] = 0.0;
			else
				probabilitieBuffer[vid - 1][t] = vehicleTrackingTree.get(t).stream().filter(v -> v.getVid() == vid).findFirst()
						.get().getProbability();

		}
	}

	public void setRootNode(int vid) {
		VehicleNode rootNode = new VehicleNode(vid);

		rootNode.setProbability(1.0);
		rootNode.setTime(Constants.START_TIME);

		rootNode.setPrevVid(vid);

		vehicleTrackingTree.get(Constants.START_TIME).add(rootNode);

	}

	public void fillVehicleTrackingTree() {

		DupRemover mDupRemover = new DupRemover();

		for (int time = Constants.START_TIME; time < mDataManager.getTotalTime() - 1; time++) {
			System.out.print(time+" ");

			if((time % 60) == 0)
				System.out.println();
			
			LinkedList<VehicleNode> candidatesAccumulator = new LinkedList<VehicleNode>();

			for (int i = 0; i < vehicleTrackingTree.get(time).size(); i++) {
				LinkedList<VehicleNode> candidates = mCandidateSelector
						.getCandidates(vehicleTrackingTree.get(time).get(i), time);
				for (int j = 0; j < candidates.size(); j++) {
					candidatesAccumulator.add(candidates.get(j));
				}
			}
			
			mDupRemover.removeDuplicateVehicle(candidatesAccumulator);
			
			for (int i = 0; i < candidatesAccumulator.size(); i++) {
				vehicleTrackingTree.get(time + 1).add(candidatesAccumulator.get(i));
			}
		}
		
		System.out.println("\n");

	}
	
	public void fillVehicleTrackingTree_v3() {

		DupRemover mDupRemover = new DupRemover();
		
		for (int time = Constants.START_TIME; time < mDataManager.getTotalTime() - 1; time++) {
			
			System.out.print(time+" ");

			if((time % 60) == 0)
				System.out.println();

			for (int i = 0; i < vehicleTrackingTree.get(time).size(); i++) {
				LinkedList<VehicleNode> candidates = mCandidateSelector
						.getCandidates(vehicleTrackingTree.get(time).get(i), time);
				for (int j = 0; j < candidates.size(); j++) {
					vehicleTrackingTree.get(time + 1).add(candidates.get(j));
				}
			}
			
			mDupRemover.removeDuplicateVehicle_v2(vehicleTrackingTree.get(time + 1));
		}
		
		System.out.println("\n");

		for (int time = Constants.START_TIME; time < mDataManager.getTotalTime(); time++) {
			mDupRemover.removeDuplicateVehicle(vehicleTrackingTree.get(time));
		}
	}
}
