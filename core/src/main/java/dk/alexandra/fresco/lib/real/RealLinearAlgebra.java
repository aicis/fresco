package dk.alexandra.fresco.lib.real;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.lib.collections.Matrix;
import java.math.BigDecimal;
import java.util.Vector;

public interface RealLinearAlgebra extends ComputationDirectory {

  /**
   * Adds two secret values and returns the result.
   *
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a+b
   */
  DRes<Matrix<DRes<SReal>>> add(DRes<Matrix<DRes<SReal>>> a, DRes<Matrix<DRes<SReal>>> b);

  /**
   * Adds a secret value to a public value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a+b
   */
  DRes<Matrix<DRes<SReal>>> add(Matrix<BigDecimal> a, DRes<Matrix<DRes<SReal>>> b);

  /**
   * Multiplies two secret values and returns the result.
   *
   * @param a Secret value 1
   * @param b Secret value 2
   * @return A deferred result computing a*b
   */
  DRes<Matrix<DRes<SReal>>> mult(DRes<Matrix<DRes<SReal>>> a, DRes<Matrix<DRes<SReal>>> b);

  /**
   * Multiply a matrix to a vector.
   * 
   * @param a Secret value 1
   * @param v Secret value 2
   * @return A deferred result computing a*v
   */
  DRes<Vector<DRes<SReal>>> vectorMult(DRes<Matrix<DRes<SReal>>> a, DRes<Vector<DRes<SReal>>> v);

  /**
   * Multiply a matrix to a vector.
   * 
   * @param a Secret matrix
   * @param v Public vector
   * @return A deferred result computing a*v
   */
  DRes<Vector<DRes<SReal>>> vectorMult(DRes<Matrix<DRes<SReal>>> a, Vector<BigDecimal> v);

  /**
   * Multiply a matrix to a vector.
   * 
   * @param a Public matrix
   * @param v Secret vector
   * @return A deferred result computing a*v
   */
  DRes<Vector<DRes<SReal>>> vectorMult(Matrix<BigDecimal> a, DRes<Vector<DRes<SReal>>> v);

  /**
   * Multiplies a public value onto a secret value and returns the result.
   *
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a*b
   */
  DRes<Matrix<DRes<SReal>>> mult(DRes<Matrix<DRes<SReal>>> a, Matrix<BigDecimal> b);

  /**
   * Multiply a public value with a secret value.
   * 
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a*b
   */
  DRes<Matrix<DRes<SReal>>> mult(Matrix<BigDecimal> a, DRes<Matrix<DRes<SReal>>> b);

  /**
   * Multiply a secret matrix by a public constant.
   *
   * @param s Public value
   * @param a Secret value
   * @return A deferred result computing sa
   */
  DRes<Matrix<DRes<SReal>>> scale(BigDecimal s, DRes<Matrix<DRes<SReal>>> a);

  /**
   * Multiply a secret matrix by a secret constant.
   *
   * @param s secret value
   * @param a Secret value
   * @return A deferred result computing sa
   */
  DRes<Matrix<DRes<SReal>>> scale(DRes<SReal> s, DRes<Matrix<DRes<SReal>>> a);

  /**
   * Multiply a public matrix by a secret constant.
   *
   * @param s Secret value
   * @param a Public value
   * @return A deferred result computing sa
   */
  DRes<Matrix<DRes<SReal>>> scale(DRes<SReal> s, Matrix<BigDecimal> a);

  /**
   * Closes a public matrix value. If the MPC party calling this method is not providing input, just
   * use null as the input value.
   *
   * @param value The value to input or null if no input should be given.
   * @param inputParty The ID of the MPC party.
   * @return The closed input value.
   */
  DRes<Matrix<DRes<SReal>>> input(Matrix<BigDecimal> value, int inputParty);

  /**
   * Closes a public vector value. If the MPC party calling this method is not providing input, just
   * use null as the input value.
   *
   * @param value The value to input or null if no input should be given.
   * @param inputParty The ID of the MPC party.
   * @return The closed input value.
   */
  DRes<Vector<DRes<SReal>>> input(Vector<BigDecimal> value, int inputParty);

  /**
   * Opens a matrix to all MPC parties.
   *
   * @param secretShare The value to open.
   * @return The opened value represented by the closed value.
   */
  DRes<Matrix<DRes<BigDecimal>>> openMatrix(DRes<Matrix<DRes<SReal>>> secretShare);

  /**
   * Opens a vector to all MPC parties.
   *
   * @param secretShare The value to open.
   * @return The opened value represented by the closed value.
   */
  DRes<Vector<DRes<BigDecimal>>> openVector(DRes<Vector<DRes<SReal>>> secretShare);


}
