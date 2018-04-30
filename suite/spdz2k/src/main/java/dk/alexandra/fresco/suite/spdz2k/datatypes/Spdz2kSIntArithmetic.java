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
    super(macShare, share);
  }
  
  /**
   * Creates a {@link Spdz2kSIntArithmetic} from a public value. <p>All parties compute the mac
   * share of the value but only party one (by convention) stores the public value as the share, the
   * others store 0.</p>
   */
  public Spdz2kSIntArithmetic(PlainT share, PlainT macKeyShare, PlainT zero, boolean isPartyOne) {
    this(isPartyOne ? share : zero, share.multiply(macKeyShare));
  }

  @Override
  public byte[] serializeShare() {
    return share.getLeastSignificant().toByteArray();
  }

}
