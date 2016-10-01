package calculator;

import io.VMDataManager;
import utility.Constants;
import utility.MathRoutine;
import utility.ProgressBarSetter;

public class EntropyCalculator extends Calculator {

	double[][] Entropies;

	public EntropyCalculator(VMDataManager mDataManager) {
		super(mDataManager);

		Entropies = new double[mDataManager.getNumVehicles()][mDataManager.getTotalTime()];
	}
	
	public void insertEntropyOf(int vid) {
		MathRoutine mMathRoutine = new MathRoutine();
		for (int t = Constants.START_TIME; t < mDataManager.getTotalTime(); t++) {
			
			double entropy = 0.0;
			
			for (int i = 0; i < vehicleTrackingTree.get(t).size(); i++) {
				double curProb = vehicleTrackingTree.get(t).get(i).getProbability();
				entropy = entropy + (-1) * curProb * mMathRoutine.log2(curProb);
			}
			
			Entropies[vid - 1][t] = entropy;
		}
	}
	
	public void calEntropies() {

		ProgressBarSetter mProgressBarSetter = new ProgressBarSetter(mDataManager);
		mProgressBarSetter.showProgressBar(Constants.ENTROPY_FLAG);

		for (int i = 1; i <= mDataManager.getNumVehicles(); i++) {

			setRootNode(i);

			mProgressBarSetter.setTitle("Evaluating ... (" + i + "/" + mDataManager.getNumVehicles() + ")");
			mProgressBarSetter.updateProgressBar(Math.floor(((double) i / (double) mDataManager.getNumVehicles()) * 100));
			
			fillVehicleTrackingTree();
			
			updateMeanArray(i);

			insertEntropyOf(i);

			initVehicleTrackingTree();
		}
		
		mProgressBarSetter.closeProgressBar();
	}
	
	public double[] getMeanEntropies(){
		MathRoutine mMathRoutine = new MathRoutine();
		return mMathRoutine.getMeanValue(Entropies, mDataManager.getTotalTime(), mDataManager.getNumVehicles());
	}
}
