/*
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.framework.sce;

import dk.alexandra.fresco.framework.PerformanceLogger;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedProtocolEvaluatorPerformanceDecorator;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedSequentialEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.SequentialEvaluator;
import dk.alexandra.fresco.framework.sce.evaluator.SequentialProtocolEvaluatorPerformanceDecorator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;

public class SCEFactory {

  /**
   * Creates an instance of the Secure Computation Engine based on the given arguments. If the
   * performance flags are either null or the empty set, no performance will be logged.
   * 
   * @param protocolSuite The protocol suite to use when evaluating native protocols.
   * @param evaluator The evaluator to use when evaluating an application.
   * @param performanceFlags The performance parameters to measure. Set this to null or the empty
   *        set to avoid logging performance.
   * @return An instance of the Secure Computation Engine.
   */
  public static <ResourcePoolT extends ResourcePool, Builder extends ProtocolBuilder> SecureComputationEngine<ResourcePoolT, Builder> getSCEFromConfiguration(
      ProtocolSuite<ResourcePoolT, Builder> protocolSuite,
      ProtocolEvaluator<ResourcePoolT, Builder> evaluator, PerformanceLogger pl) {
    if (pl == null) {
      return new SecureComputationEngineImpl<>(protocolSuite, evaluator);
    } else {
      // Adding the decorators
      ProtocolEvaluator<ResourcePoolT, Builder> decoratedEvaluator = evaluator;
      if (evaluator instanceof SequentialEvaluator) {
        decoratedEvaluator = new SequentialProtocolEvaluatorPerformanceDecorator<>(pl);
      } else if (evaluator instanceof BatchedSequentialEvaluator) {
        decoratedEvaluator = new BatchedProtocolEvaluatorPerformanceDecorator<>(pl);
      }
      SecureComputationEngine<ResourcePoolT, Builder> sce =
          new SecureComputationEngineImpl<>(protocolSuite, decoratedEvaluator);
      return new SCEPerformanceDecorator<>(sce, evaluator, protocolSuite, pl);
    }
  }
}
