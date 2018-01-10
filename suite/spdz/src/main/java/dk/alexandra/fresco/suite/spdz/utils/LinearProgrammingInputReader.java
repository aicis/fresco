package dk.alexandra.fresco.suite.spdz.utils;

import java.math.BigInteger;


public interface LinearProgrammingInputReader extends InputReader {

  /**
   * Gets the id of the player who is to receive output. 
   * An output of 0 is taken to indicate that all 
   * players should receive output.
   * @return The id of the output player
   */
  public int getOutputId();

  /**
   * Gets the coefficients of all constraints.
   * @return the constraints values
   */
  public BigInteger[][] getConstraintValues();

  /**
   * Gets the input pattern of all constraints.
   * @return the constraints pattern
   */
  public int[][] getConstraintPattern();

  /**
   * Gets the coefficients of the cost function.
   * @return the cost function values
   */
  public BigInteger[] getCostValues();

  /**
   * Gets the input pattern of the cost function.
   * @return the cost function input pattern
   */
  public int[] getCostPattern();

  /**
   * Gets the B-vector values, i.e., the right hand side of the constraints.
   * @return the B-vector values
   */
  public BigInteger[] getBValues();

  /**
   * Gets the B-vector input pattern.
   * @return the B-vector input pattern
   */
  public int[] getBPattern();

  /**
   * The F-vector values, i.e., the negated coefficients of the cost function.
   * @return the F-vector values
   */
  public BigInteger[] getFValues();

  /**
   * The F-vector input pattern, (this is the same as the cost functions input pattern).
   * @return the F-vector input pattern
   */
  public int[] getFPattern();

  /**
   * The C matrix values, i.e., the left hand side of the constraints.
   * @return the C matrix values
   */
  public BigInteger[][] getCValues();

  /**
   * The C matrix input pattern, i.e., the left hand side of the constraints.
   * @return the C matrix input pattern
   */
  public int[][] getCPattern();

}
