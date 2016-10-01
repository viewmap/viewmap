import java.util.LinkedList;

import filter.GraphFilter;
import graph.GraphAdapter;
import testroutine.Constants;
import testroutine.NodeSelector;
import testsets.Final_Test_DistanceFromTrustNode;
import testsets.Final_Test_MassedAttack;
import testsets.Final_Test_New_Massed_Attack;
import testsets.Final_Test_NodeDegree;
import testsets.Final_Test_ViewmapVsNormal;
import testsets.Test_C_DecayFactor;
import testsets.Test_C_DistanceFromTrustNode;
import testsets.Test_C_NodeDegree;
import testsets.Test_C_NumFakeNode;
import testsets.Test_C_NumTrustNode;
import testsets.Test_C_MassedAttack;
import testsets.Test_C_ViewmapVsColluding;
import testsets.Test_DecayFactor;
import testsets.Test_NodeDegree;
import testsets.Test_NumTrustNode;
import testsets.Test_ViewmapVsNormal;

public class Main {

	private static int ATTACK_TYPE = -1;

	String resultFileName;

	public static void main(String[] args) {
		
		Final_Test_New_Massed_Attack NMA = new Final_Test_New_Massed_Attack();
		NMA.runTest();
		
		/*
		GraphFilter gf = new GraphFilter();
		gf.runTest();
		
		
		
		Final_Test_NodeDegree TestND = new Final_Test_NodeDegree();
		
		TestND.runTest();
		
		Final_Test_MassedAttack TestMA = new Final_Test_MassedAttack();
		TestMA.runTest();
		*/
		/*
		Final_Test_DistanceFromTrustNode TestDFT = new Final_Test_DistanceFromTrustNode();
		TestDFT.runTest();
		
		Final_Test_ViewmapVsNormal TestVVN = new Final_Test_ViewmapVsNormal();
		TestVVN.runTest();
		
		Test_C_DistanceFromTrustNode test5 = new Test_C_DistanceFromTrustNode();

		test5.runTest();
		
		Test_C_ViewmapVsColluding test1 = new Test_C_ViewmapVsColluding();

		test1.runTest();
		
		
		
		Test_C_NumTrustNode test3 = new Test_C_NumTrustNode();

		test3.runTest();
		
		Test_C_NodeDegree test4 = new Test_C_NodeDegree();

		test4.runTest();
		
		Test_C_NumFakeNode test6 = new Test_C_NumFakeNode();

		test6.runTest();
		
		Test_C_MassedAttack test7 = new Test_C_MassedAttack();

		test7.runTest();
		
		
		Test_C_DecayFactor test2 = new Test_C_DecayFactor();

		test2.runTest();*/
		
	}

}
