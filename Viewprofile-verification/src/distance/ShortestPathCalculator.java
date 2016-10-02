package distance;

import java.util.LinkedList;
import java.util.Vector;

import graph.GraphAdapter;
import testroutine.Constants;

public class ShortestPathCalculator {
	private GraphAdapter mGraphAdapter;

	private int[][] data;
	int n = 0; 

	final static int m = 30000; 

	boolean visit[]; 
	int dis[]; 
	int prev[]; 

	int s, e; 
	int stack[];
	
	LinkedList<Integer> pathNodes;

	Vector<Integer> stackV;

	public ShortestPathCalculator(GraphAdapter mGraphAdapter){
		this.mGraphAdapter = mGraphAdapter;
		pathNodes = new LinkedList<Integer>();
	}
	
	public void init() 
	{
		initData();
		n = mGraphAdapter.getNumVertices();

		dis = new int[n];
		visit = new boolean[n];
		prev = new int[n];
		stack = new int[n];
		stackV = new Vector<Integer>();
	}

	public int theLeastDistance() {
		return dis[e - 1];
	}

	public void start(int start, int end) {
		s = start;
		e = end;

		int k = 0;
		int min = 0;

		for (int i = 0; i < n; i++) { 
			dis[i] = m;
			prev[i] = 0;
			visit[i] = false;
		}

		dis[s] = 0; 

		for (int i = 0; i < n; i++) {
			min = m;
			for (int j = 0; j < n; j++) { 
				if (visit[j] == false
						&& dis[j] < min) { 
					k = j;
					min = dis[j];
				}
			}
			visit[k] = true; 

			if (min == m)
				break; 

			for (int j = 0; j < n; j++) {
				if (dis[k] + data[k][j] < dis[j]) {
					dis[j] = dis[k] + data[k][j]; 
					prev[j] = k; 
				}
			}
		}
		inverseFind();
	}

	public int getShortestDistance(int i) {
		return dis[i];
	}

	public void inverseFind() {
		int tmp = 0;
		int top = -1;
		tmp = e;
		while (true) {
			stack[++top] = tmp;
			if (tmp == s)
				break;
			tmp = prev[tmp];
		}

		stackV.removeAllElements();
		for (int i = top; i > -1; i--) {
			//System.out.printf("%d", stack[i]);
			pathNodes.add(new Integer(stack[i]));
			stackV.add(stack[i]);
			//if (i != 0)System.out.printf(" -> ");
		}
		//System.out.printf("\n");
	}

	public LinkedList<Integer> getPathNodes(){
		return pathNodes;
	}
	
	public Vector<Integer> getStack() {
		return stackV;
	}

	private void initData() {
		int n = mGraphAdapter.getNumVertices();

		data = new int[n][n];
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				data[i][j] = m;
			}
		}

		for (int i = 0; i < n; i++) {

			data[i][i] = 0;

			int neighbors = mGraphAdapter.getGraph().get(i).getNeighborVids().size();

			for (int k = 0; k < neighbors; k++) {
				data[i][mGraphAdapter.getGraph().get(i).getNeighborVids().get(k).intValue()] = 1;
			}

		}
	}

}
