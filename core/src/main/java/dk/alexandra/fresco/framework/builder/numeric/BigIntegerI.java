package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;

public interface BigIntegerI extends Comparable<BigIntegerI>, Serializable {

  void mod(BigInteger modulus);

  BigIntegerI modInverse(BigInteger mod);

  void add(BigIntegerI operand);

  void subtract(BigIntegerI operand);

  void multiply(BigIntegerI l);

  BigIntegerI divide(BigIntegerI denominator);

  BigIntegerI divide(int i);

  BigInteger asBigInteger();

  byte[] toByteArray();

  BigIntegerI modPow(BigIntegerI valueOf, BigInteger modulus);

  BigIntegerI copy();
}
