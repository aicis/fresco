package dk.alexandra.fresco.tools.commitment;

import java.util.List;

import dk.alexandra.fresco.framework.network.serializers.SecureSerializer;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;

public class CommitmentSerializer implements SecureSerializer<Commitment> {

  @Override
  public byte[] serialize(Commitment obj) {
    // TODO
    return null;
  }

  @Override
  public Commitment deserialize(byte[] data) {
    // TODO
    return (Commitment) ByteArrayHelper.deserialize(data);
  }

  @Override
  public byte[] serialize(List<Commitment> objs) {
    // TODO
    return null;
  }

  @Override
  public List<Commitment> deserializeList(byte[] data) {
    // TODO
    return null;
  }

}
