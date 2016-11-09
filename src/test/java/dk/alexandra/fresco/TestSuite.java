package dk.alexandra.fresco;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import dk.alexandra.fresco.demo.SetIntersectionDemo;
import dk.alexandra.fresco.suite.bgw.TestBgwBasicArithmetic;
import dk.alexandra.fresco.suite.bgw.TestBgwComparison;
import dk.alexandra.fresco.suite.dummy.TestDummyProtocolSuite;
import dk.alexandra.fresco.suite.spdz.TestFakeTripGen;
import dk.alexandra.fresco.suite.spdz.TestLookUpProtocol;
import dk.alexandra.fresco.suite.spdz.TestSpdzBasicArithmetic2Parties;
import dk.alexandra.fresco.suite.spdz.TestSpdzBasicArithmetic3Parties;
import dk.alexandra.fresco.suite.spdz.TestSpdzComparison;
import dk.alexandra.fresco.suite.spdz.TestSpdzLPBuildingBlocks;
import dk.alexandra.fresco.suite.spdz.TestSpdzLPSolver2Parties;
import dk.alexandra.fresco.suite.spdz.TestSpdzLPSolver3Parties;
import dk.alexandra.fresco.suite.spdz.TestSpdzSorting;
import dk.alexandra.fresco.suite.spdz.TestSpdzStatistics;
import dk.alexandra.fresco.suite.tinytables.TestTinyTables;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	//BGW protocol suite
	TestBgwBasicArithmetic.class,
	TestBgwComparison.class,
	//Dummy protocol suite
	TestDummyProtocolSuite.class,
	//Spdz protocol suite
	TestFakeTripGen.class, TestLookUpProtocol.class, TestSpdzBasicArithmetic2Parties.class, 
	TestSpdzBasicArithmetic3Parties.class, TestSpdzComparison.class, TestSpdzLPBuildingBlocks.class,
	TestSpdzLPSolver2Parties.class, TestSpdzLPSolver3Parties.class, TestSpdzSorting.class, 
	TestSpdzStatistics.class,
	//Tinytables protocol suite
	TestTinyTables.class,
	//applications
	SetIntersectionDemo.class
})
public class TestSuite {
  //nothing
}