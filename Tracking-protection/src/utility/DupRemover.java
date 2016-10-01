package utility;

import java.util.LinkedList;

import tree.VehicleNode;

public class DupRemover {
	
	public DupRemover(){
		super();
	}
	
	public void removeDuplicateVehicle(LinkedList<VehicleNode> candidatesAccumulator) {
		LinkedList<VehicleNode> copy = new LinkedList<VehicleNode>();

		for (int i = 0; i < candidatesAccumulator.size(); i++) {
			copy.add(candidatesAccumulator.get(i));
		}

		while (!copy.isEmpty()) {
			VehicleNode compare = copy.peek();

			VehicleNode addition = new VehicleNode(compare.getVid());
			addition.setPrevVid(compare.getPrevVid());
			addition.setProbability(0.0);
			addition.setTime(compare.getTime());

			for (int i = 0; i < candidatesAccumulator.size(); i++) {
				if (compare.getVid() == candidatesAccumulator.get(i).getVid()) {
					addition.setProbability(addition.getProbability() + candidatesAccumulator.get(i).getProbability());
				}
			}

			candidatesAccumulator.removeIf(v -> v.getVid() == addition.getVid());
			candidatesAccumulator.add(addition);

			copy.removeIf(v -> v.getVid() == addition.getVid());

		}

	}
	
	public void removeDuplicateVehicle_v2(LinkedList<VehicleNode> candidatesAccumulator) {
		LinkedList<VehicleNode> copy = new LinkedList<VehicleNode>();

		for (int i = 0; i < candidatesAccumulator.size(); i++) {
			copy.add(candidatesAccumulator.get(i));
		}

		while (!copy.isEmpty()) {
			VehicleNode compare = copy.peek();

			VehicleNode addition = new VehicleNode(compare.getVid());
			addition.setPrevVid(compare.getPrevVid());
			addition.setDoublePrevVid(compare.getDoublePrevVid());
			addition.setProbability(0.0);
			addition.setTime(compare.getTime());

			for (int i = 0; i < candidatesAccumulator.size(); i++) {
				if (compare.getVid() == candidatesAccumulator.get(i).getVid() && compare.getPrevVid() == candidatesAccumulator.get(i).getPrevVid()) {
					addition.setProbability(addition.getProbability() + candidatesAccumulator.get(i).getProbability());
				}
			}

			candidatesAccumulator.removeIf(v -> v.getVid() == addition.getVid() && v.getPrevVid() == addition.getPrevVid());
			candidatesAccumulator.add(addition);

			copy.removeIf(v -> v.getVid() == addition.getVid() && v.getPrevVid() == addition.getPrevVid());

		}

	}
	
}
