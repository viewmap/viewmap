import java.io.IOException;

import javax.swing.JFileChooser;

import calculator.Calculator;
import calculator.EntropyCalculator;
import calculator.ProbDistributionCalculator;
import calculator.ProbabilityCalculator;
import chart.XYBarChartManager;
import chart.XYLineChartManager;
import io.VMDataManager;
import io.VMFileWriter;
import utility.Constants;
import utility.MathRoutine;

public class Main {

	private static String getFileNameFromJFC() {
		JFileChooser jfc = new JFileChooser();
		String filePath = "";

		jfc.setMultiSelectionEnabled(false);
		jfc.showOpenDialog(null);

		if (jfc.getSelectedFile() == null)
			return null;
		else
			filePath = jfc.getSelectedFile().toString();

		return filePath;
	}

	public static void printVMTag() {
		System.out.println("============= ************* ==============");
		System.out.println("============= ** ViewMap ** ==============");
		System.out.println("============= ************* ==============");
	}

	public static void main(String[] args) throws IOException {

		int flag = Constants.TRACKING_SUCCESS_RATIO_FLAG;
		double[] chartInputData = null;
		VMDataManager dataManager = new VMDataManager();
		XYLineChartManager mXYLineChartManager = null;
		XYBarChartManager mXYBarChartManager = null;

		String fileName = getFileNameFromJFC();

		if (fileName == null)
			return;

		String testName = "";
		String title = "";

		printVMTag();

		dataManager.createTrackTable(fileName);

		Calculator calculator = null;

		switch (flag) {
		case Constants.ENTROPY_FLAG:
			EntropyCalculator entropyCalculator = new EntropyCalculator(dataManager);

			entropyCalculator.calEntropies();

			testName = "Entropy";

			chartInputData = entropyCalculator.getMeanEntropies();

			calculator = entropyCalculator;

			title = testName + "(" + dataManager.getNumVehicles() + " cars, " + dataManager.getTotalTime() + "s)"
					+ "\n(" + fileName + ")";
			mXYLineChartManager = new XYLineChartManager(flag, title);
			mXYLineChartManager.addData(chartInputData);
			mXYLineChartManager.setChart(title);
			mXYLineChartManager.showChart();

			break;

		case Constants.TRACKING_SUCCESS_RATIO_FLAG:
			ProbabilityCalculator probCalculator = new ProbabilityCalculator(dataManager);

			probCalculator.calProbabilities();

			testName = "Tracking success ratio";

			chartInputData = probCalculator.getMeanProbabilities();

			calculator = probCalculator;

			title = testName + "(" + dataManager.getNumVehicles() + " cars, " + dataManager.getTotalTime() + "s)"
					+ "\n(" + fileName + ")";
			mXYLineChartManager = new XYLineChartManager(flag, title);
			mXYLineChartManager.addData(chartInputData);
			mXYLineChartManager.setChart(title);
			mXYLineChartManager.showChart();

			break;

		}

		if (flag != Constants.PROBABILITY_DISTRIBUTION_FLAG) {
			MathRoutine mMathRoutine = new MathRoutine();

			double[] meanProbabilities = mMathRoutine.getMeanValue(calculator.probabilitieBuffer,
					dataManager.getTotalTime(), dataManager.getNumVehicles());
			double[] meanEntropies = mMathRoutine.getMeanValue(calculator.entropiesBuffer, dataManager.getTotalTime(),
					dataManager.getNumVehicles());

			VMFileWriter vmFileWriter = new VMFileWriter(dataManager);

			vmFileWriter.writeMeanValuesToFile(Constants.TRACKING_SUCCESS_RATIO_FLAG, fileName, meanProbabilities);
			vmFileWriter.writeMeanValuesToFile(Constants.ENTROPY_FLAG, fileName, meanEntropies);
		}
	}

}
