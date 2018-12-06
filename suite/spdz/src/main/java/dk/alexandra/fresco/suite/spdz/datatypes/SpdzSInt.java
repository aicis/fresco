package dk.alexandra.fresco.suite.spdz.datatypes;

import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.value.SInt;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * Spdz-specific representation of a secret integer.
 */
public class SpdzSInt implements SInt, Serializable {

  private static final long serialVersionUID = 8828769687281856043L;
  private final FieldElement share;
  private final FieldElement mac;
  private final BigInteger mod;

  /**
   * Create a SpdzSInt containing a share, mac and modulus.
   *
   * @param share The share
   * @param mac The mac
   * @param modulus the modulus
   */
  public SpdzSInt(FieldElement share, FieldElement mac, BigInteger modulus) {
    this.share = share;
    this.mac = mac;
    this.mod = modulus;
  }

  public FieldElement getShare() {
    return share;
  }

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
    return new SpdzSInt(share, mac, this.mod);
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
    return new SpdzSInt(share, mac, this.mod);
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
    return new SpdzSInt(share, mac, this.mod);
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
    return new SpdzSInt(share, mac, this.mod);
  }

  @Override
  public String toString() {
    return "spdz(" + share + ", " + mac + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((mac == null) ? 0 : mac.hashCode());
    result = prime * result + ((mod == null) ? 0 : mod.hashCode());
    result = prime * result + ((share == null) ? 0 : share.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SpdzSInt other = (SpdzSInt) obj;
    if (mac == null) {
      if (other.mac != null) {
        return false;
      }
    } else if (!mac.equals(other.mac)) {
      return false;
    }
    if (mod == null) {
      if (other.mod != null) {
        return false;
      }
    } else if (!mod.equals(other.mod)) {
      return false;
    }
    if (share == null) {
      if (other.share != null) {
        return false;
      }
    } else if (!share.equals(other.share)) {
      return false;
    }
    return true;
  }

  @Override
  public SInt out() {
    return this;
  }
}
