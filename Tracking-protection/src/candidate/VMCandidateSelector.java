package candidate;

import java.util.LinkedList;

import io.VMDataManager;
import tree.VehicleNode;
import utility.Constants;
import utility.MathRoutine;

public class VMCandidateSelector {

	private VMDataManager mDataManager;


	public VMCandidateSelector(VMDataManager mDataManager) {
		this.mDataManager = mDataManager;
	}

	public boolean checkPredictable(int time){
		return (time >= 60)&&(time % 60 == 0);
	}
	
	public LinkedList<VehicleNode> getCandidates(VehicleNode vehicle, int time) {
		
		if(!checkPredictable(time)){
			LinkedList<VehicleNode> candidates = new LinkedList<VehicleNode>();
			VehicleNode selfVN = new VehicleNode(vehicle.getVid());
			
			selfVN.setPrevVid(vehicle.getVid());
			selfVN.setTime(time + 1);
			selfVN.setProbability(vehicle.getProbability());
			
			candidates.add(selfVN);
			
			return candidates;
		}
		
		LinkedList<VehicleNode> candidates = new LinkedList<VehicleNode>();
		MathRoutine mMathRoutine = new MathRoutine();

		VehicleNode curVehicle = mDataManager.getVehicle(time, vehicle.getVid());
		VehicleNode prevVehicle = mDataManager.getVehicle(time - 1, vehicle.getPrevVid());
		
		double speed = mMathRoutine.getDistance(curVehicle, prevVehicle)+3.5;
		double totalDistance = 0.0;
		double predictionX, predictionY;

		predictionX = curVehicle.getX() + ((double) (curVehicle.getX() - prevVehicle.getX()));
		predictionY = curVehicle.getY() + ((double) (curVehicle.getY() - prevVehicle.getY()));

		VehicleNode predVehicle = new VehicleNode(-1);
		predVehicle.setLocation(predictionX, predictionY);

		LinkedList<VehicleNode> nextTimeTrack = mDataManager.getTrackTableByTime(time + 1);

		for (int i = 0; i < nextTimeTrack.size(); i++) {

			if (mMathRoutine.getDistance(predVehicle, nextTimeTrack.get(i)) + mMathRoutine.getDistance(curVehicle, nextTimeTrack.get(i))
					<= speed) {

				VehicleNode newVehicleNode = new VehicleNode(nextTimeTrack.get(i).getVid());
				newVehicleNode.setTime(time + 1);
				candidates.add(newVehicleNode);
			}
		}

		if (candidates.size() >= 1) {
			int overlapped = 0;
			
			LinkedList<VehicleNode> rtList = new LinkedList<VehicleNode>();
			
			for(int i = 0; i < candidates.size(); i++){
				
				VehicleNode v = mDataManager.getVehicle(time + 1, candidates.get(i).getVid());
				
				if(v.getX()==predVehicle.getX() && v.getY() == predVehicle.getY()){
					VehicleNode newV = new VehicleNode(v.getVid());
					newV.setTime(time + 1);
					newV.setPrevVid(vehicle.getVid());
					
					rtList.add(newV);
					
					overlapped++;
				}
			}
			
			if(overlapped > 0){
				for(int j = 0; j < rtList.size(); j++){
					rtList.get(j).setProbability((1.0/overlapped)*vehicle.getProbability());
				}
				return rtList;
			}
		}
		
		else{

			VehicleNode newV = nextTimeTrack.get(0);
			
			VehicleNode newInitVN = new VehicleNode(newV.getVid());
			newInitVN.setTime(time + 1);
			newInitVN.setPrevVid(vehicle.getVid());
			newInitVN.setDistanceFromPred(mMathRoutine.getDistance(predVehicle, newV));
			
			candidates.add(newInitVN);
			
			for(int i = 1; i < nextTimeTrack.size(); i++){
				for(int j = 0; j < 1; j++){
					if(candidates.get(j).getDistanceFromPred() > mMathRoutine.getDistance(predVehicle, nextTimeTrack.get(i))){
						
						VehicleNode newVN = new VehicleNode(nextTimeTrack.get(i).getVid());
						newVN.setTime(time + 1);
						newVN.setPrevVid(vehicle.getVid());
						newVN.setDistanceFromPred(mMathRoutine.getDistance(predVehicle,nextTimeTrack.get(i)));
						
						candidates.add(j, newVN);
						
						candidates.removeLast();
						break;
					}
				}
			}
		}
		
		for (int i = 0; i < candidates.size(); i++) {
			candidates.get(i).setDistanceFromPred(
					mMathRoutine.getDistance(mDataManager.getVehicle(time + 1, candidates.get(i).getVid()), predVehicle));

			totalDistance = totalDistance + candidates.get(i).getDistanceFromPred();
		}

		for (int i = 0; i < candidates.size(); i++) {

			double probability = 0.0;

			if (candidates.size() != 1) {

				probability = (double) ((1.0 - (double) ((candidates.get(i).getDistanceFromPred()) / totalDistance)))
						/ (double) (candidates.size() - 1);

				if (probability == 1.0) {
					int compareVid = candidates.get(i).getVid();

					candidates.get(i).setPrevVid(vehicle.getVid());

					candidates.get(i).setProbability(1.0 * vehicle.getProbability());
					candidates.removeIf(v -> v.getVid() != compareVid);
					break;
				}
			}

			else
				probability = 1.0;

			probability = probability * vehicle.getProbability();

			candidates.get(i).setProbability(probability);

			candidates.get(i).setPrevVid(vehicle.getVid());
			

		}

		return candidates;
	}
}
