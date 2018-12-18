package dk.alexandra.fresco.suite.spdz2k.datatypes;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.suite.spdz2k.util.UIntSerializer;
import java.math.BigInteger;
import java.security.SecureRandom;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class CompUInt128Factory implements CompUIntFactory<CompUInt128> {

  private static final CompUInt128 ZERO = new CompUInt128(new byte[16]);
  private static final BigInteger valueModulus = BigInteger.ONE.shiftLeft(64);
  private static final BigInteger valueHalfModulus = BigInteger.ONE.shiftLeft(32);

  private final SecureRandom random;

  public CompUInt128Factory() {
    random = new SecureRandom();
  }

  @Override
  public CompUInt128 deserialize(byte[] bytes) {
    return new CompUInt128(bytes);
  }

  @Override
  public CompUInt128 createRandom() {
    byte[] bytes = new byte[16];
    this.random.nextBytes(bytes);
    return this.deserialize(bytes);
  }

  @Override
  public ByteSerializer<CompUInt128> createSerializer() {
    return new UIntSerializer<>(this);
  }

  @Override
  public int getLowBitLength() {
    return 64;
  }

  @Override
  public int getHighBitLength() {
    return 64;
  }

  @Override
  public CompUInt128 createElement(BigInteger value) {
    return value == null ? null : new CompUInt128(value.toByteArray(), true);
  }

  @Override
  public CompUInt128 createElement(int value) {
    return new CompUInt128(value);
  }

  @Override
  public BigInteger getModulus() {
    return valueModulus;
  }

  @Override
  public StrictBitVector convertToBitVector(FieldElement fieldElement) {
    throw new NotImplementedException();
  }

  @Override
  public BigInteger convertToUnsigned(FieldElement value) {
    return ((CompUInt128) value)
        .getLeastSignificant()
        .toBigInteger();
  }

  @Override
  public BigInteger convertToSigned(BigInteger signed) {
    if (signed.compareTo(valueHalfModulus) > 0) {
      return signed.subtract(valueModulus);
    } else {
      return signed;
    }
  }

  @Override
  public CompUInt128 zero() {
    return ZERO;
  }

}
