package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.lib.statistics.DeaSolver.AnalysisType;
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
    inputs = new HashMap<>();
    outputs = new HashMap<>();
    size = -1;
  }

  /**
   * Does the DEA analysis for a given target against the current dataset.
   * 
   * @param targetInputs a map between input labels and values.
   * @param targetOutputs a map between output labels and values.
   * @return the DEA score of the given target.
   */
  public double solve(Map<String, Double> targetInputs, Map<String, Double> targetOutputs,
      AnalysisType type) {
    int sizePlusTheta = size + 2;
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
     * 
     * x_(i + 1) = "lambda for i'th farmer in dataset"
     * 
     * In this way we may include the target twice, but this is not a problem, since the result will
     * be invariant.
     */
    // Make objective function
    double[] objectiveCoeffs = new double[sizePlusTheta];
    objectiveCoeffs[0] = 1;
    for (int i = 1; i < sizePlusTheta; i++) {
      objectiveCoeffs[i] = 0;
    }
    LinearObjectiveFunction objectiveFuntion = new LinearObjectiveFunction(objectiveCoeffs, 0);

    LinkedList<LinearConstraint> constraints = null;
    if (type == DeaSolver.AnalysisType.OUTPUT_EFFICIENCY) {
      constraints = buildOutputEfficienceConstraints(targetInputs, targetOutputs, sizePlusTheta);
    } else {
      constraints = buildInputEfficienceConstraints(targetInputs, targetOutputs, sizePlusTheta - 1);
    }

    // Solve
    SimplexSolver solver = new SimplexSolver();
    LinearConstraintSet set = new LinearConstraintSet(constraints);
    GoalType goal =
        (type == DeaSolver.AnalysisType.OUTPUT_EFFICIENCY) ? GoalType.MAXIMIZE : GoalType.MINIMIZE;
    PointValuePair pvp =
        solver.optimize(objectiveFuntion, set, goal, new NonNegativeConstraint(true));
    return pvp.getValue();
  }

  private LinkedList<LinearConstraint> buildOutputEfficienceConstraints(
      Map<String, Double> targetInputs, Map<String, Double> targetOutputs, int sizePlusTarget) {
    // Make input constraints
    LinkedList<LinearConstraint> constraints = new LinkedList<>();
    for (String inputLabel : targetInputs.keySet()) {
      double[] lcoeffs = new double[sizePlusTarget];
      lcoeffs[0] = 0;
      lcoeffs[1] = targetInputs.get(inputLabel);
      int i = 2;
      for (double d : inputs.get(inputLabel)) {
        lcoeffs[i] = d;
        i++;
      }
      LinearConstraint cons =
          new LinearConstraint(lcoeffs, Relationship.LEQ, targetInputs.get(inputLabel));
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
      LinearConstraint cons = new LinearConstraint(lcoeffs, Relationship.GEQ, 0);
      constraints.add(cons);
    }

    // Make one-constraint
    double[] oneCoeffs = new double[sizePlusTarget];
    oneCoeffs[0] = 0;
    for (int i = 1; i < sizePlusTarget; i++) {
      oneCoeffs[i] = 1;
    }
    LinearConstraint oneConstraint = new LinearConstraint(oneCoeffs, Relationship.EQ, 1);
    constraints.add(oneConstraint);
    return constraints;
  }
  
  private LinkedList<LinearConstraint> buildInputEfficienceConstraints(
      Map<String, Double> targetInputs, Map<String, Double> targetOutputs, int sizePlusTarget) {
    // Make input constraints
    LinkedList<LinearConstraint> constraints = new LinkedList<>();
    for (String inputLabel : targetInputs.keySet()) {
      double[] lcoeffs = new double[sizePlusTarget];
      lcoeffs[0] = -targetInputs.get(inputLabel);
      int i = 1;
      for (double d : inputs.get(inputLabel)) {
        lcoeffs[i] = d;
        i++;
      }
      LinearConstraint cons =
          new LinearConstraint(lcoeffs, Relationship.LEQ, 0);
      constraints.add(cons);
    }

    // Make output constraints
    for (String outputLabel : targetOutputs.keySet()) {
      double[] lcoeffs = new double[sizePlusTarget];
      lcoeffs[0] = 0;
      int i = 1;
      for (double d : outputs.get(outputLabel)) {
        lcoeffs[i] = d;       
        i++;
      }
      LinearConstraint cons = new LinearConstraint(lcoeffs, Relationship.GEQ, targetOutputs.get(outputLabel));
      constraints.add(cons);
    }

    // Make one-constraint
    double[] oneCoeffs = new double[sizePlusTarget];
    oneCoeffs[0] = 0;
    for (int i = 1; i < sizePlusTarget; i++) {
      oneCoeffs[i] = 1;
    }
    LinearConstraint oneConstraint = new LinearConstraint(oneCoeffs, Relationship.EQ, 1);
    constraints.add(oneConstraint);
    return constraints;
  }

  /**
   * Add an new input type with corresponding values to the current dataset. A value must be given
   * for each entry in the current dataset. If the current dataset is empty this will define the
   * size of the dataset.
   * 
   * @param label label of the new datatype
   * @param inputList a list of values for the datatype, must be of the same size as the list of any
   *        prior added datatypes.
   */
  public void addInputType(String label, List<Double> inputList) {
    if (size < 0) {
      size = inputList.size();
    } else if (size != inputList.size()) {
      throw new IllegalArgumentException(
          "Size of input list \"" + label + "\": " + inputList.size() + ". Should be: " + size);
    }
    inputs.put(label, inputList);
  }

  /**
   * Convenience method for adding the dataset. Given two BigInteger matrices, will call the
   * appropriate addInputType() and addOutputType() methods.
   * 
   * @param rawBasisInputs Matrix of inputs
   * @param rawBasisOutputs Matrix of outputs
   */
  public void addBasis(BigInteger[][] rawBasisInputs, BigInteger[][] rawBasisOutputs) {
    for (int i = 0; i < rawBasisInputs[0].length; i++) {
      List<Double> inputList = new ArrayList<>(rawBasisInputs.length);
      for (BigInteger[] rawBasisInput : rawBasisInputs) {
        inputList.add(rawBasisInput[i].doubleValue());
      }
      addInputType("input_" + i, inputList);
    }
    for (int i = 0; i < rawBasisOutputs[0].length; i++) {
      List<Double> outputList = new ArrayList<>(rawBasisOutputs.length);
      for (BigInteger[] rawBasisOutput : rawBasisOutputs) {
        outputList.add(rawBasisOutput[i].doubleValue());
      }
      addOutputType("output_" + i, outputList);
    }
  }

  /**
   * Convenience method for solving a DEA problem.
   * 
   * For each row in the two BigInteger matrices (inputs and outputs), it will attempt to solve the
   * problem using the solve() method.
   * 
   * @param rawTargetInputs A matrix containing input values
   * @param rawTargetOutputs A matrix containing output values
   * @return An array of results.
   */
  public double[] solve(BigInteger[][] rawTargetInputs, BigInteger[][] rawTargetOutputs,
      AnalysisType type) {
    double[] results = new double[rawTargetInputs.length];
    for (int i = 0; i < rawTargetInputs.length; i++) {
      Map<String, Double> targetRowInput = new HashMap<>();
      Map<String, Double> targetRowOutput = new HashMap<>();

      for (int j = 0; j < rawTargetInputs[i].length; j++) {
        targetRowInput.put("input_" + j, rawTargetInputs[i][j].doubleValue());
      }
      for (int j = 0; j < rawTargetOutputs[i].length; j++) {
        targetRowOutput.put("output_" + j, rawTargetOutputs[i][j].doubleValue());
      }
      results[i] = solve(targetRowInput, targetRowOutput, type);
    }
    return results;
  }

  /**
   * Add an new output type with corresponding values to the current dataset. A value must be given
   * for each entry in the current dataset. If the current dataset is empty this will define the
   * size of the dataset.
   * 
   * @param label label of the new datatype
   * @param outputList a list of values for the datatype, must be of the same size as the list of any
   *        prior added datatypes.
   */
  public void addOutputType(String label, List<Double> outputList) {
    outputs.put(label, outputList);
    if (size < 0) {
      size = outputList.size();
    } else if (size != outputList.size()) {
      throw new IllegalArgumentException(
          "Size of output list \"" + label + "\": " + outputList.size() + ". Should be: " + size);
    }
    inputs.put(label, outputList);
  }

  public void addDataSet(Map<String, List<Double>> inputSet, Map<String, List<Double>> outputSet) {
    for (Entry<String, List<Double>> e : inputSet.entrySet()) {
      addInputType(e.getKey(), e.getValue());
    }
    for (Entry<String, List<Double>> e : outputSet.entrySet()) {
      addOutputType(e.getKey(), e.getValue());
    }
  }

}
