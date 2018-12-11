package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;

public interface Modulus extends Serializable {

  BigInteger getBigInteger();

  BigInteger getBigIntegerHalved();
}
