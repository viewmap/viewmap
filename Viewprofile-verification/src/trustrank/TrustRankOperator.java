package trustrank;

import graph.GraphAdapter;

public class TrustRankOperator {
	private double[][] transitionTable;
	private GraphAdapter mAttackGraphAdapter;
	private int num_vertices;
	private double decayFactor;
	
	public TrustRankOperator(GraphAdapter mAttackGraphAdapter){
		this.mAttackGraphAdapter = mAttackGraphAdapter;
		
		num_vertices = mAttackGraphAdapter.getNumVertices();

		transitionTable = new double[num_vertices][num_vertices];
	}
	
	public void setDecayFactor(double d){
		decayFactor = d;
	}
	
	public void createTransitionTable(){
		
		setZero(transitionTable);
		
		for(int i = 0; i < num_vertices; i++){
			for(int j = 0; j <mAttackGraphAdapter.getGraph().get(i).getNeighborVids().size(); j++){
				int neighborVid = mAttackGraphAdapter.getGraph().get(i).getNeighborVids().get(j).intValue();
				
				transitionTable[i][neighborVid]
						= 1.0/(double)(mAttackGraphAdapter.getGraph().get(neighborVid).getNeighborVids().size());
			}
		}
				
	}
	
	private void setZero(double[] array){
		for(int i = 0; i < array.length; i++){
			array[i] = 0.0;
		}
	}
	private void setZero(double[][] array){
		for(int i = 0; i < array.length; i++){
			for(int j = 0; j < array[0].length; j++)
				array[i][j] = 0.0;
		}
	}
	
	private void matrixMultiply(double [][] A, double [] B, double [] result){
	    
	    int i, j;
	    
	    double sum;
	    
	    for(i = 0; i < num_vertices; i++){
	        sum = 0.0;
	        for(j = 0; j < num_vertices; j++){
	            sum = sum + A[i][j]*B[j];
	        }
	        result[i] = sum;
	    }
	    
	}
	
	public void trustRank(int[] TrustNodes){
		
		double[] trustScore = new double[num_vertices];
		double[] d = new double[num_vertices];
		double[] factor_A = new double[num_vertices];
		double[] factor_B = new double[num_vertices];
		
		double[] multiplexedFactor = new double[num_vertices];
		
		double[] previousTrustScore = new double[num_vertices];
		
		setZero(trustScore);
		setZero(d);
		setZero(factor_A);
		setZero(factor_B);
		
		for(int i = 0; i < TrustNodes.length; i++){
			trustScore[TrustNodes[i]] = 1.0/(double)(TrustNodes.length);
			d[TrustNodes[i]] = 1.0/(double)(TrustNodes.length);
		}
		
		int j = 0;
		while(true){
			
			if(j != 0)
				copyArray(trustScore, previousTrustScore);
			
			for(int k = 0; k < num_vertices; k++){
				factor_A[k] = d[k]*(1-decayFactor);
			}
			
			matrixMultiply(transitionTable, trustScore, multiplexedFactor);
			
			for(int k = 0; k < num_vertices; k++){
				factor_B[k] = decayFactor*multiplexedFactor[k];
			}
			
			for(int k = 0; k < num_vertices; k++){
				trustScore[k] = factor_A[k] + factor_B[k];
			}
			
			for(int k = 0; k < num_vertices; k++){
				mAttackGraphAdapter.getGraph().get(k).assignTrustScore(trustScore[k]);
			}
			
			if(j != 0){
				if(isConverged(trustScore, previousTrustScore))
					break;
			}
			
			j++;
			
		}
			
	}
	
	
	private void copyArray(double[] src, double[] dest){
		for(int i = 0; i < src.length; i++){
			dest[i] = src[i];
		}
	}
	
	private boolean isConverged(double[] pre, double[] curr){
		boolean result = false;
		boolean notAllOverSpread = false;
		
		double sum = 0.0;
		
		int i;
		for(i = 0; i < pre.length; i++){
			sum = sum + Math.abs(pre[i]-curr[i]);
		}
		
		for(i = 0; i < curr.length; i++){
			if(curr[i] == 0.0 && pre[i] == 0.0){
				notAllOverSpread = true;
				break;
			}
		}
		
		if(sum <= 1.0e-10 && !notAllOverSpread)
			result = true;
		
		return result;

		
		
	}
}
