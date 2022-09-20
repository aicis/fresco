package dk.alexandra.fresco.tools.ot.base;

import java.math.BigInteger;

public interface InterfaceNaorPinkasElement {

  byte[] toByteArray();

  InterfaceNaorPinkasElement groupOp(InterfaceNaorPinkasElement other);

  InterfaceNaorPinkasElement inverse();

  InterfaceNaorPinkasElement multiply(BigInteger other);


}
