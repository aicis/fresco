/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/

package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.network.NetworkingStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.lib.statistics.DEASolver;
import dk.alexandra.fresco.lib.statistics.DEASolver.AnalysisType;
import dk.alexandra.fresco.lib.statistics.DEASolverTests.RandomDataDeaTest;
import dk.alexandra.fresco.lib.statistics.DEASolverTests.TestDeaFixed1;
import dk.alexandra.fresco.lib.statistics.DEASolverTests.TestDeaFixed2;
import dk.alexandra.fresco.suite.spdz.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.storage.InitializeStorage;
import org.junit.Ignore;
import org.junit.Test;


/**
 * Tests for the DEASolver.
 * 
 */
public class TestSpdzDEASolver2Parties extends AbstractSpdzTest {

  @Test
  public void test_DEASolver_2_Sequential_batched_dummy_minimize_1() throws Exception {
    runTest(new TestDeaFixed2(DEASolver.AnalysisType.INPUT_EFFICIENCY),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_DEASolver_2_Sequential_batched_dummy_minimize_2() throws Exception {
    runTest(new TestDeaFixed1(DEASolver.AnalysisType.INPUT_EFFICIENCY),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_DEASolver_2_Sequential_batched_dummy_maximize_fixed_data_1() throws Exception {
    runTest(new TestDeaFixed1(AnalysisType.OUTPUT_EFFICIENCY),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_DEASolver_2_Sequential_batched_dummy_maximize_fixed_data_2() throws Exception {
    runTest(new TestDeaFixed2(AnalysisType.OUTPUT_EFFICIENCY),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET,
        PreprocessingStrategy.DUMMY, 2);
  }


  @Test
  public void test_DEASolver_2_Sequential_batched_dummy_maximize() throws Exception {
    runTest(new RandomDataDeaTest(5, 1, 30, 3, DEASolver.AnalysisType.OUTPUT_EFFICIENCY),
        EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET,
        PreprocessingStrategy.DUMMY, 2);
  }

  @Test
  public void test_DEASolver_2_Sequential_dummy() throws Exception {
    runTest(new RandomDataDeaTest(2, 1, 5, 1, DEASolver.AnalysisType.OUTPUT_EFFICIENCY),
        EvaluationStrategy.SEQUENTIAL, NetworkingStrategy.KRYONET, PreprocessingStrategy.DUMMY, 2);
  }


  // Ignoring the streamed tests since they take too long with respect to generating preprocessed
  // material
  // Note that the poor performance of non-batched evaulation is most likely also the case here.
  // TODO: Maybe add the @Category(IntegrationTest.class) instead of @Ignore.
  @Ignore
  @Test
  public void test_DEASolver_2_Sequential_batched_streamed() throws Exception {
    int noOfThreads = 1;
    InitializeStorage.cleanup();
    try {
      InitializeStorage.initStreamedStorage(new FilebasedStreamedStorageImpl(new InMemoryStorage()),
          2, noOfThreads, 20000, 500, 800000, 3000);
      runTest(
          new RandomDataDeaTest(4, 1, 10, 2, DEASolver.AnalysisType.OUTPUT_EFFICIENCY),
          EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET,
          PreprocessingStrategy.STATIC, 2);
    } finally {
      InitializeStorage.cleanup();
    }
  }


  @Test
  @Ignore
  public void test_DEASolver_2_SequentialBatched_streamed() throws Exception {
    int noOfThreads = 2;
    InitializeStorage.cleanup();
    try {
      InitializeStorage.initStreamedStorage(new FilebasedStreamedStorageImpl(new InMemoryStorage()),
          2, noOfThreads, 20000, 500, 800000, 3000);
      runTest(
          new RandomDataDeaTest(4, 1, 10, 2, DEASolver.AnalysisType.OUTPUT_EFFICIENCY),
          EvaluationStrategy.SEQUENTIAL_BATCHED, NetworkingStrategy.KRYONET,
          PreprocessingStrategy.STATIC, 2);
    } finally {
      InitializeStorage.cleanup();
    }
  }
}
