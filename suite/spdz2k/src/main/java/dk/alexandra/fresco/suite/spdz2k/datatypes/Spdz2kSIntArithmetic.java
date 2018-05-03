package dk.alexandra.fresco.suite.spdz2k.datatypes;

/**
 * Represents an authenticated, secret-share element.
 *
 * @param <PlainT> type of underlying plain value, i.e., the value type we use for arithmetic.
 */
public class Spdz2kSIntArithmetic<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kSInt<PlainT> {

  /**
   * Creates a {@link Spdz2kSIntArithmetic}.
   */
  public Spdz2kSIntArithmetic(PlainT share, PlainT macShare) {
    super(share, macShare);
  }

  /**
   * Creates a {@link Spdz2kSIntArithmetic} from a public value. <p>All parties compute the mac
   * share of the value but only party one (by convention) stores the public value as the share, the
   * others store 0.</p>
   */
  public Spdz2kSIntArithmetic(PlainT share, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    this(isPartyOne ? share : zero, share.multiply(macKeyShare));
  }

  /**
   * Compute sum of this and other.
   */
  public Spdz2kSIntArithmetic<PlainT> add(Spdz2kSIntArithmetic<PlainT> other) {
    return new Spdz2kSIntArithmetic<>(share.add(other.share), macShare.add(other.macShare));
  }

  /**
   * Compute difference of this and other.
   */
  public Spdz2kSIntArithmetic<PlainT> subtract(Spdz2kSIntArithmetic<PlainT> other) {
    return new Spdz2kSIntArithmetic<>(share.subtract(other.share),
        macShare.subtract(other.macShare));
  }

  /**
   * Compute product of this and constant (open) value.
   */
  public Spdz2kSIntArithmetic<PlainT> multiply(PlainT other) {
    return new Spdz2kSIntArithmetic<>(share.multiply(other), macShare.multiply(other));
  }

  /**
   * Compute sum of this and constant (open) value. <p>All parties compute their mac share of the
   * public value and add it to the mac share of the authenticated value, however only party 1 adds
   * the public value to is value share.</p>
   *
   * @param other constant, open value
   * @param macKeyShare mac key share for maccing open value
   * @param zero zero value
   * @param isPartyOne used to ensure that only one party adds value to share
   * @return result of sum
   */
  public Spdz2kSIntArithmetic<PlainT> addConstant(
      PlainT other, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    Spdz2kSIntArithmetic<PlainT> wrapped = new Spdz2kSIntArithmetic<>(other, macKeyShare, zero,
        isPartyOne);
    return add(wrapped);
  }
  
  @Override
  public String toString() {
    return "Spdz2kSIntArithmetic{" +
        "share=" + share +
        ", macShare=" + macShare +
        '}';
  }

}
