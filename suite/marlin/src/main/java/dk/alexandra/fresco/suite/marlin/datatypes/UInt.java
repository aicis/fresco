package dk.alexandra.fresco.suite.marlin.datatypes;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class UInt implements BigUInt<UInt> {

  private final int[] chunks;

  public UInt(int[] chunks) {
    this.chunks = chunks;
  }

  public UInt(byte[] bytes, int requiredBitLength) {
    this(toIntArray(bytes, requiredBitLength));
  }

  public UInt(BigInteger value, int requiredBitLength) {
    this(value.toByteArray(), requiredBitLength);
  }

  @Override
  public UInt add(UInt other) {
    return null;
  }

  @Override
  public UInt multiply(UInt other) {
    return null;
  }

  @Override
  public UInt subtract(UInt other) {
    return null;
  }

  @Override
  public UInt negate() {
    return null;
  }

  @Override
  public boolean isZero() {
    return false;
  }

  @Override
  public int getBitLength() {
    return chunks.length * Integer.SIZE;
  }

  @Override
  public byte[] toByteArray() {
    ByteBuffer buffer = ByteBuffer.allocate(getBitLength() / Byte.SIZE);
    IntBuffer intBuffer = buffer.asIntBuffer();
    intBuffer.put(chunks);
    return buffer.array();
  }

  @Override
  public BigInteger toBigInteger() {
    return new BigInteger(1, toByteArray());
  }

  @Override
  public long getLow() {
    return 0;
  }

  @Override
  public long getHigh() {
    return 0;
  }

  @Override
  public UInt shiftLowIntoHigh() {
    return null;
  }

  private static int[] toIntArray(byte[] bytes, int requiredBitLength) {
    IntBuffer intBuf =
        ByteBuffer.wrap(pad(bytes, requiredBitLength))
            .order(ByteOrder.BIG_ENDIAN)
            .asIntBuffer();
    int[] array = new int[intBuf.remaining()];
    intBuf.get(array);
    return array;
  }

  private static byte[] pad(byte[] bytes, int requiredBitLength) {
    byte[] padded = new byte[requiredBitLength / 8];
    // potentially drop byte containing sign bit
    boolean dropSignBitByte = (bytes[0] == 0x00);
    int bytesLen = dropSignBitByte ? bytes.length - 1 : bytes.length;
    if (bytesLen > padded.length) {
      throw new IllegalArgumentException("Exceeds capacity");
    }
    int srcPos = dropSignBitByte ? 1 : 0;
    System.arraycopy(bytes, srcPos, padded, padded.length - bytesLen, bytesLen);
    return padded;
  }

}
