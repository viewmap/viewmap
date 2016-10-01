package io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import utility.Constants;

public class VMFileWriter {
	
	VMDataManager mDataManager;
	boolean windows_enviroment = false;
	
	public VMFileWriter(VMDataManager mDataManager) {
		this.mDataManager = mDataManager;
		
		windows_enviroment = System.getProperty("os.name").contains("Windows");
		
	}
	
	public String extractFileNameFromPath(String fullFileName){
		String[] tokensSpiltedByTab = null;
		
		if(windows_enviroment){
			tokensSpiltedByTab = fullFileName.split("\\\\");
		} else {
			tokensSpiltedByTab = fullFileName.split("/");
		}
		
		return tokensSpiltedByTab[tokensSpiltedByTab.length - 1];
	}
	
	public String getCurrentTime(){
		long time = System.currentTimeMillis(); 
		SimpleDateFormat dayTime = new SimpleDateFormat("yyyy_mm_dd_hh_mm_ss");
		return dayTime.format(new Date(time));
	}
	
	public void writeMeanValuesToFile(int flag, String fileName, double[] meanValues) throws IOException {
		
		String valueName = "";
		if(flag == Constants.ENTROPY_FLAG)
			valueName = "_MeanEntropies";
		else if(flag == Constants.TRACKING_SUCCESS_RATIO_FLAG)
			valueName = "_TrackingRatio_";
		
		fileName = "["+getCurrentTime()+"]"+valueName+extractFileNameFromPath(fileName);
		
		FileOutputStream output = null;
		
		if(windows_enviroment)
			output = new FileOutputStream("C:\\Users\\LJM\\Desktop\\" + fileName );
		else
			output = new FileOutputStream(fileName);
			
		for (int t = Constants.START_TIME; t < mDataManager.getTotalTime(); t++) {
			
			String inputString = "" + t;
			
			inputString = inputString + "\t" + meanValues[t] + "\n";
			
			output.write(inputString.getBytes());
		}
		output.close();
	}
}
