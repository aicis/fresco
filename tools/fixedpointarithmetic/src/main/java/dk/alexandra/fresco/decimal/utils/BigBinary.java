package dk.alexandra.fresco.decimal.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Instances of this class represents a fractional value represented in binary as <i>value =
 * unscaled * 2<sub>-scale</sub></i>. This can be seen as a simpel version of {@link BigDecimal}
 * where we use base 2 instead of base 10.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class BigBinary {

  private BigInteger unscaled;
  private int scale;

  public BigBinary(BigInteger unscaled, int scale) {
    this.unscaled = unscaled;
    this.scale = scale;
  }
  
  /**
   * Create a new BigBinary representing the given BigDecimal value with the given scale.
   * 
   * @param value
   * @param scale
   */
  public BigBinary(BigDecimal value, int scale) {
    this(value.multiply(BigDecimal.valueOf(2.0).pow(scale)).toBigInteger(), scale);
  }
  
  /**
   * Return the scale of this BigBinary.
   * 
   * @return
   */
  public int scale() {
    return scale;
  }
  
  /**
   * Return the unscaled value of this BigBinary.
   * 
   * @return
   */
  public BigInteger unscaledValue() {
    return unscaled;
  }
  
  /**
   * Return a new BigBinary value which represents the same value as this but with the given scale.
   * 
   * @param scale
   * @return
   */
  public BigBinary setScale(int scale) {
    BigInteger newUnscaled = unscaled;
    if (scale < this.scale) {
      newUnscaled = unscaled.shiftRight(this.scale - scale);
    } else if (scale > this.scale) {
      newUnscaled = unscaled.shiftLeft(scale - this.scale);
    }
    return new BigBinary(newUnscaled, scale);
  }
  
  public BigDecimal toBigDecimal() {
    return new BigDecimal(unscaled).setScale(scale).divide(BigDecimal.valueOf(2.0).pow(scale), RoundingMode.HALF_UP);
  }
  
  @Override
  public String toString() {
    System.out.println(unscaled);
    System.out.println(BigDecimal.valueOf(2.0).pow(scale));
    return unscaled + " * 2^-" + scale + " = " + toBigDecimal(); 
  }
  
}
