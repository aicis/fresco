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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.CopyProtocolImpl;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.lp.LPPrefix;
import dk.alexandra.fresco.lib.lp.LPTableau;
import dk.alexandra.fresco.lib.lp.Matrix;
import dk.alexandra.fresco.lib.lp.SimpleLPPrefix;

/**
 * A helper class used to build the LPPrefix from the SInts representing the
 * input and outputs of DEA instance and the prefix GateProducer that populates
 * these SInts. Note that we use the words "output" and "input" in terms of the
 * DEA instance. I.e. in the way the economists use these words.
 * 
 * 
 */
public class DEAPrefixBuilderMaximize extends DEAPrefixBuilder {

	// A value no benchmarking result should be larger than. Note the benchmarking results are of the form
		// \theta = "the factor a farmer can do better than he currently does" and thus is not necessarily upper bounded.
	private static final int BENCHMARKING_BIG_M = 1000000;

	/**
	 * Constructs an empty builder
	 */
	public DEAPrefixBuilderMaximize() {
		super();
	}

	/**
	 * Builds an LPPrefix from the given SInts, provider and prefix. Attempts to
	 * check if the values given are consistent before building the prefix. If
	 * this is not the case a IllegalStateException will be thrown.
	 * 
	 * @return an LPPrefix
	 */
	public LPPrefix build() {
		// TODO: this should only be done once
		if (!ready()) {
			throw new IllegalStateException(
					"Builder not ready to build LPPrefix not enough data supplied!");
		}
		if (!consistent()) {
			throw new IllegalStateException(
					"Trying to build LPPrefix from inconsistent data!");
		}
		/*
		 * First copy the target values to the basis. This ensures that the
		 * target values are in the basis thus the score must at least be 1.
		 */
		ParallelProtocolProducer copyTargetToBasis = new ParallelProtocolProducer();
		List<SInt[]> newBasisInputs = new LinkedList<SInt[]>();
		List<SInt[]> newBasisOutputs = new LinkedList<SInt[]>();

		ListIterator<SInt[]> basisIt = basisInputs.listIterator();
		ListIterator<SInt> targetIt = targetInputs.listIterator();
		while (basisIt.hasNext()) {
			SInt[] basisInput = basisIt.next();
			SInt targetInput = targetIt.next();
			SInt[] newInputs = new SInt[basisInput.length + 1];
			SInt copy = provider.getSInt();
			copyTargetToBasis
					.append(new CopyProtocolImpl<SInt>(targetInput, copy));
			System.arraycopy(basisInput, 0, newInputs, 0, basisInput.length);
			newInputs[newInputs.length - 1] = copy;
			newBasisInputs.add(newInputs);
		}
		this.basisInputs = newBasisInputs;
		basisIt = basisOutputs.listIterator();
		targetIt = targetOutputs.listIterator();
		while (basisIt.hasNext()) {
			SInt[] basisOutput = basisIt.next();
			SInt targetOutput = targetIt.next();
			SInt[] newOutputs = new SInt[basisOutput.length + 1];
			SInt copy = provider.getSInt();
			copyTargetToBasis.append(new CopyProtocolImpl<SInt>(targetOutput, copy));
			System.arraycopy(basisOutput, 0, newOutputs, 0, basisOutput.length);
			newOutputs[newOutputs.length - 1] = copy;
			newBasisOutputs.add(newOutputs);
		}
		this.basisOutputs = newBasisOutputs;
		
		/*
		 * NeProtocol the basis output 
		 */
		int lambdas = basisInputs.get(0).length;

		ParallelProtocolProducer negateBasisOutput = new ParallelProtocolProducer();
		List<SInt[]> negatedBasisOutputs = new ArrayList<SInt[]>(basisOutputs.size());
		OInt negativeOne = provider.getOInt(BigInteger.valueOf(-1));
		for (SInt[] outputs : basisOutputs) {
			SInt[] negOutputs = new SInt[outputs.length];
			int idx = 0;
			for (SInt output : outputs) {
				negOutputs[idx] = provider.getSInt();
				negateBasisOutput.append(provider.getMultProtocol(negativeOne,
						output, negOutputs[idx]));
				idx++;
			}
			negatedBasisOutputs.add(negOutputs);	
		}

		int constraints = basisInputs.size() + basisOutputs.size() + 1;
		int slackvariables = constraints;
		int variables = lambdas + slackvariables + 1;
		SInt[][] slack = getIdentity(slackvariables, provider);
		SInt[][] C = new SInt[constraints][variables];
		for (int i = 0; i < basisInputs.size(); i++) {
			C[i] = inputRow(basisInputs.get(i), slack[i], provider);
		}

		for (int i = basisInputs.size(); i < constraints - 1; i++) {
			C[i] = outputRow(negatedBasisOutputs.get(i - basisInputs.size()),
					targetOutputs.get(i - basisInputs.size()), slack[i]);
		}
		C[C.length - 1] = lambdaRow(lambdas, slack[C.length - 1], provider);

		SInt F[] = fVector(variables, lambdas, provider);
		SInt B[] = bVector(constraints, targetInputs.toArray(new SInt[1]),
				provider);
		SInt z = provider.getSInt(0);
		SInt pivot = provider.getSInt(1);
		SInt[] basis = new SInt[constraints];		
		
		LPTableau tab = new LPTableau(new Matrix<SInt>(C), B, F, z);
		Matrix<SInt> updateMatrix = new Matrix<SInt>(getIdentity(
				constraints + 1, provider));
		SequentialProtocolProducer seqGP = new SequentialProtocolProducer();
		if (this.prefix != null) {
			seqGP.append(this.prefix);
		}
		seqGP.append(copyTargetToBasis);
		seqGP.append(negateBasisOutput);
		return new SimpleLPPrefix(updateMatrix, tab, pivot, basis, seqGP);
	}

	private static SInt[] fVector(int size, int lambdas, BasicNumericFactory provider) {
		SInt[] F = new SInt[size];
		int index = 0;
		// Delta has coefficient 1
		F[0] = provider.getSInt(-1);
		index++;
		// Make sure there are lambdas > 0
		while (index < lambdas + 1) {
			F[index] = provider.getSInt(0 - BENCHMARKING_BIG_M);
			index++;
		}
		// Slack variables do not contribute to cost
		while (index < size) {
			F[index] = provider.getSInt(0);
			index++;
		}
		return F;
	}

	private static SInt[] bVector(int size, SInt[] bankInputs,
			BasicNumericFactory provider) {
		SInt[] B = new SInt[size];
		int index = 0;
		for (SInt input : bankInputs) {
			B[index] = input;
			index++;
		}
		// For each bank output constraint B is zero
		while (index < size - 1) {
			B[index] = provider.getSInt(0);
			index++;
		}
		// For the lambda constraint B is one
		B[index] = provider.getSInt(1);
		return B;
	}

	private static SInt[] inputRow(SInt[] vflInputs, SInt[] slackVariables,
			BasicNumericFactory provider) {
		SInt[] row = new SInt[vflInputs.length + slackVariables.length + 1];
		int index = 0;
		row[0] = provider.getSInt(0);
		index++;
		for (SInt vflInput : vflInputs) {
			row[index] = vflInput;
			index++;
		}
		for (SInt slack : slackVariables) {
			row[index] = slack;
			index++;
		}
		return row;
	}

	private static SInt[] outputRow(SInt[] vflOutputs, SInt bankOutput,
			SInt[] slackVariables) {
		SInt[] row = new SInt[vflOutputs.length + slackVariables.length + 1];
		int index = 0;
		row[0] = bankOutput;
		index++;
		for (SInt vflOutput : vflOutputs) {
			row[index] = vflOutput;
			index++;
		}
		for (SInt slack : slackVariables) {
			row[index] = slack;
			index++;
		}
		return row;
	}

	private static SInt[] lambdaRow(int lambdas, SInt[] slackVariables,
			BasicNumericFactory provider) {
		SInt[] row = new SInt[lambdas + slackVariables.length + 1];
		int index = 0;
		row[0] = provider.getSInt(0);
		index++;
		while (index < lambdas + 1) {
			row[index] = provider.getSInt(BigInteger.ONE);
			index++;
		}
		for (SInt slack : slackVariables) {
			row[index] = slack;
			index++;
		}
		return row;
	}

	@Override
	public DEAPrefixBuilder createNewInstance() {
		return new DEAPrefixBuilderMaximize();
	}

}
