package dk.alexandra.fresco.suite.tinytables.datatypes;

import java.io.Serializable;

public class TinyTablesTriple implements Serializable {

  private static final long serialVersionUID = 7661693903147118861L;
  private static final TinyTablesTriple trip0 = new TinyTablesTriple(false, false, false);
  private static final TinyTablesTriple trip1 = new TinyTablesTriple(false, false, true);
  private static final TinyTablesTriple trip2 = new TinyTablesTriple(false, true, false);
  private static final TinyTablesTriple trip3 = new TinyTablesTriple(false, true, true);
  private static final TinyTablesTriple trip4 = new TinyTablesTriple(true, false, false);
  private static final TinyTablesTriple trip5 = new TinyTablesTriple(true, false, true);
  private static final TinyTablesTriple trip6 = new TinyTablesTriple(true, true, false);
  private static final TinyTablesTriple trip7 = new TinyTablesTriple(true, true, true);
  private static final TinyTablesTriple[] trips = new TinyTablesTriple[] {
      trip0,
      trip1,
      trip2,
      trip3,
      trip4,
      trip5,
      trip6,
      trip7
  };

  private final TinyTablesElement elementA;
  private final TinyTablesElement elementB;
  private final TinyTablesElement elementC;

  /**
   * Factory method for TinyTablesTriple.
   *
   * @param shareA the value of the first part of the element
   * @param shareB the value of the second part of the element
   * @param shareC the value of the third part of the element
   * @return
   */
  public static TinyTablesTriple fromShares(boolean shareA, boolean shareB, boolean shareC) {
    int index = (shareA ? 4 : 0) + (shareB ? 2 : 0) + (shareC ? 1 : 0);
    return trips[index];
  }

  private TinyTablesTriple(TinyTablesElement a, TinyTablesElement b, TinyTablesElement c) {
    this.elementA = a;
    this.elementB = b;
    this.elementC = c;
  }

  private TinyTablesTriple(boolean a, boolean b, boolean c) {
    this(TinyTablesElement.getTinyTablesElement(a), TinyTablesElement.getTinyTablesElement(b),
        TinyTablesElement.getTinyTablesElement(c));
  }

  public TinyTablesElement getA() {
    return this.elementA;
  }

  public TinyTablesElement getB() {
    return this.elementB;
  }

  public TinyTablesElement getC() {
    return this.elementC;
  }

  @Override
  public String toString() {
    return "TinyTablesTriple:(" + elementA + "," + elementB + "," + elementC + ")";
  }
}
