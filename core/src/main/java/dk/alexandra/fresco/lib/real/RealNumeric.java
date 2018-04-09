package dk.alexandra.fresco.lib.real;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;

/**
 * Basic interface for numeric applications on real numbers.
 */
public interface RealNumeric extends ComputationDirectory {

  /**
   * Adds two secret values and returns the result.
   *
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a+b
   */
  DRes<SReal> add(DRes<SReal> a, DRes<SReal> b);

  /**
   * Adds a secret value with a public value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a+b
   */
  DRes<SReal> add(BigDecimal a, DRes<SReal> b);

  /**
   * Subtracts two secret values and returns the result.
   *
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a-b
   */
  DRes<SReal> sub(DRes<SReal> a, DRes<SReal> b);

  /**
   * Subtracts a public value and a secret value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a-b
   */
  DRes<SReal> sub(BigDecimal a, DRes<SReal> b);

  /**
   * Subtracts a secret value and a public value and returns the result.
   *
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a-b
   */
  DRes<SReal> sub(DRes<SReal> a, BigDecimal b);

  /**
   * Multiplies two secret values and returns the result.
   *
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a*b
   */
  DRes<SReal> mult(DRes<SReal> a, DRes<SReal> b);

  /**
   * Multiplies a public value onto a secret value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a*b
   */
  DRes<SReal> mult(BigDecimal a, DRes<SReal> b);

  /**
   * Divides two secret values and returns the result.
   *
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a/b
   */
  DRes<SReal> div(DRes<SReal> a, DRes<SReal> b);

  /**
   * Divides a secret value with a public value and returns the result.
   *
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a/b
   */
  DRes<SReal> div(DRes<SReal> a, BigDecimal b);

  /**
   * Creates a known secret value from a public value. This is primarily a helper function in order
   * to use public values within the FRESCO functions.
   *
   * @param value The public value.
   * @return A secret value which represents the given public value.
   */
  DRes<SReal> known(BigDecimal value);

  /**
   * Create a secret real value from a secret integer value representing the same value.
   * 
   * @param value A secret integer.
   * @return A secret real with the same value as the input
   */
  DRes<SReal> fromSInt(DRes<SInt> value);

  /**
   * Closes a public value. If the MPC party calling this method is not providing input, just use
   * null as the input value.
   *
   * @param value The value to input or null if no input should be given.
   * @param inputParty The ID of the MPC party.
   * @return The closed input value.
   */
  DRes<SReal> input(BigDecimal value, int inputParty);

  /**
   * Opens a value to all MPC parties.
   *
   * @param secretShare The value to open.
   * @return The opened value represented by the closed value.
   */
  DRes<BigDecimal> open(DRes<SReal> secretShare);

  /**
   * Opens a value to a single given party.
   *
   * @param secretShare The value to open.
   * @param outputParty The party to receive the opened value.
   * @return The opened value if you are the outputParty, or null otherwise.
   */
  DRes<BigDecimal> open(DRes<SReal> secretShare, int outputParty);

  /**
   * Compare two secret values. Returns a secret int that is 1 if x \leq y and 0 otherwise.
   *
   * @param x Secret value 1
   * @param y Secret value 2
   * @return A secret int that is 1 if x \leq y and 0 otherwise.
   */
  DRes<SInt> leq(DRes<SReal> x, DRes<SReal> y);

}
