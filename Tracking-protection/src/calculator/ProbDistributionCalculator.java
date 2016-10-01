package calculator;

import java.util.NoSuchElementException;

import io.VMDataManager;
import tree.VehicleNode;
import utility.Constants;
import utility.ProgressBarSetter;

public class ProbDistributionCalculator extends Calculator {
	private double[] lastTimeProbabilities;

	public ProbDistributionCalculator(VMDataManager mDataManager) {
		super(mDataManager);

		lastTimeProbabilities = new double[mDataManager.getNumVehicles()];
	}

	private void updateProbabilities() {
		for(int vid = 1; vid <= mDataManager.getNumVehicles(); vid++){
			int curVid = vid;
			VehicleNode tempVN = null;
			
			try{
				tempVN = vehicleTrackingTree.get(mDataManager.getTotalTime()-1).stream().filter(v -> v.getVid() == curVid).findFirst()
					.get();
			} catch(NoSuchElementException e){
				lastTimeProbabilities[vid - 1] = 0.0;
				continue;
			}
			
			lastTimeProbabilities[vid - 1] = tempVN.getProbability();
		}
		
		double probability = 0.0;
		for(int vid = 0; vid < mDataManager.getNumVehicles(); vid++){
			probability = probability + lastTimeProbabilities[vid];
		}
		
		System.out.println(probability);

	}

	public void calProbabilities(int vid) {
		ProgressBarSetter mProgressBarSetter = new ProgressBarSetter(mDataManager);
		mProgressBarSetter.showProgressBar(Constants.PROBABILITY_DISTRIBUTION_FLAG);

		setRootNode(vid);

		mProgressBarSetter.setTitle("Evaluating ... (" + vid + "/" + 1 + ")");
		mProgressBarSetter.updateProgressBar(100);

		fillVehicleTrackingTree();
		
		updateProbabilities();

		initVehicleTrackingTree();

		mProgressBarSetter.closeProgressBar();
	}

	public double[] getProbabilitiesInLastTime() {
		return lastTimeProbabilities;
	}
}
