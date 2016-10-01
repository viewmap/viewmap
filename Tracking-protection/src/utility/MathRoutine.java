package utility;

import tree.VehicleNode;

public class MathRoutine {
	public double log2(double param) {
		return (Math.log10(param)) / (Math.log10(2.0));
	}
	
	public double[] getMeanValue(double[][] inputData, int totalTime, int totalVehicles) {
		double[] retArr = new double[totalTime];
		for (int t = Constants.START_TIME; t < totalTime; t++) {
			for (int v = 0; v < totalVehicles; v++) {
				retArr[t] = retArr[t] + inputData[v][t];
			}
			retArr[t] = retArr[t] / (double) (totalVehicles);
		}
		return retArr;
	}
	
	public double getDistance(VehicleNode v1, VehicleNode v2) {
		double distance = 0.0;

		double delX = Math.abs(v1.getX() - v2.getX());
		double delY = Math.abs(v1.getY() - v2.getY());

		distance = Math.sqrt(Math.pow(delX, 2) + Math.pow(delY, 2));

		return distance;
	}
	
	public double[] getAccel(VehicleNode dPrevV, VehicleNode prevV, VehicleNode curV){
		double[] acc = new double[2];
		
		double delPrevVelX = prevV.getX()-dPrevV.getX();
		double delPrevVelY = prevV.getY()-dPrevV.getY();
		
		double delCurVelX = curV.getX()-prevV.getX();
		double delCurVelY = curV.getY()-prevV.getY();
		
		acc[0] = delCurVelX - delPrevVelX;
		acc[1] = delCurVelY - delPrevVelY;
		
		
		return acc;
	}
}
