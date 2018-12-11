package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;

public interface Modulus<T> extends Serializable {

  public Modulus<T> half();

  public BigInteger getBigInteger();

  public int bytesLength();

  public int bitLength();

  public boolean equals(Object o);

  public int hashCode();

  public String toString();
}
