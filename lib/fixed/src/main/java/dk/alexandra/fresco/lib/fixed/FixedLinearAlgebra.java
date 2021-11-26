package dk.alexandra.fresco.lib.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.lib.common.collections.Matrix;
import dk.alexandra.fresco.lib.fixed.truncations.BinaryTruncation;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.function.Function;

public abstract class FixedLinearAlgebra implements ComputationDirectory {

  private static Function<ProtocolBuilderNumeric, FixedLinearAlgebra> provider = DefaultFixedLinearAlgebra::new;

  /** Redefine default FixedLinearAlgebra implementation to use */
  public static void load(Function<ProtocolBuilderNumeric, FixedLinearAlgebra> provider) {
    FixedLinearAlgebra.provider = provider;
  }

  /**
   * Create a FixedLinearAlgebra using the given builder.
   *
   * @param builder The root builder to use.
   * @return A new FixedLinearAlgebra computation directory.
   */
  public static FixedLinearAlgebra using(ProtocolBuilderNumeric builder) {
    return new DefaultFixedLinearAlgebra(builder);
  }

  /**
   * Adds two secret values and returns the result.
   *
   * @param a First secret value
   * @param b Second secret value
   * @return A deferred result computing a+b
   */
  public abstract DRes<Matrix<DRes<SFixed>>> add(DRes<Matrix<DRes<SFixed>>> a, DRes<Matrix<DRes<SFixed>>> b);

  /**
   * Adds a secret value to a public value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a+b
   */
  public abstract DRes<Matrix<DRes<SFixed>>> add(Matrix<BigDecimal> a, DRes<Matrix<DRes<SFixed>>> b);

  /**
   * Subtracts two secret values and returns the result.
   *
   * @param a First secret value
   * @param b Second secret value
   * @return A deferred result computing a-b
   */
  public abstract DRes<Matrix<DRes<SFixed>>> sub(DRes<Matrix<DRes<SFixed>>> a, DRes<Matrix<DRes<SFixed>>> b);

  /**
   * Subtracts a secret value to a public value and returns the result.
   *
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a-b
   */
  public abstract DRes<Matrix<DRes<SFixed>>> sub(Matrix<BigDecimal> a, DRes<Matrix<DRes<SFixed>>> b);

  /**
   * Subtracts a secret value to a public value and returns the result.
   *
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a-b
   */
  public abstract DRes<Matrix<DRes<SFixed>>> sub(DRes<Matrix<DRes<SFixed>>> a, Matrix<BigDecimal> b);
  
  /**
   * Multiplies two secret values and returns the result.
   *
   * @param a First secret value
   * @param b Second secret value
   * @return A deferred result computing a*b
   */
  public abstract DRes<Matrix<DRes<SFixed>>> mult(DRes<Matrix<DRes<SFixed>>> a, DRes<Matrix<DRes<SFixed>>> b);

  /**
   * Multiply a matrix to a vector.
   * 
   * @param a First secret value
   * @param b Second secret value
   * @return A deferred result computing a*v
   */
  public abstract DRes<ArrayList<DRes<SFixed>>> vectorMult(DRes<Matrix<DRes<SFixed>>> a,
      DRes<ArrayList<DRes<SFixed>>> b);

  /**
   * Multiply a matrix to a vector.
   * 
   * @param a Secret matrix
   * @param v Public vector
   * @return A deferred result computing a*v
   */
  public abstract DRes<ArrayList<DRes<SFixed>>> vectorMult(DRes<Matrix<DRes<SFixed>>> a, ArrayList<BigDecimal> v);

  /**
   * Multiply a matrix to a vector.
   * 
   * @param a Public matrix
   * @param v Secret vector
   * @return A deferred result computing a*v
   */
  public abstract DRes<ArrayList<DRes<SFixed>>> vectorMult(Matrix<BigDecimal> a, DRes<ArrayList<DRes<SFixed>>> v);

  /**
   * Multiplies a public value onto a secret value and returns the result.
   *
   * @param a Secret value
   * @param b Public value
   * @return A deferred result computing a*b
   */
  public abstract DRes<Matrix<DRes<SFixed>>> mult(DRes<Matrix<DRes<SFixed>>> a, Matrix<BigDecimal> b);

  /**
   * Multiply a public value with a secret value.
   * 
   * @param a Public value
   * @param b Secret value
   * @return A deferred result computing a*b
   */
  public abstract DRes<Matrix<DRes<SFixed>>> mult(Matrix<BigDecimal> a, DRes<Matrix<DRes<SFixed>>> b);

  /**
   * Multiply a secret matrix by a public constant.
   *
   * @param s Public value
   * @param a Secret value
   * @return A deferred result computing sa
   */
  public abstract DRes<Matrix<DRes<SFixed>>> scale(BigDecimal s, DRes<Matrix<DRes<SFixed>>> a);

  /**
   * Multiply a secret matrix by a public constant.
   *
   * @param s Public value
   * @param a Secret value
   * @return A deferred result computing sa
   */
  public DRes<Matrix<DRes<SFixed>>> scale(double s, DRes<Matrix<DRes<SFixed>>> a) {
    return scale(BigDecimal.valueOf(s), a);
  }

  /**
   * Multiply a secret matrix by a secret constant.
   *
   * @param s secret value
   * @param a Secret value
   * @return A deferred result computing sa
   */
  public abstract DRes<Matrix<DRes<SFixed>>> scale(DRes<SFixed> s, DRes<Matrix<DRes<SFixed>>> a);

  /**
   * Multiply a public matrix by a secret constant.
   *
   * @param s Secret value
   * @param a Public value
   * @return A deferred result computing sa
   */
  public abstract DRes<Matrix<DRes<SFixed>>> scale(DRes<SFixed> s, Matrix<BigDecimal> a);

  /**
   * Transpose a secret matrix.
   *
   * @param a Secret value
   * @return A deferred result computing a^t
   */
  public abstract DRes<Matrix<DRes<SFixed>>> transpose(DRes<Matrix<DRes<SFixed>>> a);
  
  /**
   * Closes a public matrix value. If the MPC party calling this method is not providing input, just
   * use null as the input value.
   *
   * @param value The value to input or null if no input should be given.
   * @param inputParty The ID of the MPC party.
   * @return The closed input value.
   */
  public abstract DRes<Matrix<DRes<SFixed>>> input(Matrix<BigDecimal> value, int inputParty);

  /**
   * Closes a public vector value. If the MPC party calling this method is not providing input, just
   * use null as the input value.
   *
   * @param value The value to input or null if no input should be given.
   * @param inputParty The ID of the MPC party.
   * @return The closed input value.
   */
  public abstract DRes<ArrayList<DRes<SFixed>>> input(ArrayList<BigDecimal> value, int inputParty);

  /**
   * Opens a matrix to all MPC parties.
   *
   * @param secretShare The value to open.
   * @return The opened value represented by the closed value.
   */
  public abstract DRes<Matrix<DRes<BigDecimal>>> openMatrix(DRes<Matrix<DRes<SFixed>>> secretShare);

  /**
   * Opens a vector to all MPC parties.
   *
   * @param secretShare The value to open.
   * @return The opened value represented by the closed value.
   */
  public abstract DRes<ArrayList<DRes<BigDecimal>>> openArrayList(DRes<ArrayList<DRes<SFixed>>> secretShare);


}
