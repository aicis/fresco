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
import java.util.logging.Logger;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AlgebraUtil;
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
public class DEAPrefixBuilder {

	private List<SInt[]> basisInputs, basisOutputs;
	private List<SInt> targetInputs, targetOutputs;
	private BasicNumericFactory provider;
	private ProtocolProducer prefix;

	private final Logger log = Logger.getLogger("DEAPREFIX");
	// A value no benchmarking result should be larger than. Note the benchmarking results are of the form
		// \theta = "the factor a farmer can do better than he currently does" and thus is not necessarily upper bounded.
	private static final int BENCHMARKING_BIG_M = 1000000;

	/**
	 * Returns a list of SInt arrays representing the basis inputs, i.e., those
	 * with which the target is compared against. One for each input type.
	 * 
	 * @return a list of SInt arrays representing the basis inputs.
	 */
	public List<SInt[]> getBasisInputs() {
		return basisInputs;
	}

	/**
	 * Returns a list of SInt arrays representing the basis outputs, i.e., those
	 * with which the target is compared against. One for each output type.
	 * 
	 * @return a list of SInt arrays representing the basis output.
	 */
	public List<SInt[]> getBasisOutputs() {
		return basisOutputs;
	}

	/**
	 * Returns a list of SInts. One for each input type.
	 * 
	 * @return a list of SInts representing the target inputs.
	 */
	public List<SInt> getTargetInputs() {
		return targetInputs;
	}

	/**
	 * Returns a list of SInts. One for each output type.
	 * 
	 * @return a list of SInts representing the target output.
	 */
	public List<SInt> getTargetOutputs() {
		return targetOutputs;
	}

	/**
	 * Returns the provider of this builder
	 * 
	 * @return the provider
	 */
	public BasicNumericFactory getProvider() {
		return provider;
	}

	/**
	 * Returns the GateProducer used to populate the inputs and outputs of this
	 * builder.
	 * 
	 * @return the prefix of this builder
	 */
	public ProtocolProducer getCircuit() {
		return prefix;
	}

	/**
	 * Constructs an empty builder
	 */
	public DEAPrefixBuilder() {
		basisInputs = new ArrayList<SInt[]>();
		basisOutputs = new ArrayList<SInt[]>();
		targetInputs = new ArrayList<SInt>();
		targetOutputs = new ArrayList<SInt>();
		provider = null;
		prefix = null;
	}

	/**
	 * Sets a list of SInt arrays used as the basis inputs by this builder. One
	 * array for each input type. Note that the order of this list should match
	 * that of the list given for target inputs. Otherwise the left and right
	 * hand sides of the constraints build by this builder will not match.
	 * 
	 * @param basisInputs
	 * @return this builder modified to set the list of basis inputs
	 */
	public DEAPrefixBuilder basisInputs(List<SInt[]> basisInputs) {
		this.basisInputs = basisInputs;
		return this;
	}

	/**
	 * Adds a SInt array to the list of basis inputs by this builder. Note that
	 * the order of this list should match that of the list given for target
	 * inputs. Otherwise the left and right hand sides of the constraints build
	 * by this builder will not match.
	 * 
	 * @param inputs
	 * @return this builder modified to add the input to the list of basis
	 *         inputs
	 */
	public DEAPrefixBuilder addBasisInput(SInt[] inputs) {
		if (this.basisInputs == null) {
			this.basisInputs = new ArrayList<SInt[]>();
		}
		this.basisInputs.add(inputs);
		return this;
	}

	/**
	 * Sets a list of SInt arrays used as the basis outputs by this builder. One
	 * array for each output type. Note that the order of this list should match
	 * that of the list given for target outputs. Otherwise the left and right
	 * hand sides of the constraints build by this builder will not match. Note
	 * also that basis output values should be negated compared to the values in
	 * the database.
	 * 
	 * @param basisOutputs
	 * @return this builder modified to set the list of basis outputs
	 */
	public DEAPrefixBuilder basisOutputs(List<SInt[]> basisOutputs) {
		this.basisOutputs = basisOutputs;
		return this;
	}

	/**
	 * Adds a SInt array to the list of outputs of basis by this builder. Note
	 * that the order of this list should match that of the list given for
	 * target outputs. Otherwise the left and right hand sides of the
	 * constraints build by this builder will not match.
	 * 
	 * @param outputs
	 * @return this builder modified to add the output to the list of basis
	 *         outputs
	 */
	public DEAPrefixBuilder addBasisOutput(SInt[] outputs) {
		if (this.basisOutputs == null) {
			this.basisOutputs = new ArrayList<SInt[]>();
		}
		this.basisOutputs.add(outputs);
		return this;
	}

	/**
	 * Sets a list of SInts used as the target inputs by this builder. One array
	 * for each input type. Note that the order of this list should match that
	 * of the list given for basis inputs. Otherwise the left and right hand
	 * sides of the constraints build by this builder will not match.
	 * 
	 * @param targetInputs
	 * @return this builder modified to set the list of target inputs
	 */
	public DEAPrefixBuilder targetInputs(List<SInt> targetInputs) {
		this.targetInputs = targetInputs;
		return this;
	}

	/**
	 * Adds a SInt to the list of inputs of the target by this builder. Note
	 * that the order of this list should match that of the list given for basis
	 * inputs. Otherwise the left and right hand sides of the constraints build
	 * by this builder will not match.
	 * 
	 * @param targetInput
	 * @return this builder modified to add the input to the list of the target
	 *         inputs
	 */
	public DEAPrefixBuilder addTargetInput(SInt targetInput) {
		if (this.targetInputs == null) {
			this.targetInputs = new ArrayList<SInt>();
		}
		this.targetInputs.add(targetInput);
		return this;
	}

	/**
	 * Sets a list of SInts used as the outputs of the target by this builder.
	 * One array for each output type. Note that the order of this list should
	 * match that of the list given for basis outputs. Otherwise the left and
	 * right hand sides of the constraints build by this builder will not match.
	 * 
	 * @param targetOutputs
	 * @return this builder modified to set the list of target outputs
	 */
	public DEAPrefixBuilder targetOutputs(List<SInt> targetOutputs) {
		this.targetOutputs = targetOutputs;
		return this;
	}

	/**
	 * Adds a SInt to the list of outputs of the target by this builder. Note
	 * that the order of this list should match that of the list given for basis
	 * outputs. Otherwise the left and right hand sides of the constraints build
	 * by this builder will not match.
	 * 
	 * @param targetOutput
	 * @return this builder modified to add the output to the list of the target
	 *         outputs
	 */
	public DEAPrefixBuilder addTargetOutput(SInt targetOutput) {
		if (this.targetOutputs == null) {
			this.targetOutputs = new ArrayList<SInt>();
		}
		this.targetOutputs.add(targetOutput);
		return this;
	}

	/**
	 * Sets the provider to be used by this builder. Note this should be the
	 * provider that provided the SInts and the prefix of this builder.
	 * 
	 * @param provider
	 * @return this builder with the provider set
	 */
	public DEAPrefixBuilder provider(BasicNumericFactory provider) {
		this.provider = provider;
		return this;
	}

	/**
	 * Sets the prefix of this builder which should populate all the SInts used
	 * be this builder
	 * 
	 * @param prefix
	 * @return this builder with the prefix set
	 */
	public DEAPrefixBuilder prefix(ProtocolProducer prefix) {
		this.prefix = prefix;
		return this;
	}

	/**
	 * Adds to the prefix of this builder. The new prefix is will run the
	 * current prefix in parallel with the given additional prefix. The joined
	 * prefix should populate all the SInts used be this builder
	 * 
	 * @param prefix
	 * @return this builder with added prefix
	 */
	public DEAPrefixBuilder addPrefix(ProtocolProducer addPrefix) {
		if (this.prefix == null) {
			this.prefix = addPrefix;
		} else {
			ProtocolProducer gp = new ParallelProtocolProducer(this.prefix, addPrefix);
			this.prefix = gp;
		}
		return this;
	}

	/**
	 * Checks if this builder is ready to be build. I.e. if all needed values
	 * have been set.
	 * 
	 * @return true if all needed values are set. false otherwise.
	 */
	public boolean ready() {
		boolean ready = true;
		ready = (basisInputs != null); // &&
		// basisOutputs != null &&
		// targetInputs != null &&
		// targetOutputs != null &&
		// provider != null &&
		// prefix != null &&
		// !basisInputs.isEmpty() &&
		// !basisOutputs.isEmpty() &&
		// !targetInputs.isEmpty() &&
		// !targetOutputs.isEmpty());
		return ready;
	}

	/**
	 * Appends the inputs and outputs of an other builder to this builder. The
	 * prefix is changed to be one evaluating the prefix of this builder and the
	 * other builder in parallel. Note this builder and the other builder must
	 * use the same provider or an IllegalArgumentException will be thrown. Also
	 * note the method will modify this builder, but not the appended builder.
	 * 
	 * @param builder
	 *            a builder
	 * @return this builder with the other builder appended
	 */
	public DEAPrefixBuilder append(DEAPrefixBuilder builder) {
		if (this.provider != builder.getProvider()) {
			throw new IllegalArgumentException(
					"Cannot append builder. Builders are not using the same provider.");
		}

		if (this.basisInputs == null) {
			this.basisInputs = builder.getBasisInputs();
		} else {
			if (builder.getBasisInputs() != null) {
				this.basisInputs.addAll(builder.getBasisInputs());
			}
		}

		if (this.basisOutputs == null) {
			this.basisOutputs = builder.getBasisOutputs();
		} else {
			if (builder.getBasisOutputs() != null) {
				this.basisOutputs.addAll(builder.getBasisOutputs());
			}
		}

		if (this.targetInputs == null) {
			this.targetInputs = builder.getTargetInputs();
		} else {
			if (builder.getTargetInputs() != null) {
				this.targetInputs.addAll(builder.getTargetInputs());
			}
		}

		if (this.targetOutputs == null) {
			this.targetOutputs = builder.getTargetOutputs();
		} else {
			if (builder.getTargetOutputs() != null) {
				this.targetOutputs.addAll(builder.getTargetOutputs());
			}
		}

		if (this.prefix == null) {
			this.prefix = builder.prefix;
		} else {
			if (builder.prefix != null) {
				ParallelProtocolProducer par = new ParallelProtocolProducer(
						this.prefix, builder.getCircuit());
				this.prefix = par;
			}
		}
		return this;
	}

	/**
	 * Returns a copy of this prefix builder, i.e. the SInts in the prefix of
	 * the returned builder will be copied from those in the prefix of this
	 * builder.
	 * 
	 * The copying will be done by the circuit part of the prefix of the
	 * returned builder. This means that the circuit of the prefix given by this
	 * builder should be evaluated strictly before that of the returned builder
	 * copy.
	 * 
	 * @return a copy of this prefix builder
	 */
	public DEAPrefixBuilder copy() {
		List<SInt[]> copyBasisInputs = new ArrayList<SInt[]>(this
				.getBasisInputs().size());
		ParallelProtocolProducer copyProducer = new ParallelProtocolProducer();
		for (SInt[] inputs : this.getBasisInputs()) {
			SInt[] copyInputs = AlgebraUtil
					.sIntFill(new SInt[inputs.length], provider);
			for (int i = 0; i < inputs.length; i++) {
				copyProducer.append(new CopyProtocolImpl<SInt>(inputs[i],
						copyInputs[i]));
			}
			copyBasisInputs.add(copyInputs);
		}

		List<SInt[]> copyBasisOutputs = new ArrayList<SInt[]>(this
				.getBasisOutputs().size());
		for (SInt[] outputs : this.getBasisOutputs()) {
			SInt[] copyOutputs = AlgebraUtil.sIntFill(new SInt[outputs.length],
					provider);
			for (int i = 0; i < outputs.length; i++) {
				copyProducer.append(new CopyProtocolImpl<SInt>(outputs[i],
						copyOutputs[i]));
			}
			copyBasisOutputs.add(copyOutputs);
		}

		List<SInt> copyTargetInputs = new ArrayList<SInt>(this
				.getTargetInputs().size());
		for (SInt input : this.getTargetInputs()) {
			SInt copyInput = provider.getSInt();
			copyProducer.append(new CopyProtocolImpl<SInt>(input, copyInput));
			copyTargetInputs.add(copyInput);
		}

		List<SInt> copyTargetOutputs = new ArrayList<SInt>(this
				.getTargetOutputs().size());
		for (SInt output : this.getTargetOutputs()) {
			SInt copyOutput = provider.getSInt();
			copyProducer.append(new CopyProtocolImpl<SInt>(output, copyOutput));
			copyTargetOutputs.add(copyOutput);
		}

		DEAPrefixBuilder copyBuilder = new DEAPrefixBuilder();
		copyBuilder = copyBuilder.basisInputs(copyBasisInputs)
				.basisOutputs(copyBasisOutputs).targetInputs(copyTargetInputs)
				.targetOutputs(copyTargetOutputs)
				.prefix(new SequentialProtocolProducer()).provider(this.provider);

		if(this.prefix != null){
			this.prefix = new SequentialProtocolProducer(this.prefix, copyProducer);
		} else {
			this.prefix = new SequentialProtocolProducer(copyProducer);
		}
		return copyBuilder;
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
		while (basisIt.hasNext() && targetIt.hasNext()) {
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
		while (basisIt.hasNext() && targetIt.hasNext()) {
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
		LPTableau tab = new LPTableau(new Matrix<SInt>(C), B, F, z);
		Matrix<SInt> updateMatrix = new Matrix<SInt>(getIdentity(
				constraints + 1, provider));
		SequentialProtocolProducer seqGP = new SequentialProtocolProducer();
		if (this.prefix != null) {
			seqGP.append(this.prefix);
		}
		seqGP.append(copyTargetToBasis);
		seqGP.append(negateBasisOutput);
		return new SimpleLPPrefix(updateMatrix, tab, pivot, seqGP);
	}

	private boolean consistent() {
		boolean consistent = true;
		boolean printedError = false;
		if (!ready()) {
			return false;
		}
		consistent = consistent && (basisInputs.size() == targetInputs.size());
		if (!consistent) {
			log.warning("BasisInputs size (" + basisInputs.size()
					+ ")does not equal targetInputs size ("
					+ targetInputs.size() + ")");
			printedError = true;
		}
		consistent = consistent
				&& (basisOutputs.size() == targetOutputs.size());
		if (!consistent && !printedError) {
			log.warning("BasisOutputs size (" + basisOutputs.size()
					+ ")does not equal targetOutputs size ("
					+ targetOutputs.size() + ")");
			printedError = true;
		}
		int dbSize = basisInputs.get(0).length;
		for (SInt[] inputs : basisInputs) {
			consistent = consistent && (inputs.length == dbSize);
		}
		if (!consistent && !printedError) {
			log.warning("All basisInputs does not agree on the length of the SInt array");
			printedError = true;
		}
		for (SInt[] outputs : basisOutputs) {
			consistent = consistent && (outputs.length == dbSize);
		}
		if (!consistent && !printedError) {
			log.warning("All basisOutputs does not agree on the length of the SInt array");
		}
		return consistent;
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

	private static SInt[][] getIdentity(int dimension, BasicNumericFactory provider) {
		SInt[][] identity = new SInt[dimension][dimension];
		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				if (i == j) {
					identity[i][j] = provider.getSInt(1);
				} else {
					identity[i][j] = provider.getSInt(0);
				}
			}
		}
		return identity;
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

	private static SInt[] fillPublicVector(int size, BigInteger value,
			BasicNumericFactory provider) {
		SInt[] vector = new SInt[size];
		for (int i = 0; i < size; i++) {
			vector[i] = provider.getSInt(value);
		}
		return vector;
	}

}
