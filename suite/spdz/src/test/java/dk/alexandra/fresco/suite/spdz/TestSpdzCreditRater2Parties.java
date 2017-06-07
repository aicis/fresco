/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.IntegrationTest;
import dk.alexandra.fresco.framework.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.lib.statistics.CreditRaterTest;
import dk.alexandra.fresco.suite.spdz.storage.InitializeStorage;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;


/**
 * Tests for the CreditRater.
 * 
 */
public class TestSpdzCreditRater2Parties extends AbstractSpdzTest{

  private int[] values;
  private int[][] intervals, scores;
  
  @Before
  public void setTestValues() {
    values = new int[]{101, 251};
    intervals = new int[2][];
    intervals[0] = new int[]{100, 500};
    intervals[1] = new int[]{250, 500};   
    
    scores = new int[2][];
    scores[0] = new int[]{10, 13, 15};
    scores[1] = new int[]{10, 13, 15};    
  }
  
  
  @Test
  public void test_CreditRater_alternate_values() throws Exception {
    values = new int[]{101};
    intervals = new int[1][];
    intervals[0] = new int[]{100, 500};
    scores = new int[1][];
    scores[0] = new int[]{10, 13, 15};
    runTest(new CreditRaterTest.TestCreditRater(values, intervals, scores),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET,
        PreprocessingStrategy.DUMMY, 2);
   
    values = new int[]{10};
    runTest(new CreditRaterTest.TestCreditRater(values, intervals, scores),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET,
        PreprocessingStrategy.DUMMY, 2);
    values = new int[]{100};
    runTest(new CreditRaterTest.TestCreditRater(values, intervals, scores),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET,
        PreprocessingStrategy.DUMMY, 2);
    values = new int[]{500};
    runTest(new CreditRaterTest.TestCreditRater(values, intervals, scores),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET,
        PreprocessingStrategy.DUMMY, 2);
    values = new int[]{1000};
    runTest(new CreditRaterTest.TestCreditRater(values, intervals, scores),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET,
        PreprocessingStrategy.DUMMY, 2);
    
    intervals[0] = new int[]{1000};
    scores[0] = new int[]{10, 20};
    runTest(new CreditRaterTest.TestCreditRater(values, intervals, scores),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET,
        PreprocessingStrategy.DUMMY, 2);
    
    values = new int[]{101, 440, 442};
    intervals = new int[3][];
    intervals[0] = new int[]{100, 500};
    intervals[1] = new int[]{100, 200};
    intervals[2] = new int[]{50, 800};
    scores = new int[3][];
    scores[0] = new int[]{10, 13, 15};
    scores[1] = new int[]{1, 3, 21};
    scores[2] = new int[]{16, 15, 11};
    runTest(new CreditRaterTest.TestCreditRater(values, intervals, scores),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET,
        PreprocessingStrategy.DUMMY, 2);

  }


  @Test
	public void test_CreditRater_2_Sequential_batched_dummy() throws Exception {
		runTest(new CreditRaterTest.TestCreditRater(values, intervals, scores), 
				EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
	}

	// Using a non-batched evaulation strategy has extremely poor performance.
	// Hence the problem size is reduced
	// TODO figure out what the problem is
	@Category(IntegrationTest.class)
	@Test
	public void test_CreditRater_2_Sequential_dummy() throws Exception {
		runTest(new CreditRaterTest.TestCreditRater(values, intervals, scores), 
				EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
	}


  // Ignoring the streamed tests since they take too long with respect to generating preprocessed material
	// Note that the poor performance of non-batched evaulation is most likely also the case here.
	//TODO: Maybe add the @Category(IntegrationTest.class) instead of @Ignore.
	//TODO Does not work atm
	@Ignore
	@Test
	public void test_CreditRater_2_Sequential_batched_streamed() throws Exception {
		int noOfThreads = 1;
		InitializeStorage.cleanup();
		try {
			InitializeStorage.initStreamedStorage(new FilebasedStreamedStorageImpl(new InMemoryStorage()), 2, noOfThreads, 20000, 500, 500000, 2000);
			runTest(new CreditRaterTest.TestCreditRater(values, intervals, scores), 
					EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET, PreprocessingStrategy.STATIC, 2);
		} finally {
			InitializeStorage.cleanup();
		}
	}
	
	@Ignore
	@Test
	public void test_CreditRater_2_SequentialBatched_streamed() throws Exception {
		int noOfThreads = 2;		
		InitializeStorage.cleanup();
		try {
			InitializeStorage.initStreamedStorage(new FilebasedStreamedStorageImpl(new InMemoryStorage()), 2, noOfThreads, 20000, 500, 800000, 3000);
			runTest(new CreditRaterTest.TestCreditRater(values, intervals, scores),
					EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET, PreprocessingStrategy.STATIC, 2);
		} finally {
			InitializeStorage.cleanup();
		}
	}
}
