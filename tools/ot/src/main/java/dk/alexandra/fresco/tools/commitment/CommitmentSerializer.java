package dk.alexandra.fresco.tools.commitment;

import dk.alexandra.fresco.framework.network.serializers.SecureSerializer;

import java.util.ArrayList;
import java.util.List;

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
    byte[] commList = new byte[elements.size() * Commitment.DIGEST_LENGTH];
    // ensure all field elements are in the same field and have same bit length
    for (int i = 0; i < elements.size(); i++) {
      System.arraycopy(elements.get(i).commitmentVal, 0, commList,
          i * Commitment.DIGEST_LENGTH, Commitment.DIGEST_LENGTH);
    }
    return commList;
  }

  @Override
  public Commitment deserialize(byte[] data) {
    if (data.length != Commitment.DIGEST_LENGTH) {
      throw new IllegalArgumentException(
          "The length of the byte array to deserialize is wrong.");
    }
    Commitment comm = new Commitment();
    comm.commitmentVal = data.clone();
    return comm;
  }

  @Override
  public List<Commitment> deserializeList(byte[] data) {
    if (data.length % Commitment.DIGEST_LENGTH != 0) {
      throw new IllegalArgumentException(
          "The length of the byte array to deserialize is wrong.");
    }
    if (data.length == 0) {
      return new ArrayList<>();
    }
    // A serialized commitment only consists of its internal digest
    List<Commitment> res = new ArrayList<>(data.length / Commitment.DIGEST_LENGTH);
    for (int i = 0; i < data.length / Commitment.DIGEST_LENGTH; i++) {
      Commitment comm = new Commitment();
      byte[] commVal = new byte[Commitment.DIGEST_LENGTH];
      System.arraycopy(data, i * Commitment.DIGEST_LENGTH,
          commVal, 0, Commitment.DIGEST_LENGTH);
      comm.commitmentVal = commVal;
      res.add(comm);
    }
    return res;
  }
}
