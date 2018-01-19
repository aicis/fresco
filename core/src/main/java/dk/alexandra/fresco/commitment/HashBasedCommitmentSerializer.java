package dk.alexandra.fresco.commitment;

import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;

import java.util.ArrayList;
import java.util.List;

public class HashBasedCommitmentSerializer implements ByteSerializer<HashBasedCommitment> {

  @Override
  public byte[] serialize(HashBasedCommitment comm) {
    return comm.getCommitmentValue();
  }

  @Override
  public byte[] serialize(List<HashBasedCommitment> elements) {
    if (elements.isEmpty()) {
      return new byte[] {};
    }
    // A serialized commitment only consists of its internal digest
    byte[] commList = new byte[elements.size() * HashBasedCommitment.DIGEST_LENGTH];
    // Ensure all field elements are in the same field and have same bit length
    for (int i = 0; i < elements.size(); i++) {
      System.arraycopy(elements.get(i).getCommitmentValue(), 0, commList,
          i * HashBasedCommitment.DIGEST_LENGTH, HashBasedCommitment.DIGEST_LENGTH);
    }
    return commList;
  }

  @Override
  public HashBasedCommitment deserialize(byte[] data) {
    if (data.length != HashBasedCommitment.DIGEST_LENGTH) {
      throw new IllegalArgumentException(
          "The length of the byte array to deserialize is wrong.");
    }
    HashBasedCommitment comm = new HashBasedCommitment();
    comm.setCommitmentValue(data.clone());
    return comm;
  }

  @Override
  public List<HashBasedCommitment> deserializeList(byte[] data) {
    if (data.length % HashBasedCommitment.DIGEST_LENGTH != 0) {
      throw new IllegalArgumentException(
          "The length of the byte array to deserialize is wrong.");
    }
    if (data.length == 0) {
      return new ArrayList<>();
    }
    // A serialized commitment only consists of its internal digest
    List<HashBasedCommitment> res = new ArrayList<>(
        data.length / HashBasedCommitment.DIGEST_LENGTH);
    for (int i = 0; i < data.length / HashBasedCommitment.DIGEST_LENGTH; i++) {
      HashBasedCommitment comm = new HashBasedCommitment();
      byte[] commVal = new byte[HashBasedCommitment.DIGEST_LENGTH];
      System.arraycopy(data, i * HashBasedCommitment.DIGEST_LENGTH,
          commVal, 0, HashBasedCommitment.DIGEST_LENGTH);
      comm.setCommitmentValue(commVal);
      res.add(comm);
    }
    return res;
  }
}
