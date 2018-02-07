package dk.alexandra.fresco.fixedpoint;

import java.math.BigDecimal;
import java.util.List;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.lib.collections.Matrix;

/**
 * Basic interface for fixed point numeric applications.
 */
public interface LinearAlgebra extends ComputationDirectory {

  /**
   * Adds two secret values and returns the result.
   *
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a+b
   */
  DRes<Matrix<DRes<SFixed>>> add(DRes<Matrix<DRes<SFixed>>> a,
      DRes<Matrix<DRes<SFixed>>> b);

  /**
   * Adds a secret value with a public value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a+b
   */
  DRes<Matrix<DRes<SFixed>>> add(Matrix<BigDecimal> a, DRes<Matrix<DRes<SFixed>>> b);

  /**
   * Multiplies two secret values and returns the result.
   *
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a*b
   */
  DRes<Matrix<DRes<SFixed>>> mult(DRes<Matrix<DRes<SFixed>>> a, DRes<Matrix<DRes<SFixed>>> b);

  /**
   * Multiplies a public value onto a secret value and returns the result.
   *
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a*b
   */
  DRes<Matrix<DRes<SFixed>>> mult(DRes<Matrix<DRes<SFixed>>> a, Matrix<BigDecimal> b);


  /**
   * Multiply a public value with a secret value.
   * 
   * @param a
   * @param b
   * @return
   */
  DRes<Matrix<DRes<SFixed>>> mult(Matrix<BigDecimal> a,
      DRes<Matrix<DRes<SFixed>>> b);

  /**
   * Multiply a secret matrix by a public constant.
   *
   * @param s Public value
   * @param a Secret value
   * @return A deferred result computing sa
   */
  DRes<Matrix<DRes<SFixed>>> scale(BigDecimal s, DRes<Matrix<DRes<SFixed>>> a);

  /**
   * Multiply a secret matrix by a secret constant.
   *
   * @param s secret value
   * @param a Secret value
   * @return A deferred result computing sa
   */
  DRes<Matrix<DRes<SFixed>>> scale(DRes<SFixed> s, DRes<Matrix<DRes<SFixed>>> a);

  /**
   * Multiply a public matrix by a secret constant.
   *
   * @param s Secret value
   * @param a Public value
   * @return A deferred result computing sa
   */
  DRes<Matrix<DRes<SFixed>>> scale(DRes<SFixed> s, Matrix<BigDecimal> a);

  /**
   * Closes a public value. If the MPC party calling this method is not providing input, just use
   * null as the input value.
   *
   * @param value The value to input or null if no input should be given.
   * @param inputParty The ID of the MPC party.
   * @return The closed input value.
   */
  DRes<Matrix<DRes<SFixed>>> input(Matrix<BigDecimal> value, int inputParty);

  /**
   * Opens a value to all MPC parties.
   *
   * @param secretShare The value to open.
   * @return The opened value represented by the closed value.
   */
  DRes<Matrix<DRes<BigDecimal>>> open(DRes<Matrix<DRes<SFixed>>> secretShare);

  DRes<SFixed> innerProduct(DRes<List<DRes<SFixed>>> a, DRes<List<DRes<SFixed>>> b);

  DRes<SFixed> innerProduct(List<BigDecimal> a, DRes<List<DRes<SFixed>>> b);
  
}
