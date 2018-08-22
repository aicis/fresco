package dk.alexandra.fresco.suite.tinytables.ot.datatypes;

import dk.alexandra.fresco.framework.util.BitSetUtils;
import java.util.BitSet;

/**
 * <p>This class represents the input to an oblivious transfer protocol from the
 * sender.</p>
 *
 * <p>In an oblivious transfer, the sender provides two boolean values,
 * <i>x<sub>0</sub></i> and <i>x<sub>1</sub></i>, and the receiver provides one
 * boolean value <i>&sigma; &isin; {0,1}</i>. After the protocol has finished,
 * the receiver knows <i>x<sub>&sigma;</sub></i>, but not the other input given
 * by the sender, and the sender does not know <i>&sigma;</i>.</p>
 *
 * @author jonas
 *
 */
public class OTInput {


  private BitSet x0, x1;
  private int length;

  /**
   * Create a new {@link OTInput} where both <i>x<sub>0</sub></i> and
   * <i>x<sub>1</sub></i> are single bits.
   *
   * @param x0
   * @param x1
   */
  public OTInput(boolean x0, boolean x1) {
    this(new boolean[] {x0}, new boolean[] {x1});
  }

  /**
   * Create a new {@link OTInput} where <i>x<sub>0</sub></i> and
   * <i>x<sub>1</sub></i> are arrays of bits. Note that they should have the
   * same length.
   *
   * @param x0
   * @param x1
   */
  public OTInput(boolean[] x0, boolean[] x1) {
    if (x0.length != x1.length) {
      throw new IllegalArgumentException("Inputs must have same lenght");
    }
    this.length = x0.length;
    this.x0 = BitSetUtils.fromArray(x0);
    this.x1 = BitSetUtils.fromArray(x1);
  }

  /**
   * Return the length of <i>x<sub>0</sub></i> and <i>x<sub>1</sub></i> (which
   * are equal).
   *
   * @return
   */
  public int getLength() {
    return this.length;
  }

  public BitSet getX0() {
    return x0;
  }

  public BitSet getX1() {
    return x1;
  }

}
