package dk.alexandra.fresco.lib.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

public abstract class AdvancedFixedNumeric implements ComputationDirectory {

  private static Function<ProtocolBuilderNumeric, AdvancedFixedNumeric> provider = DefaultAdvancedFixedNumeric::new;

  /**
   * Redefine default AdvancedFixedNumeric implementation to use
   */
  public static void load(Function<ProtocolBuilderNumeric, AdvancedFixedNumeric> provider) {
    AdvancedFixedNumeric.provider = provider;
  }

  /**
   * Create a new AdvancedFixedNumeric using the given builder.
   *
   * @param builder The root builder to use.
   * @return A new AdvancedFixedNumeric computation directory.
   */
  public static AdvancedFixedNumeric using(ProtocolBuilderNumeric builder) {
    return new DefaultAdvancedFixedNumeric(builder);
  }

  /**
   * Calculate the sum of all terms in a list.
   *
   * @param terms List of secret values
   * @return A deferred result computing the sum of the terms
   */
  public abstract DRes<SFixed> sum(List<DRes<SFixed>> terms);

  /**
   * Calculate the inner product of two secret vectors.
   *
   * @param a List of secret values
   * @param b List of secret values
   * @return A deferred result computing computing the inner product of the two lists
   */
  public abstract DRes<SFixed> innerProduct(List<DRes<SFixed>> a, List<DRes<SFixed>> b);

  /**
   * Calculate the inner product of a public and a secret vector.
   *
   * @param a List of public values
   * @param b List of secret values
   * @return A deferred result computing computing the inner product of the two lists
   */
  public abstract DRes<SFixed> innerProductWithPublicPart(List<BigDecimal> a, List<DRes<SFixed>> b);

  /**
   * Calcualte the exponential function of a secret input x. The result is mmore precise for small
   * inputs, so the input should be scaled if possible (note that <i>exp(n x) =
   * exp(x)<sup>n</sup></i>).
   *
   * @param x Secret value
   * @return A deferred result computing computing e<sup>x</sup>
   */
  public abstract DRes<SFixed> exp(DRes<SFixed> x);

  /**
   * Create a random value between 0 and 1.
   *
   * <p>
   * As it is impossible to uniformly sample the infinite fixeds between 0 and 1, the number will be
   * sampled as <i>r * 2<sup>-n</sup></i> for a random positive <i>n</i> bit number <i>r</i>.
   * </p>
   *
   * @return The random value
   */
  public abstract DRes<SFixed> random();

  /**
   * Calculate the natural logarithm of a secret value.
   *
   * @param x Secret value
   * @return A deferred result computing computing log(x)
   */
  public abstract DRes<SFixed> log(DRes<SFixed> x);

  /**
   * Calculate the square root of a secret value.
   *
   * @param x Secret value
   * @return A deferred result computing &radic;x
   */
  public abstract DRes<SFixed> sqrt(DRes<SFixed> x);

  /**
   * Calculate a power of two <i>c = 2<sup>-k</sup></i> such that the product <i>x * c</i> is in the
   * interval <i>[0.5, 1]</i>.
   *
   * @param x A secret non-zero value
   * @return A deferred result computing the pair <i>(c, k)</i> as decribed above.
   */
  public abstract DRes<Pair<DRes<SFixed>, DRes<SInt>>> normalize(DRes<SFixed> x);

  /**
   * Calculate the reciprocal of a secret value.
   *
   * @param x A secret value
   * @return A deferred result computing 1/x
   */
  public abstract DRes<SFixed> reciprocal(DRes<SFixed> x);

  /**
   * Calculate 2 to a secret integer power which may be negative.
   *
   * @param x A secret value.
   * @return A deferred result computing <i>2<sup>x</sup></i>
   */
  public abstract DRes<SFixed> twoPower(DRes<SInt> x);

  /**
   * Evaluate a polynomial with public coefficients and a secret input.
   *
   * @param input      A secret value
   * @param polynomial The coefficients for the polynomial in increaseing order of degree.
   * @return A deferred result computing the value of the polynomial on the input.
   */
  public abstract DRes<SFixed> polynomialEvalutation(DRes<SFixed> input, double... polynomial);

  /**
   * Pick one of two inputs depending on a condition. The first is picked if the condition is 1 and
   * the other if the condition is 0.
   *
   * @param condition A secret value which is 0 or 1
   * @param first     A secret value
   * @param second    A secret value
   * @return
   */
  public abstract DRes<SFixed> condSelect(DRes<SInt> condition, DRes<SFixed> first,
      DRes<SFixed> second);

  /**
   * Compute the floor of a secret input.
   *
   * @param x A secret value.
   * @return A deferred result computing &lfloor; x &rfloor;
   */
  public abstract DRes<SInt> floor(DRes<SFixed> x);

  /**
   * Compute the sign of a secret input.
   *
   * @param x A secret value.
   * @return A deferred result computing the sign for x (0 has positive sign).
   */
  public abstract DRes<SInt> sign(DRes<SFixed> x);

}
