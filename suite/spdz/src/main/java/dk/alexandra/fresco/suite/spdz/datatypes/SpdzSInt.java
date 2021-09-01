package dk.alexandra.fresco.suite.spdz.datatypes;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.value.SInt;
import java.io.Serializable;

/**
 * Spdz-specific representation of a secret integer.
 */
public class SpdzSInt implements SInt, Serializable {

  private static final long serialVersionUID = 8828769687281856043L;
  private final FieldElement share;
  private final FieldElement mac;

  /**
   * Create a SpdzSInt containing a share, mac and modulus.
   *
   * @param share The share
   * @param mac The mac
   */
  public SpdzSInt(FieldElement share, FieldElement mac) {
    this.share = share;
    this.mac = mac;
  }

  @Override
  public FieldElement getShare() {
    return share;
  }

  @Override
  public FieldElement getMac() {
    return mac;
  }

  /**
   * Adds two {@link SpdzSInt} instances.
   *
   * @param e The value to add
   * @return The sum
   */
  public SpdzSInt add(SpdzSInt e) {
    FieldElement share = this.share.add(e.getShare());
    FieldElement mac = this.mac.add(e.getMac());
    return new SpdzSInt(share, mac);
  }

  /**
   * Add a public value to this {@link SpdzSInt}.
   *
   * @param e The value to add
   * @param id The party id (used to determine if this call was made by party 1 or not)
   * @return The sum
   */
  public SpdzSInt add(SpdzSInt e, int id) {
    FieldElement share = this.share;
    FieldElement mac = this.mac.add(e.getMac());
    if (id == 1) {
      share = share.add(e.getShare());
    }
    return new SpdzSInt(share, mac);
  }

  /**
   * Subtract a {@link SpdzSInt} from this value.
   *
   * @param e The value to subtract
   * @return The difference
   */
  public SpdzSInt subtract(SpdzSInt e) {
    FieldElement share = this.share.subtract(e.getShare());
    FieldElement mac = this.mac.subtract(e.getMac());
    return new SpdzSInt(share, mac);
  }

  /**
   * Multiply this {@link SpdzSInt} with a constant.
   *
   * @param c The constant to multiply
   * @return The product
   */
  public SpdzSInt multiply(FieldElement c) {
    FieldElement share = this.share.multiply(c);
    FieldElement mac = this.mac.multiply(c);
    return new SpdzSInt(share, mac);
  }

  @Override
  public String toString() {
    return "spdz(" + share + ", " + mac + ")";
  }

  @Override
  public SInt out() {
    return this;
  }

  public byte[] serializeShare(ByteSerializer<FieldElement> serializer) {
    return serializer.serialize(getShare());
  }
}
