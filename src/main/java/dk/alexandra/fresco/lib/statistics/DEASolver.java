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
package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.RandomFieldElementFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.lp.*;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Protocol for solving DEA problems.
 *
 * Given a dataset (two matrices of inputs and outputs) and a number of query
 * vectors, the protocol will compute how well the query vectors perform
 * compared to the dataset.
 *
 * The result/score of the computation must be converted to a double using Gauss
 * reduction to be meaningful. See the DEASolverTests for an example.
 *
 */
public class DEASolver implements Application {

	private static final long serialVersionUID = 7679664125131997196L;
	private List<List<SInt>> targetInputs, targetOutputs;
	private List<List<SInt>> inputDataSet, outputDataSet;

	private SInt[] optimal;
	private SInt[][] basis;
	private AnalysisType type;

	public enum AnalysisType { INPUT_EFFICIENCY, OUTPUT_EFFICIENCY }

	/**
	 * Construct a DEA problem for the solver to solve. The problem consists of
	 * 4 matrixes: 2 basis input/output matrices containing the dataset which
	 * the queries will be measured against
	 *
	 * 2 query input/output matrices containing the data to be evaluated.
	 *
	 * @param type
	 *         The type of analysis to do
	 * @param inputValues
	 *            Matrix of query input values
	 * @param outputValues
	 *            Matrix of query output values
	 * @param setInput
	 *            Matrix containing the basis input
	 * @param setOutput
	 *            Matrix containing the basis output
	 * @throws MPCException
	 */
	public DEASolver(AnalysisType type, List<List<SInt>> inputValues, List<List<SInt>> outputValues, List<List<SInt>> setInput,
			List<List<SInt>> setOutput) throws MPCException {
		this.type = type;
		this.targetInputs = inputValues;
		this.targetOutputs = outputValues;
		this.inputDataSet = setInput;
		this.outputDataSet = setOutput;
		if (!consistencyCheck()) {
			throw new MPCException("Inconsistent dataset / query data");
		}
	}

	/**
	 * Verify that the input is consistent
	 *
	 * @return If the input is consistent.
	 */
	private boolean consistencyCheck() {

		int inputVariables = inputDataSet.get(0).size();
		int outputVariables = outputDataSet.get(0).size();
		if (inputDataSet.size() != outputDataSet.size()) {
			return false;
		}
		if (targetInputs.size() != targetOutputs.size()) {
			return false;
		}
		for (List<SInt> x : targetInputs) {
			if (x.size() != inputVariables) {
				return false;
			}
		}
		for (List<SInt> x : inputDataSet) {
			if (x.size() != inputVariables) {
				return false;
			}
		}
		for (List<SInt> x : targetOutputs) {
			if (x.size() != outputVariables) {
				return false;
			}
		}
		for (List<SInt> x : outputDataSet) {
			if (x.size() != outputVariables) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ProtocolProducer prepareApplication(ProtocolFactory provider) {

		SequentialProtocolProducer seq = new SequentialProtocolProducer();

		LPPrefix[] prefixes = getPrefixWithSecretSharedValues((BasicNumericFactory) provider);

		BasicNumericFactory bnFactory = (BasicNumericFactory) provider;
		LocalInversionFactory localInvFactory = (LocalInversionFactory) provider;
		NumericBitFactory numericBitFactory = (NumericBitFactory) provider;
		ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) provider;
		PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) provider;
		RandomFieldElementFactory randFactory = (RandomFieldElementFactory) provider;

		// TODO get security parameter from somewhere
		LPFactory lpFactory = new LPFactoryImpl(64, bnFactory, localInvFactory, numericBitFactory, expFromOIntFactory,
				expFactory, randFactory);

		for (LPPrefix prefix : prefixes) {
			seq.append(prefix.getPrefix());
		}
		// TODO processing the prefixes in parallel, causes a null pointer.
		// Investigate why this is the case

		ParallelProtocolProducer parallelProtocolProducer = new ParallelProtocolProducer();
		seq.append(parallelProtocolProducer);

		optimal = new SInt[targetInputs.size()];
		this.basis = new SInt[targetInputs.size()][];

		for (int i = 0; i < targetInputs.size(); i++) {
			optimal[i] = bnFactory.getSInt();

			SInt pivot = prefixes[i].getPivot();
			LPTableau tableau = prefixes[i].getTableau();
			Matrix<SInt> update = prefixes[i].getUpdateMatrix();

			this.basis[i] = new SInt[tableau.getC().getHeight()];

			final ProtocolProducer solver = lpFactory.getLPSolverProtocol(tableau, update, pivot, basis[i]);
			final ProtocolProducer optimalComputer = lpFactory.getOptimalValueProtocol(update, tableau, pivot,
					optimal[i]);

			SequentialProtocolProducer iSeq = new SequentialProtocolProducer();
			iSeq.append(solver);
			iSeq.append(optimalComputer);

			parallelProtocolProducer.append(iSeq);
		}
		return seq;
	}

	public SInt[] getResult() {
		return this.optimal;
	}

	 /**
   * First array is the size of targetInputs, i.e. the number of LP instances
   * to compute. Innermost array will, after evaluation, contain the final
   * basis of the tableau, i.e. the variables that the basis consists of.
   *
   * @return The final basis.
   */
  public SInt[][] getBasis() {
    return this.basis;
  }

	private LPPrefix[] getPrefixWithSecretSharedValues(BasicNumericFactory provider) {
		int dataSetSize = this.inputDataSet.size();

		LPPrefix[] prefixes = new LPPrefix[this.targetInputs.size()];

		int lpInputs = this.inputDataSet.get(0).size();
		int lpOutputs = this.outputDataSet.get(0).size();
		SInt[][] basisInputs = new SInt[lpInputs][dataSetSize];
		SInt[][] basisOutputs = new SInt[lpOutputs][dataSetSize];

		for (int i = 0; i < dataSetSize; i++) {
			for (int j = 0; j < inputDataSet.get(i).size(); j++) {
				List<SInt> current = inputDataSet.get(i);
				basisInputs[j][i] = current.get(j);
			}
			for (int j = 0; j < outputDataSet.get(i).size(); j++) {
				List<SInt> current = outputDataSet.get(i);
				basisOutputs[j][i] = current.get(j);
			}
		}

		DEAPrefixBuilder basisBuilder = null;

		if(type == AnalysisType.INPUT_EFFICIENCY) {
			basisBuilder = new DEAInputEfficiencyPrefixBuilder();
		} else {
			basisBuilder = new DEAPrefixBuilderMaximize();
		}
		basisBuilder.provider(provider);
		basisBuilder.basisInputs(Arrays.asList(basisInputs));
		basisBuilder.basisOutputs(Arrays.asList(basisOutputs));

		DEAPrefixBuilder[] basisBuilderCopies = new DEAPrefixBuilder[this.targetInputs.size()];

		for (int i = 0; i < this.targetInputs.size(); i++) {
			if (i == 0) {
				basisBuilderCopies[i] = basisBuilder;
			} else {
				basisBuilderCopies[i] = basisBuilder.copy();
			}
		}

		for (int i = 0; i < this.targetInputs.size(); i++) {
			DEAPrefixBuilder targetBuilder = null;
			if(type == AnalysisType.INPUT_EFFICIENCY) {
				targetBuilder = new DEAInputEfficiencyPrefixBuilder();
			} else {
				targetBuilder = new DEAPrefixBuilderMaximize();
			}
			targetBuilder.provider(provider);
			List<SInt> current = targetInputs.get(i);
			for (int j = 0; j < current.size(); j++) {
				targetBuilder.addTargetInput(current.get(j));
			}
			current = targetOutputs.get(i);
			for (int j = 0; j < current.size(); j++) {
				targetBuilder.addTargetOutput(current.get(j));
			}
			basisBuilderCopies[i].append(targetBuilder);
			prefixes[i] = basisBuilderCopies[i].build();
		}
		return prefixes;
	}
}
