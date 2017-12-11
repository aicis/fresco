package dk.alexandra.fresco.tools.commitment;

import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.network.serializers.SecureSerializer;

public class CommitmentSerializer implements SecureSerializer<Commitment> {

  @Override
  public byte[] serialize(Commitment comm) {
    return comm.commitmentVal;
  }

  @Override
  public Commitment deserialize(byte[] data) {
    Commitment comm = new Commitment();
    comm.commitmentVal = data.clone();
    return comm;
  }

  @Override
  public byte[] serialize(List<Commitment> elements) {
    if (elements.isEmpty()) {
      return new byte[] {};
    }
    byte[] commList = new byte[elements.size() * Commitment.digestLength];
    // ensure all field elements are in the same field and have same bit length
    for (int i = 0; i < commList.length; i++) {
      System.arraycopy(elements.get(i).commitmentVal, 0, commList,
          i * Commitment.digestLength, Commitment.digestLength);
    }
    return commList;
  }

  @Override
  public List<Commitment> deserializeList(byte[] data) {
    if (data.length % Commitment.digestLength != 0) {
      throw new IllegalArgumentException(
          "The length of the byte array to deserialize is wrong.");
    }
    if (data.length == 0) {
      return new ArrayList<>();
    }
    List<Commitment> res = new ArrayList<>(data.length / Commitment.digestLength);
    for (int i = 0; i < data.length / Commitment.digestLength; i++) {
      Commitment comm = new Commitment();
      byte[] commVal = new byte[Commitment.digestLength];
      System.arraycopy(data, i * Commitment.digestLength,
          commVal, 0, Commitment.digestLength);
      comm.commitmentVal = commVal;
      res.add(comm);
    }
    return res;
  }

}
