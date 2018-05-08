package dk.alexandra.fresco.suite.spdz2k.datatypes;

/**
 * Represents an authenticated, secret-share element.
 *
 * @param <PlainT> type of underlying plain value, i.e., the value type we use for arithmetic.
 */
public class Spdz2kSIntBoolean<PlainT extends CompUInt<?, ?, PlainT>> extends
    Spdz2kSInt<PlainT> {

  /**
   * Creates a {@link Spdz2kSIntBoolean}.
   */
  public Spdz2kSIntBoolean(PlainT share, PlainT macShare) {
    super(share, macShare);
  }

  /**
   * Creates a {@link Spdz2kSIntBoolean} from a public value. <p>All parties compute the mac share
   * of the value but only party one (by convention) stores the public value as the share, the
   * others store 0.</p>
   */
  public Spdz2kSIntBoolean(PlainT share, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    this(isPartyOne ? share : zero, share.toArithmeticRep().multiply(macKeyShare));
  }

  /**
   * Compute sum of this and other.
   */
  public Spdz2kSIntBoolean<PlainT> xor(Spdz2kSIntBoolean<PlainT> other) {
    return new Spdz2kSIntBoolean<>(
        share.add(other.share),
        macShare.add(other.macShare)
    );
  }

  /**
   * Compute product of this and constant (open) value.
   */
  public Spdz2kSIntBoolean<PlainT> and(int otherBit) {
    return new Spdz2kSIntBoolean<>(
        share.multiplyByBit(otherBit),
        macShare.multiplyByBit(otherBit)
    );
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
  public Spdz2kSIntBoolean<PlainT> xorOpen(
      PlainT other, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    Spdz2kSIntBoolean<PlainT> wrapped = new Spdz2kSIntBoolean<>(other, macKeyShare, zero,
        isPartyOne);
    return xor(wrapped);
  }

  public Spdz2kSIntArithmetic<PlainT> asArithmetic() {
    return new Spdz2kSIntArithmetic<>(share.toArithmeticRep(), macShare);
  }

  @Override
  public String toString() {
    return "Spdz2kSIntBoolean{" +
        "share=" + share +
        ", macShare=" + macShare +
        '}';
  }

}
