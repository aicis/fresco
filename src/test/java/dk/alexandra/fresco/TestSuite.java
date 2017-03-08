/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco;

import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Categories.ExcludeCategory;
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
import dk.alexandra.fresco.suite.spdz.TestSpdzMiMC;
import dk.alexandra.fresco.suite.spdz.TestSpdzSorting;
import dk.alexandra.fresco.suite.spdz.TestSpdzStatistics;
import dk.alexandra.fresco.suite.tinytables.TestTinyTables;

@RunWith(Categories.class)
@Suite.SuiteClasses({
	//BGW protocol suite
	//TestBgwBasicArithmetic.class,
	//TestBgwComparison.class,
	//Dummy protocol suite
	//TestDummyProtocolSuite.class,
	//Spdz protocol suite
	TestFakeTripGen.class, 
	TestLookUpProtocol.class, TestSpdzBasicArithmetic2Parties.class, 
	TestSpdzBasicArithmetic3Parties.class, TestSpdzComparison.class, TestSpdzLPBuildingBlocks.class,
	TestSpdzLPSolver2Parties.class, TestSpdzLPSolver3Parties.class, TestSpdzSorting.class, 
	TestSpdzStatistics.class, TestSpdzMiMC.class,
	//Tinytables protocol suite
	TestTinyTables.class,
	//applications
	SetIntersectionDemo.class
})
@ExcludeCategory(IntegrationTest.class)
public class TestSuite {
  //nothing
}