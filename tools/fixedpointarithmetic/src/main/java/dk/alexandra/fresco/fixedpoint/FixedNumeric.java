package dk.alexandra.fresco.fixedpoint;

import java.math.BigDecimal;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Basic interface for fixed point numeric applications.
 */
public interface FixedNumeric extends ComputationDirectory {

  /**
   * Adds two secret values and returns the result.
   *
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a+b
   */
  DRes<SFixed> add(DRes<SFixed> a, DRes<SFixed> b);

  /**
   * Adds a secret value with a public value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a+b
   */
  DRes<SFixed> add(BigDecimal a, DRes<SFixed> b);

  /**
   * Subtracts two secret values and returns the result.
   *
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a-b
   */
  DRes<SFixed> sub(DRes<SFixed> a, DRes<SFixed> b);

  /**
   * Subtracts a public value and a secret value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a-b
   */
  DRes<SFixed> sub(BigDecimal a, DRes<SFixed> b);

  /**
   * Subtracts a secret value and a public value and returns the result.
   *
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a-b
   */
  DRes<SFixed> sub(DRes<SFixed> a, BigDecimal b);

  /**
   * Multiplies two secret values and returns the result.
   *
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a*b
   */
  DRes<SFixed> mult(DRes<SFixed> a, DRes<SFixed> b);

  /**
   * Multiplies a public value onto a secret value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a*b
   */
  DRes<SFixed> mult(BigDecimal a, DRes<SFixed> b);

  /**
   * Divides two secret values and returns the result.
   *
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a/b
   */
  DRes<SFixed> div(DRes<SFixed> a, DRes<SFixed> b);

  /**
   * Divides a secret value with a public value and returns the result.
   *
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a/b
   */
  DRes<SFixed> div(DRes<SFixed> a, BigDecimal b);

  /**
   * Creates a known secret value from a public value. This is primarily a helper function in order
   * to use public values within the FRESCO functions.
   *
   * @param value The public value.
   * @return A secret value which represents the given public value.
   */
  DRes<SFixed> known(BigDecimal value);
  
  DRes<SFixed> fromSInt(DRes<SInt> value);

  /**
   * Closes a public value. If the MPC party calling this method is not providing input, just use
   * null as the input value.
   *
   * @param value The value to input or null if no input should be given.
   * @param inputParty The ID of the MPC party.
   * @return The closed input value.
   */
  DRes<SFixed> input(BigDecimal value, int inputParty);

  /**
   * Opens a value to all MPC parties.
   *
   * @param secretShare The value to open.
   * @return The opened value represented by the closed value.
   */
  DRes<BigDecimal> open(DRes<SFixed> secretShare);

  /**
   * Opens a value to a single given party.
   *
   * @param secretShare The value to open.
   * @param outputParty The party to receive the opened value.
   * @return The opened value if you are the outputParty, or null otherwise.
   */
  DRes<BigDecimal> open(DRes<SFixed> secretShare, int outputParty);
  
  /**
   * Create a random value between 0 and 1.
   * 
   * @return The random value 
   */
  DRes<SFixed> random();
}
