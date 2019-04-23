package dk.alexandra.fresco.framework.network.serializers;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.util.List;

public class StrictBitVectorSerializer implements ByteSerializer<StrictBitVector> {

  @Override
  public byte[] serialize(StrictBitVector obj) {
    return obj.toByteArray();
  }

  @Override
  public StrictBitVector deserialize(byte[] data) {
    return new StrictBitVector(data);
  }

  @Override
  public byte[] serialize(List<StrictBitVector> objs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<StrictBitVector> deserializeList(byte[] data) {
    throw new UnsupportedOperationException();
  }
}
