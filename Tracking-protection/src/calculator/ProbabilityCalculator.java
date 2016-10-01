package calculator;

import io.VMDataManager;
import utility.Constants;
import utility.MathRoutine;
import utility.ProgressBarSetter;

public class ProbabilityCalculator extends Calculator {

	double[][] Probabilities;

	public ProbabilityCalculator(VMDataManager mDataManager) {
		super(mDataManager);

		Probabilities = new double[mDataManager.getNumVehicles()][mDataManager.getTotalTime()];
	}
	
	private void insertProbOf(int vid){
		for (int t = Constants.START_TIME; t < mDataManager.getTotalTime(); t++) {

			boolean exist = false;
			for (int j = 0; j < vehicleTrackingTree.get(t).size(); j++) {
				if (vehicleTrackingTree.get(t).get(j).getVid() == vid) {
					exist = true;
					break;
				}
			}

			if (!exist)
				Probabilities[vid - 1][t] = 0.0;
			else
				Probabilities[vid - 1][t] = vehicleTrackingTree.get(t).stream().filter(v -> v.getVid() == vid).findFirst()
						.get().getProbability();

		}
	}
	
	public void calProbabilities() {
		ProgressBarSetter mProgressBarSetter = new ProgressBarSetter(mDataManager);
		mProgressBarSetter.showProgressBar(Constants.TRACKING_SUCCESS_RATIO_FLAG);
		
		for (int i = 1; i <= mDataManager.getNumVehicles(); i++) {
			
			System.out.print("vid "+i+": ");

			setRootNode(i);
			
			mProgressBarSetter.setTitle("Evaluating ... (" + i + "/" + mDataManager.getNumVehicles() + ")");
			mProgressBarSetter
					.updateProgressBar(Math.floor(((double) i / (double) mDataManager.getNumVehicles()) * 100));

			fillVehicleTrackingTree();
			
			updateMeanArray(i);
						
			insertProbOf(i);

			initVehicleTrackingTree();
			
			System.out.println();
		}

		mProgressBarSetter.closeProgressBar();
	}
	
	public double[] getMeanProbabilities(){
		MathRoutine mMathRoutine = new MathRoutine();
		return mMathRoutine.getMeanValue(Probabilities, mDataManager.getTotalTime(), mDataManager.getNumVehicles());
	}

	

}
