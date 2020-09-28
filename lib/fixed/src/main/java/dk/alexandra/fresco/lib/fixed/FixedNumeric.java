package dk.alexandra.fresco.lib.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;

/**
 * Basic interface for numeric applications on fixed numbers.
 */
public interface FixedNumeric extends ComputationDirectory {

  /**
   * Create a new FixedNumeric using the given builder.
   *
   * @param builder The root builder to use.
   * @return A new FixedNumeric computation directory.
   */
  static FixedNumeric using(ProtocolBuilderNumeric builder) {
    return new DefaultFixedNumeric(builder);
  }

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
   * Adds a secret value with a public value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a+b
   */
  default DRes<SFixed> add(double a, DRes<SFixed> b) {
    return add(BigDecimal.valueOf(a), b);
  }
  
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
   * Subtracts a public value and a secret value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a-b
   */
  default DRes<SFixed> sub(double a, DRes<SFixed> b) {
    return sub(BigDecimal.valueOf(a), b);
  }
  
  /**
   * Subtracts a secret value and a public value and returns the result.
   *
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a-b
   */
  DRes<SFixed> sub(DRes<SFixed> a, BigDecimal b);

  /**
   * Subtracts a secret value and a public value and returns the result.
   *
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a-b
   */
  default DRes<SFixed> sub(DRes<SFixed> a, double b) {
    return sub(a, BigDecimal.valueOf(b));
  }

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
   * Multiplies a public value onto a secret value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a*b
   */
  default DRes<SFixed> mult(double a, DRes<SFixed> b) {
    return mult(BigDecimal.valueOf(a), b);
  }

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
   * Divides a secret value with a public value and returns the result.
   *
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a/b
   */
  default DRes<SFixed> div(DRes<SFixed> a, double b) {
    return div(a, BigDecimal.valueOf(b));
  }
  
  /**
   * Creates a known secret value from a public value. This is primarily a helper function in order
   * to use public values within the FRESCO functions.
   *
   * @param value The public value.
   * @return A secret value which represents the given public value.
   */
  DRes<SFixed> known(BigDecimal value);

  /**
   * Creates a known secret value from a public value. This is primarily a helper function in order
   * to use public values within the FRESCO functions.
   *
   * @param value The public value.
   * @return A secret value which represents the given public value.
   */
  default DRes<SFixed> known(double value) {
    return known(BigDecimal.valueOf(value));
  }

  /**
   * Create a secret fixed value from a secret integer value representing the same value.
   * 
   * @param value A secret integer.
   * @return A secret fixed with the same value as the input
   */
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
   * Closes a public value. If the MPC party calling this method is not providing input, just use
   * null as the input value.
   *
   * @param value The value to input or null if no input should be given.
   * @param inputParty The ID of the MPC party.
   * @return The closed input value.
   */
  default DRes<SFixed> input(double value, int inputParty) {
    return input(BigDecimal.valueOf(value), inputParty);
  }
  
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
   * Compare two secret values. Returns a secret int that is 1 if x \leq y and 0 otherwise.
   *
   * @param x Secret value 1
   * @param y Secret value 2
   * @return A secret int that is 1 if x \leq y and 0 otherwise.
   */
  DRes<SInt> leq(DRes<SFixed> x, DRes<SFixed> y);

}
