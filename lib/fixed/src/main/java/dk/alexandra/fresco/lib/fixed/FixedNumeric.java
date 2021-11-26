package dk.alexandra.fresco.lib.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.fixed.truncations.BinaryTruncation;
import dk.alexandra.fresco.lib.fixed.truncations.Truncation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

/**
 * Basic interface for numeric applications on fixed numbers.
 */
public abstract class FixedNumeric implements ComputationDirectory {

  private static Function<ProtocolBuilderNumeric, FixedNumeric> provider = builder ->
      new DefaultFixedNumeric(builder, new BinaryTruncation(builder.getBasicNumericContext().getDefaultFixedPointPrecision()));

  /** Redefine default FixedNumeric implementation to use */
  public static void load(Function<ProtocolBuilderNumeric, FixedNumeric> provider) {
    FixedNumeric.provider = provider;
  }

  /**
   * Create a new FixedNumeric using the given builder.
   *
   * @param builder The root builder to use.
   * @return A new FixedNumeric computation directory.
   */
  public static FixedNumeric using(ProtocolBuilderNumeric builder) {
    return provider.apply(builder);
  }

  /**
   * Adds two secret values and returns the result.
   *
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a+b
   */
  public abstract DRes<SFixed> add(DRes<SFixed> a, DRes<SFixed> b);

  /**
   * Adds a secret value with a public value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a+b
   */
  public abstract DRes<SFixed> add(BigDecimal a, DRes<SFixed> b);
  
  /**
   * Adds a secret value with a public value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a+b
   */
  public DRes<SFixed> add(double a, DRes<SFixed> b) {
    return add(BigDecimal.valueOf(a), b);
  }
  
  /**
   * Subtracts two secret values and returns the result.
   *
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a-b
   */
  public abstract DRes<SFixed> sub(DRes<SFixed> a, DRes<SFixed> b);

  /**
   * Subtracts a public value and a secret value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a-b
   */
  public abstract DRes<SFixed> sub(BigDecimal a, DRes<SFixed> b);

  /**
   * Subtracts a public value and a secret value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a-b
   */
  public DRes<SFixed> sub(double a, DRes<SFixed> b) {
    return sub(BigDecimal.valueOf(a), b);
  }
  
  /**
   * Subtracts a secret value and a public value and returns the result.
   *
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a-b
   */
  public abstract DRes<SFixed> sub(DRes<SFixed> a, BigDecimal b);

  /**
   * Subtracts a secret value and a public value and returns the result.
   *
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a-b
   */
  public DRes<SFixed> sub(DRes<SFixed> a, double b) {
    return sub(a, BigDecimal.valueOf(b));
  }

  /**
   * Multiplies two secret values and returns the result.
   *
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a*b
   */
  public abstract DRes<SFixed> mult(DRes<SFixed> a, DRes<SFixed> b);

  /**
   * Multiplies a public value onto a secret value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a*b
   */
  public abstract DRes<SFixed> mult(BigDecimal a, DRes<SFixed> b);

  /**
   * Multiplies a public value onto a secret value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a*b
   */
  public DRes<SFixed> mult(double a, DRes<SFixed> b) {
    return mult(BigDecimal.valueOf(a), b);
  }

  /**
   * Divides two secret values and returns the result.
   *
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a/b
   */
  public abstract DRes<SFixed> div(DRes<SFixed> a, DRes<SFixed> b);

  /**
   * Divides a secret value with a public value and returns the result.
   *
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a/b
   */
  public  abstract DRes<SFixed> div(DRes<SFixed> a, BigDecimal b);
  
  /**
   * Divides a secret value with a public value and returns the result.
   *
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a/b
   */
  public  DRes<SFixed> div(DRes<SFixed> a, double b) {
    return div(a, BigDecimal.valueOf(b));
  }
  
  /**
   * Creates a known secret value from a public value. This is primarily a helper function in order
   * to use public values within the FRESCO functions.
   *
   * @param value The public value.
   * @return A secret value which represents the given public value.
   */
  public abstract DRes<SFixed> known(BigDecimal value);

  /**
   * Creates a known secret value from a public value. This is primarily a helper function in order
   * to use public values within the FRESCO functions.
   *
   * @param value The public value.
   * @return A secret value which represents the given public value.
   */
  public  DRes<SFixed> known(double value) {
    return known(BigDecimal.valueOf(value));
  }

  /**
   * Create a secret fixed value from a secret integer value representing the same value.
   * 
   * @param value A secret integer.
   * @return A secret fixed with the same value as the input
   */
  public abstract DRes<SFixed> fromSInt(DRes<SInt> value);

  /**
   * Closes a public value. If the MPC party calling this method is not providing input, just use
   * null as the input value.
   *
   * @param value The value to input or null if no input should be given.
   * @param inputParty The ID of the MPC party.
   * @return The closed input value.
   */
  public abstract DRes<SFixed> input(BigDecimal value, int inputParty);

  /**
   * Closes a public value. If the MPC party calling this method is not providing input, just use
   * null as the input value.
   *
   * @param value The value to input or null if no input should be given.
   * @param inputParty The ID of the MPC party.
   * @return The closed input value.
   */
  public DRes<SFixed> input(double value, int inputParty) {
    return input(BigDecimal.valueOf(value), inputParty);
  }
  
  /**
   * Opens a value to all MPC parties.
   *
   * @param secretShare The value to open.
   * @return The opened value represented by the closed value.
   */
  public abstract DRes<BigDecimal> open(DRes<SFixed> secretShare);

  /**
   * Opens a value to a single given party.
   *
   * @param secretShare The value to open.
   * @param outputParty The party to receive the opened value.
   * @return The opened value if you are the outputParty, or null otherwise.
   */
  public abstract DRes<BigDecimal> open(DRes<SFixed> secretShare, int outputParty);

  /**
   * Compare two secret values. Returns a secret int that is 1 if x \leq y and 0 otherwise.
   *
   * @param x Secret value 1
   * @param y Secret value 2
   * @return A secret int that is 1 if x \leq y and 0 otherwise.
   */
  public abstract DRes<SInt> leq(DRes<SFixed> x, DRes<SFixed> y);

}
