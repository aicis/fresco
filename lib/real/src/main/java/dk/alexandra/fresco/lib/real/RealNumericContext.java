package dk.alexandra.fresco.lib.real;

public class RealNumericContext {

  private final int precision;

  /**
   * @param precision the number of bits used for the fractional part in fixed number arithmetic.
   */
  public RealNumericContext(int precision) {
    this.precision = precision;
  }

  /**
   * Returns the precision, eg. the number of bits used for the fractional part in fixed number
   * arithmetic.
   * 
   * @return
   */
  public int getPrecision() {
    return precision;
  }

}
