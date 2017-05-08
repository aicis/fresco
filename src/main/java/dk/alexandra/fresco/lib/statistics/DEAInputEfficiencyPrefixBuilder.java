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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.debug.MarkerProtocolImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AppendableProtocolProducer;
import dk.alexandra.fresco.lib.helper.CopyProtocolImpl;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.lp.LPPrefix;
import dk.alexandra.fresco.lib.lp.LPTableau;
import dk.alexandra.fresco.lib.lp.Matrix;
import dk.alexandra.fresco.lib.lp.OpenAndPrintProtocol;
import dk.alexandra.fresco.lib.lp.SimpleLPPrefix;

/**
 * Temporary class - should be merged with DEAPrefixBuilder with a flag to choose which one you want.
 * @author Kasper Damgaard
 *
 */
public class DEAPrefixBuilderMinimum extends DEAPrefixBuilder{

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
		
		SequentialProtocolProducer seqGP = new SequentialProtocolProducer();
		
		if (this.prefix != null) {
			seqGP.append(this.prefix);
		}
		
		/*
		 * Negate the target inputs
		 */
		ParallelProtocolProducer negateTargetInputs = new ParallelProtocolProducer();
		OInt negativeOne = provider.getOInt(BigInteger.valueOf(-1));
		SInt[] negatedTargetInputs = new SInt[this.targetInputs.size()];
		for(int i = 0; i < this.targetInputs.size(); i++) {
			negatedTargetInputs[i] = provider.getSInt();
			SInt inp = this.targetInputs.get(i);			
			negateTargetInputs.append(provider.getMultProtocol(negativeOne,
					inp, negatedTargetInputs[i]));
		}
		seqGP.append(negateTargetInputs);
		
		
		/*
		 * First copy the target values to the basis. This ensures that the
		 * target values are in the basis thus the score must at least be 1.
		 */
		ParallelProtocolProducer copyTargetToBasis = new ParallelProtocolProducer();
		List<SInt[]> newBasisInputs = new LinkedList<SInt[]>();
		List<SInt[]> newBasisOutputs = new LinkedList<SInt[]>();

		ListIterator<SInt[]> basisIt = basisInputs.listIterator();
		ListIterator<SInt> targetIt = targetInputs.listIterator();
		while (basisIt.hasNext() && targetIt.hasNext()) {
			SInt[] basisInput = basisIt.next();
			SInt targetInput = targetIt.next();
			SInt[] newInputs = new SInt[basisInput.length + 1];
			SInt copy = provider.getSInt();
			copyTargetToBasis
					.append(new CopyProtocolImpl<SInt>(targetInput, copy));
			newInputs[0] = copy;
			System.arraycopy(basisInput, 0, newInputs, 1, basisInput.length);
			
			newBasisInputs.add(newInputs);
		}
		this.basisInputs = newBasisInputs;
		basisIt = basisOutputs.listIterator();
		targetIt = targetOutputs.listIterator();
		while (basisIt.hasNext() && targetIt.hasNext()) {
			SInt[] basisOutput = basisIt.next();
			SInt targetOutput = targetIt.next();
			SInt[] newOutputs = new SInt[basisOutput.length + 1];
			SInt copy = provider.getSInt();
			copyTargetToBasis.append(new CopyProtocolImpl<SInt>(targetOutput, copy));
			System.arraycopy(basisOutput, 0, newOutputs, 1, basisOutput.length);
			newOutputs[0] = copy;
			newBasisOutputs.add(newOutputs);
		}
		this.basisOutputs = newBasisOutputs;
		seqGP.append(copyTargetToBasis);
		
		/*
		 * NeProtocol the basis output
		 */
		int lambdas = basisInputs.get(0).length;

		ParallelProtocolProducer negateBasisOutput = new ParallelProtocolProducer();
		List<SInt[]> negatedBasisOutputs = new ArrayList<SInt[]>(basisOutputs.size());
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
		seqGP.append(negateBasisOutput);
		
		int constraints = basisInputs.size() + basisOutputs.size() + 1;
		int slackvariables = constraints;
		int variables = lambdas + slackvariables + 1;
		SInt[][] slack = getIdentity(slackvariables, provider);
		SInt[][] C = new SInt[constraints][variables];
		for (int i = 0; i < basisInputs.size(); i++) {
			C[i] = inputRow(negatedTargetInputs[i], basisInputs.get(i), slack[i], provider, seqGP);
		}

		for (int i = basisInputs.size(); i < constraints - 1; i++) {
			C[i] = outputRow(negatedBasisOutputs.get(i - basisInputs.size()),
					targetOutputs.get(i - basisInputs.size()), slack[i], provider, seqGP);
		}
		C[C.length - 1] = lambdaRow(lambdas, slack[C.length - 1], provider);

		SInt F[] = fVector(variables, lambdas, provider);
		SInt B[] = bVector(constraints, negatedTargetInputs,
				provider);
		SInt z = provider.getSInt(0);
		SInt pivot = negatedTargetInputs[0]; //do a single pivot with hardcoded input 0 as the first one.
		SInt[] basis = new SInt[constraints];		
		
		LPTableau tab = new LPTableau(new Matrix<SInt>(C), B, F, z);
		Matrix<SInt> updateMatrix = generateUpdateMatrix(targetInputs.toArray(new SInt[0]), constraints+1, provider, seqGP); 				
		
		seqGP.append(tab.toString(provider));
		seqGP.append(new OpenAndPrintProtocol("U: ", updateMatrix.toArray(), provider));
		
		return new SimpleLPPrefix(updateMatrix, tab, pivot, basis, seqGP);
	}

	/**
	 * Creates a matrix with the properties:
	 * 
	 *  1 0  0  0  0 0
	 *  b -a 0  0  0 0
	 *  c 0 -a  0  0 0
	 *  0 0  0 -a  0 0
	 *  0 0  0  0 -a 0
	 *  1 0  0  0  0 -a
	 * 
	 * @param targetInputs
	 * @param dimension
	 * @param bnf
	 * @return
	 */
	private static Matrix<SInt> generateUpdateMatrix(SInt[] targetInputs, int dimension, BasicNumericFactory bnf, AppendableProtocolProducer app) {
		SInt[][] matrix = new SInt[dimension][dimension];
		//handle the first targetInputs.length rows
		for (int i = 0; i < targetInputs.length; i++) {
			for (int j = 0; j < dimension; j++) {
				if(j == 0) {
					//use target input values except for the pivot element
					if(i == 0) {
						matrix[i][j] = bnf.getSInt(1);	
					} else {
						matrix[i][j] = targetInputs[i];
					}
				} else if (i == j) {
					//use the pivot element as the diagonal
					SInt neg = bnf.getSInt();
					app.append(bnf.getMultProtocol(bnf.getOInt(BigInteger.valueOf(-1)), targetInputs[0], neg));
					matrix[i][j] = neg;
				} else {
					matrix[i][j] = bnf.getSInt(0);
				}
			}
		}
		
		//handle the rest of the rows
		for (int i = targetInputs.length; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {				
				if (i == j) {
					//use the pivot element as the diagonal
					SInt neg = bnf.getSInt();
					app.append(bnf.getMultProtocol(bnf.getOInt(BigInteger.valueOf(-1)), targetInputs[0], neg));
					matrix[i][j] = neg;
				} else {
					matrix[i][j] = bnf.getSInt(0);
				}
			}
		}
		
		//set bottom, leftmost to 1.
		matrix[dimension-1][0] = bnf.getSInt(1);
		return new Matrix<SInt>(matrix);
	}
	
	private static SInt[] fVector(int size, int lambdas, BasicNumericFactory provider) {
		SInt[] F = new SInt[size];
		int index = 0;
		// theta should be minimized. Everything else should not count towards the objective function.
		F[0] = provider.getSInt(1);
		index++;
		while (index < lambdas + 1) {
			F[index] = provider.getSInt(0);
			index++;
		}
		// Slack variables do not contribute to cost
		while (index < size) {
			F[index] = provider.getSInt(0);
			index++;
		}
		return F;
	}

	private static SInt[] bVector(int size, SInt[] inputs,
			BasicNumericFactory provider) {
		SInt[] B = new SInt[size];
		int index = 0;
		for (SInt input : inputs) {
			B[index] = input;
			index++;
		}
		// For each output constraint B is zero
		while (index < size - 1) {
			B[index] = provider.getSInt(0);
			index++;
		}
		// For the lambda constraint B is one
		B[index] = provider.getSInt(1);
		return B;
	}

	private static SInt[] inputRow(SInt targetInput, SInt[] basisInputs, SInt[] slackVariables,
			BasicNumericFactory provider, AppendableProtocolProducer app) {
		SInt[] row = new SInt[1 + basisInputs.length + slackVariables.length];
		int index = 0;		
		row[index++] = targetInput;	
	
		for (SInt input : basisInputs) {
			SInt sub = provider.getSInt();
			//Even though this is an add protocol, it is really a sub since we aforehand did a negate of targetInput
			app.append(provider.getAddProtocol(input, targetInput, sub));
			row[index] = sub;
			index++;
		}
		for (SInt slack : slackVariables) {
			row[index] = slack;
			index++;
		}
		return row;
	}

	private static SInt[] outputRow(SInt[] outputs, SInt targetOutput,
			SInt[] slackVariables, BasicNumericFactory bnf, AppendableProtocolProducer app) {
		SInt[] row = new SInt[outputs.length + slackVariables.length + 1];
		int index = 0;
		row[index++] = bnf.getSInt(0);
		for (SInt output : outputs) {
			SInt add = bnf.getSInt();
			app.append(bnf.getAddProtocol(output, targetOutput, add));
			row[index] = add;
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
		return new DEAPrefixBuilderMinimum();
	}

}
