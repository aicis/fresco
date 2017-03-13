/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.framework.sce.evaluator;

import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.configuration.ConfigurationException;

public enum EvaluationStrategy {
    SEQUENTIAL, PARALLEL, PARALLEL_BATCHED, SEQUENTIAL_BATCHED;

	public static ProtocolEvaluator fromString(String evalStr) throws ConfigurationException {
		EvaluationStrategy evalStrategy = EvaluationStrategy.valueOf(evalStr.toUpperCase());
		switch(evalStrategy) {
		case SEQUENTIAL:
			return new SequentialEvaluator();
		case PARALLEL:
			return new ParallelEvaluator();
		case SEQUENTIAL_BATCHED:
			return new BatchedSequentialEvaluator();
		case PARALLEL_BATCHED:
			return new BatchedParallelEvaluator();
		default:
			throw new ConfigurationException("Unrecognized evaluation strategy:" + evalStr);
		}
	}
	
	public static ProtocolEvaluator fromEnum(EvaluationStrategy strat) throws ConfigurationException {
		switch(strat) {
		case SEQUENTIAL:
			return new SequentialEvaluator();
		case PARALLEL:
			return new ParallelEvaluator();
		case SEQUENTIAL_BATCHED:
			return new BatchedSequentialEvaluator();
		case PARALLEL_BATCHED:
			return new BatchedParallelEvaluator();
		default:
			throw new ConfigurationException("Unrecognized evaluation strategy:" + strat);
		}
	}
	
	public static String evaluatorToString(ProtocolEvaluator evaluator) {
		if(evaluator instanceof SequentialEvaluator) {
			return SEQUENTIAL.name();
		} else if(evaluator instanceof ParallelEvaluator) {
			return PARALLEL.name();
		}
		else if(evaluator instanceof BatchedSequentialEvaluator) {
			return SEQUENTIAL_BATCHED.name();
		}
		else if(evaluator instanceof BatchedParallelEvaluator) {
			return PARALLEL_BATCHED.name();
		} else {
			throw new ConfigurationException("Unrecognized evaluation strategy:" + evaluator.toString());
		}
	}
}
