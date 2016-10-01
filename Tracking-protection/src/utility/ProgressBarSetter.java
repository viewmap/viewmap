package utility;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

import io.VMDataManager;

public class ProgressBarSetter {
	VMDataManager mDataManager;
	
	JProgressBar bar;
	JFrame progress;
	Container content;
	
	int flag;
	
	public ProgressBarSetter(VMDataManager mDataManager){
		this.mDataManager = mDataManager;
	}
	
	public void setTitle(String title){
		progress.setTitle(title);
	}
	
	public void updateProgressBar(double percentage){
		bar.setValue((int) percentage);
		bar.setStringPainted(true);
	}
	
	public void showProgressBar(int flag) {

		String testName = "";

		if (flag == Constants.ENTROPY_FLAG) {
			testName = "entropy";
		}

		if (flag == Constants.TRACKING_SUCCESS_RATIO_FLAG) {
			testName = "tracking success ratio";
		}
		
		if (flag == Constants.PROBABILITY_DISTRIBUTION_FLAG){
			testName = "probability distributin";
		}

		progress = new JFrame("Evaluating ... (" + 0 + "/" + mDataManager.getNumVehicles() + ")");
		progress.setSize(600, 150);

		Dimension frameSize = progress.getSize();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		progress.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		progress.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		bar = new JProgressBar();
		Border border = BorderFactory.createTitledBorder("Calculating " + testName + " ... ");
		bar.setBorder(border);
		bar.setValue(0);
		bar.setStringPainted(true);
		content = progress.getContentPane();
		content.add(bar, BorderLayout.CENTER);

		progress.setVisible(true);
	}
	
	public void closeProgressBar() {
		progress.dispose();
	}
}
