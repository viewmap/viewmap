package graph;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GraphAdapter {
	private int num_vertices;
	private LinkedList<Vertice> Graph;

	public GraphAdapter() {
		super();
	}

	public LinkedList<Vertice> getGraph(){
		return Graph;
	}
	
	public int getNumVertices(){
		return num_vertices;
	}
	
	public void updateNumVertices(){
		num_vertices = Graph.size();
	}

	private void createGraph(int num_vertices) {

		Graph = new LinkedList<Vertice>();
		this.num_vertices = num_vertices;

		for (int i = 0; i < num_vertices; i++) {

			Vertice newNode = new Vertice(i);;
			
			Graph.add(newNode);
			
		}
	}

	public void addNode(int vid){
		
		Graph.add(new Vertice(vid));
		
		updateNumVertices();
		
	}
	
	public void addNullNode(){
		
		Graph.add(null);
	}
	
	public void addFakeNode(int vid){
		Graph.add(new Vertice(vid));
		num_vertices++;
	}
	
	public void addEdge(int vid1, int vid2) {
		
		Graph.get(vid1).addNeighbor(vid2);
		Graph.get(vid2).addNeighbor(vid1);
		
	}
	
	public void showGraph(){
		for(int i = 0; i < num_vertices; i++){

			System.out.print(i+" : ");
			for(int j = 0; j < Graph.get(i).getNeighborVids().size(); j++){
				System.out.print(Graph.get(i).getNeighborVids().get(j).intValue()+", ");
			}
			System.out.println();
		}
	}
	
	public void showAttackers(){
		System.out.print("Attacker List : ");
		
		for(int i = 0; i < num_vertices; i++){
			if(Graph.get(i).isAttacker())
				System.out.print(i+", ");
		}
		System.out.println();
	}
	
	private int getNumVertexFromFileString(String buff){
		String[] tokensSpiltedByTab = buff.split("\t");
		
		return Integer.parseInt(tokensSpiltedByTab[0]);
	}
	
	private void setPointFromFileString(int vid, String buff){
		String[] tokensSpiltedByTab = buff.split("\t");
		
		double x, y;
		
		x = Double.parseDouble(tokensSpiltedByTab[2]);
		y = Double.parseDouble(tokensSpiltedByTab[3]);
		
		Graph.get(vid).setX(x);
		Graph.get(vid).setY(y);
		
	}
	
	private void createEdgesFromFileString(String buff){
		String[] tokensSpiltedByTab = buff.split("\t");
		
		int vid1, vid2;
		
		vid1 = Integer.parseInt(tokensSpiltedByTab[1]);
		vid2 = Integer.parseInt(tokensSpiltedByTab[2]);
		
		addEdge(vid1,vid2);
		
		
	}
	
	private void skipReadLine(BufferedReader graphReader) throws IOException{
		graphReader.readLine();
	}
	
	public void createGraphFromFile(String filename){
		try {
			BufferedReader graphReader = new BufferedReader(new FileReader(filename));
			String buff;
			
			try {
				
				for(int i = 0; i < 2; i++)
					skipReadLine(graphReader);
				
				buff = graphReader.readLine();
				if(buff != null){
					int num_vertices;
					
					num_vertices = getNumVertexFromFileString(buff);
				
					skipReadLine(graphReader);
					
					createGraph(num_vertices);
				}
				else
					System.out.println("file read error - first line reading error");
				
				
				for(int vid = 0; vid < num_vertices; vid++){
					buff = graphReader.readLine();
					setPointFromFileString(vid, buff);
				}
				
				while((buff = graphReader.readLine()) != null){
					createEdgesFromFileString(buff);
				}
				
				
			} catch (IOException e) {
				System.out.println("file read error - error in reading file");
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("file read error - File Not Found");
			e.printStackTrace();
		}
		
		
	}
	
}
