package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;

public interface Modulus extends Serializable {

  public BigInteger getBigInteger();

  public int bytesLength();

  public int bitLength();
}
