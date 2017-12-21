package dk.alexandra.fresco.framework.network.serializers;

import java.util.List;

import dk.alexandra.fresco.framework.util.StrictBitVector;

public class StrictBitVectorSerializer implements ByteSerializer<StrictBitVector> {

  @Override
  public byte[] serialize(StrictBitVector obj) {
    return obj.toByteArray();
  }

  @Override
  public StrictBitVector deserialize(byte[] data) {
    return new StrictBitVector(data, data.length * 8);
  }

  @Override
  public byte[] serialize(List<StrictBitVector> objs) {
    StrictBitVector combined =
        StrictBitVector.concat(objs.toArray(new StrictBitVector[objs.size()]));
    return combined.toByteArray();
  }

  @Override
  public List<StrictBitVector> deserializeList(byte[] data) {
    // TODO generalize FieldElement unpack to byte array and use that
    return null;
  }

}
