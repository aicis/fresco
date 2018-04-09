package dk.alexandra.fresco.suite.spdz.utils;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;
import java.util.List;

/**
 * A serializer for the value share portion of {@link SpdzSInt} instances.
 */
public class SpdzSIntShareSerializer implements ByteSerializer<SpdzSInt> {

  private final ByteSerializer<BigInteger> serializer;
  private final int byteLength;

  public SpdzSIntShareSerializer(ByteSerializer<BigInteger> serializer, int byteLength) {
    this.serializer = serializer;
    this.byteLength = byteLength;
  }

  @Override
  public byte[] serialize(SpdzSInt object) {
    return serializer.serialize(object.getShare());
  }

  @Override
  public byte[] serialize(List<SpdzSInt> objects) {
    final byte[] bytes = new byte[objects.size() * byteLength];
    for (int i = 0; i < objects.size(); i++) {
      BigInteger share = objects.get(i).getShare();
      System.arraycopy(serializer.serialize(share), 0, bytes,
          i * byteLength, byteLength);
    }
    return bytes;
  }

  @Override
  public SpdzSInt deserialize(byte[] bytes) {
    return null;
  }

  @Override
  public List<SpdzSInt> deserializeList(byte[] bytes) {
    return null;
  }

}
