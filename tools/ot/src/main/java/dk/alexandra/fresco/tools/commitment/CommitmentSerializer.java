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
  public byte[] serialize(List<Commitment> elements) {
    if (elements.isEmpty()) {
      return new byte[] {};
    }
    // A serialized commitment only consists of its internal digest
    byte[] commList = new byte[elements.size() * Commitment.digestLength];
    for (int i = 0; i < elements.size(); i++) {
      System.arraycopy(elements.get(i).commitmentVal, 0, commList,
          i * Commitment.digestLength, Commitment.digestLength);
    }
    return commList;
  }

  @Override
  public Commitment deserialize(byte[] data) {
    if (data.length != Commitment.digestLength) {
      throw new IllegalArgumentException(
          "The length of the byte array to deserialize is wrong.");
    }
    Commitment comm = new Commitment();
    comm.commitmentVal = data.clone();
    return comm;
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
    // A serialized commitment only consists of its internal digest
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
