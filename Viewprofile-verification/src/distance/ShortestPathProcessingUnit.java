package distance;

import java.util.LinkedList;

import graph.GraphAdapter;
import testroutine.Constants;

public class ShortestPathProcessingUnit {
	private GraphAdapter mGraphAdapter;
	private LinkedList<LinkedList<Integer>> hopTable;

	private int[][] weight;
	private boolean[] found;
	private int[][] path;
	private int[] distance;
	private int[] check;

	public ShortestPathProcessingUnit(GraphAdapter mGraphAdapter) {
		this.mGraphAdapter = mGraphAdapter;
		init_array();
	}

	public void setHopTable(LinkedList<LinkedList<Integer>> hopTable) {
		this.hopTable = hopTable;
		setHopTable();
	}

	private void init_array() {
		int n = mGraphAdapter.getNumVertices();
		weight = new int[n][n];
		found = new boolean[n];
		path = new int[n][n];
		distance = new int[n];
		check = new int[n];
	}

	private void path_init() {

		int i, j;

		int n = mGraphAdapter.getNumVertices();

		for (i = 0; i < n; i++)

			for (j = 0; j < n; j++)

				path[i][j] = Constants.INF;

	}

	private void setWeightTable() {
		int i, j;

		int n = mGraphAdapter.getNumVertices();

		for (i = 0; i < n; i++) {
			for (j = 0; j < n; j++) {
				weight[i][j] = Constants.INF;
			}
		}

		for (i = 0; i < n; i++) {
			LinkedList<Integer> neighbors = mGraphAdapter.getGraph().get(i).getNeighborVids();

			for (j = 0; j < neighbors.size(); j++) {
				weight[i][neighbors.get(j).intValue()] = 1;
			}
		}
	}

	private int choose(int distance[], int n, boolean found[]) {

		int i, min, minpos;

		min = Constants.INF;

		minpos = -1;

		for (i = 0; i < n; i++)

			if (distance[i] < min && !(found[i])) {

				min = distance[i];

				minpos = i;

			}

		return minpos;

	}

	private boolean shortest_path(int start, int n)

	{
		
		boolean isIsolated = false;
		
		int i, j, u, w;

		for (i = 0; i < n; i++) {

			distance[i] = weight[start][i];

			found[i] = false;

			check[i] = 1;

			path[i][0] = start;

		}

		found[start] = true;

		distance[start] = 0;

		for (i = 0; i < n - 2; i++) {

			u = choose(distance, n, found);

			if(u == -1){
				continue;
			}
			
			found[u] = true;

			for (w = 0; w < n; w++) {

				if (!found[w]) {

					if (distance[u] + weight[u][w] < distance[w]) {

						if (i == 0) {

							path[w][check[w]] = u; 

							check[w]++;

						}

						else {

							for (j = 0; j < (check[u] + 1); j++) {

								path[w][j] = path[u][j]; 

								path[w][j + 1] = u;
								check[w]++;

							}

						}

						distance[w] = distance[u] + weight[u][w];

					}

				}

			}

		}
		
		return isIsolated;

	}

	private void setHopTable() {
		int i, j, sum = 0, cnt = 0;

		int n = mGraphAdapter.getNumVertices();

		setWeightTable();
		
		for (i = 0; i < n; i++) {
			path_init();
			shortest_path(i, n);

			for (j = i + 1; j < n; j++) {
				hopTable.get(i).add(new Integer(distance[j]));
			}
		}
	}

}
