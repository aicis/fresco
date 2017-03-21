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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.linear.LinearConstraint;
import org.apache.commons.math3.optim.linear.LinearConstraintSet;
import org.apache.commons.math3.optim.linear.LinearObjectiveFunction;
import org.apache.commons.math3.optim.linear.NonNegativeConstraint;
import org.apache.commons.math3.optim.linear.Relationship;
import org.apache.commons.math3.optim.linear.SimplexSolver;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;


/**
 * Solves the DEA problem on a given plaintext dataset.
 * 
 */
public class PlaintextDEASolver {

	private Map<String, List<Double>> inputs;
	private Map<String, List<Double>> outputs;
	private int size;

	/**
	 * Constructs a new DEA solver with an empty dataset.
	 */
	public PlaintextDEASolver() {
		inputs = new HashMap<String, List<Double>>();
		outputs = new HashMap<String, List<Double>>();
		size = -1;
	}

	/**
	 * Does the DEA analysis for a given target against the current dataset.
	 * 
	 * @param targetInputs
	 *            a map between input labels and values.
	 * @param targetOutputs
	 *            a map between output labels and values.
	 * @return the DEA score of the given target.
	 */
	public double solve(Map<String, Double> targetInputs,
			Map<String, Double> targetOutputs) {
		int sizePlusTarget = size + 2;
		/*
		 * We number the variables as follows:
		 * 
		 * Let the order for variables be x_0, x_1, ..., x_(n + 1)
		 * 
		 * Were n is the number of farmers in the dataset.
		 * 
		 * Then:
		 * 
		 * x_0 = theta
		 * 
		 * x_1 = "lambda of target"
		 * 
		 * x_(i + 1) = "lambda for i'th farmer in dataset"
		 * 
		 * In this way we may include the target twice, but this is not a
		 * problem, since the result will be invariant.
		 */
		// Make objective function
		double[] objectiveCoeffs = new double[sizePlusTarget];
		objectiveCoeffs[0] = 1;
		for (int i = 1; i < sizePlusTarget; i++) {
			objectiveCoeffs[i] = 0;
		}
		LinearObjectiveFunction objectiveFuntion = new LinearObjectiveFunction(
				objectiveCoeffs, 0);

		// Make input constraints
		LinkedList<LinearConstraint> constraints = new LinkedList<LinearConstraint>();
		for (String inputLabel : targetInputs.keySet()) {
			double[] lcoeffs = new double[sizePlusTarget];
			lcoeffs[0] = 0;
			lcoeffs[1] = targetInputs.get(inputLabel);
			int i = 2;
			for (double d : inputs.get(inputLabel)) {
				lcoeffs[i] = d;
				i++;
			}
			LinearConstraint cons = new LinearConstraint(lcoeffs,
					Relationship.LEQ, targetInputs.get(inputLabel));
			constraints.add(cons);
		}

		// Make output constraints
		for (String outputLabel : targetOutputs.keySet()) {
			double[] lcoeffs = new double[sizePlusTarget];

			lcoeffs[0] = -targetOutputs.get(outputLabel);
			lcoeffs[1] = targetOutputs.get(outputLabel);

			int i = 2;
			for (double d : outputs.get(outputLabel)) {
				lcoeffs[i] = d;
				i++;
			}
			LinearConstraint cons = new LinearConstraint(lcoeffs,
					Relationship.GEQ, 0);
			constraints.add(cons);
		}

		// Make one-constraint
		double[] oneCoeffs = new double[sizePlusTarget];
		oneCoeffs[0] = 0;
		for (int i = 1; i < sizePlusTarget; i++) {
			oneCoeffs[i] = 1;
		}
		LinearConstraint oneConstraint = new LinearConstraint(oneCoeffs,
				Relationship.EQ, 1);
		constraints.add(oneConstraint);

		// Solve
		SimplexSolver solver = new SimplexSolver();
		LinearConstraintSet set = new LinearConstraintSet(constraints);
		PointValuePair pvp = solver.optimize(objectiveFuntion, set,
				GoalType.MAXIMIZE, new NonNegativeConstraint(true));
		return pvp.getValue();
	}

	/**
	 * Add an new input type with corresponding values to the current dataset. A
	 * value must be given for each entry in the current dataset. If the current
	 * dataset is empty this will define the size of the dataset.
	 * 
	 * @param label
	 *            label of the new datatype
	 * @param inputList
	 *            a list of values for the datatype, must be of the same size as
	 *            the list of any prior added datatypes.
	 */
	public void addInputType(String label, List<Double> inputList) {
		if (size < 0) {
			size = inputList.size();
		} else if (size != inputList.size()) {
			throw new IllegalArgumentException("Size of input list \"" + label
					+ "\": " + inputList.size() + ". Should be: " + size);
		}
		inputs.put(label, inputList);
	}

	/**
	 * Convenience method for adding the dataset. 
	 * Given two BigInteger matrices, will call
	 * the appropriate addInputType() and addOutputType()
	 * methods.
	 * @param rawBasisInputs Matrix of inputs
	 * @param rawBasisOutputs Matrix of outputs
	 */
	public void addBasis(BigInteger[][] rawBasisInputs, BigInteger[][] rawBasisOutputs){
		for(int i = 0; i<rawBasisInputs[0].length; i++){
			List<Double> inputList = new ArrayList<Double>(rawBasisInputs.length);
			for(int j= 0; j< rawBasisInputs.length; j++){
				inputList.add(rawBasisInputs[j][i].doubleValue());
			}
			addInputType("input_"+i, inputList);
		}
		for(int i = 0; i<rawBasisOutputs[0].length; i++){
			List<Double> outputList = new ArrayList<Double>(rawBasisOutputs.length);
			for(int j= 0; j< rawBasisOutputs.length; j++){
				outputList.add(rawBasisOutputs[j][i].doubleValue());
			}
			addOutputType("output_"+i, outputList);
		}
	}
	
	/**
	 * Convenience method for solving a DEA problem.
	 *  
	 * For each row in the two BigInteger matrices (inputs
	 * and outputs), it will attempt to solve the problem
	 * using the solve() method. 
	 * @param rawTargetInputs A matrix containing input values
	 * @param rawTargetOutputs A matrix containing output values
	 * @return An array of results.
	 */
	public double[] solve(BigInteger[][] rawTargetInputs, BigInteger[][] rawTargetOutputs){
		double[] results = new double[rawTargetInputs.length];
		for(int i = 0; i<rawTargetInputs.length; i++){
			Map<String, Double> targetRowInput = new HashMap<String, Double>();
			Map<String, Double> targetRowOutput = new HashMap<String, Double>();

			for(int j= 0; j< rawTargetInputs[i].length; j++){
				targetRowInput.put("input_"+j, rawTargetInputs[i][j].doubleValue());
			}
			for(int j= 0; j< rawTargetOutputs[i].length; j++){
				targetRowOutput.put("output_"+j, rawTargetOutputs[i][j].doubleValue());
			}
			results[i] = solve(targetRowInput, targetRowOutput);
		}
		return results;
	}
	
	/**
	 * Add an new output type with corresponding values to the current dataset.
	 * A value must be given for each entry in the current dataset. If the
	 * current dataset is empty this will define the size of the dataset.
	 * 
	 * @param label
	 *            label of the new datatype
	 * @param inputList
	 *            a list of values for the datatype, must be of the same size as
	 *            the list of any prior added datatypes.
	 */
	public void addOutputType(String label, List<Double> outputList) {
		outputs.put(label, outputList);
		if (size < 0) {
			size = outputList.size();
		} else if (size != outputList.size()) {
			throw new IllegalArgumentException("Size of output list \"" + label
					+ "\": " + outputList.size() + ". Should be: " + size);
		}
		inputs.put(label, outputList);
	}
	
	public void addDataSet(Map<String, List<Double>> inputSet, Map<String, List<Double>> outputSet) {
		for(Entry<String, List<Double>> e: inputSet.entrySet()){
			addInputType(e.getKey(), e.getValue());
		}
		for(Entry<String, List<Double>> e: outputSet.entrySet()){
			addOutputType(e.getKey(), e.getValue());
		}
	}

}
