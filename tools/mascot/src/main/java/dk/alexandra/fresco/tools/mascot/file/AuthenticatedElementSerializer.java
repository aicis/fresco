package dk.alexandra.fresco.tools.mascot.file;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElementSerializer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuthenticatedElementSerializer implements ByteSerializer<AuthenticatedElement> {

  private final int modBitLength;
  private final BigInteger modulus;
  private final FieldElementSerializer fieldSerializer;

  public AuthenticatedElementSerializer( BigInteger modulus) {
    this.modBitLength = modulus.bitLength();
    this.modulus = modulus;
    this.fieldSerializer = new FieldElementSerializer(modulus);
  }

  /**
   * Return size in bytes needed to represent an authenticated element. This is the mac share plus
   * value share. Each of these are max the value of the modulus minus 1.
   */
  public int getElementSize() {
    return 2 * (modBitLength / 8);
  }

  @Override
  public byte[] serialize(AuthenticatedElement object) {
    byte[] share = fieldSerializer.serialize(object.getShare());
    byte[] mac = fieldSerializer.serialize(object.getMac());
    byte[] arr = new byte[share.length + mac.length];
    System.arraycopy(share, 0, arr, 0, share.length);
    System.arraycopy(mac, 0, arr, share.length, mac.length);
    return arr;
  }

  @Override
  public byte[] serialize(List<AuthenticatedElement> objects) {
    byte[] res = new byte[getElementSize() * objects.size()];
    int currentPos = 0;
    for (AuthenticatedElement currentObj : objects) {
      byte[] currentSerialized = serialize(currentObj);
      System.arraycopy(currentSerialized, 0, res, currentPos, currentSerialized.length);
      currentPos += currentSerialized.length;
    }
    return res;
  }

  @Override
  public AuthenticatedElement deserialize(byte[] bytes) {
    byte[] byteShare = Arrays.copyOfRange(bytes, 0, modBitLength / 8);
    byte[] byteMac = Arrays.copyOfRange(bytes, modBitLength / 8, bytes.length);
    FieldElement share = fieldSerializer.deserialize(byteShare);
    FieldElement mac = fieldSerializer.deserialize(byteMac);
    AuthenticatedElement res = new AuthenticatedElement(share, mac, modulus);
    return res;
  }

  @Override
  public List deserializeList(byte[] bytes) {
    int amount = bytes.length / getElementSize();
    List<AuthenticatedElement> res = new ArrayList<>(amount);
    System.out.println("Deserializing " + amount);
    for (int i = 0; i < amount; i++) {
      Object currentObj = deserialize(
          Arrays.copyOfRange(bytes, i * getElementSize(), (i + 1) * getElementSize()));
      res.add((AuthenticatedElement) currentObj);
    }
    System.out.println(res);
    return res;
  }
}
