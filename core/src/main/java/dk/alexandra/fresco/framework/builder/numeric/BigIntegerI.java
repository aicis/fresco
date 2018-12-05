package dk.alexandra.fresco.framework.builder.numeric;

import java.io.Serializable;
import java.math.BigInteger;

public interface BigIntegerI extends Comparable<BigIntegerI>, Serializable {

  BigIntegerI modInverse(BigInteger mod);

  BigIntegerI add(BigIntegerI operand);

  BigIntegerI subtract(BigIntegerI operand);

  BigIntegerI multiply(BigIntegerI l);

  BigIntegerI divide(BigIntegerI denominator);

  BigIntegerI divide(int i);

  BigInteger asBigInteger();

  void toByteArray(byte[] bytes, int offset, int byteLength);

  BigIntegerI modPow(BigIntegerI valueOf, BigInteger modulus);

}
