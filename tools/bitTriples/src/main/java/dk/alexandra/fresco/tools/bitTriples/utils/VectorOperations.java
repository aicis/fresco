package dk.alexandra.fresco.tools.bitTriples.utils;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.bitTriples.BitTripleResourcePool;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VectorOperations {

  public static boolean isZero(StrictBitVector vector) {
    return vector.equals(new StrictBitVector(vector.getSize()));
  }

  public static StrictBitVector multiply(StrictBitVector vector, boolean bit) {
    if (bit) {
      return vector;
    } else {
      return new StrictBitVector(vector.getSize());
    }
  }

  public static List<StrictBitVector> multiply(List<StrictBitVector> list, StrictBitVector r) {
    if (r.getSize() != list.size()) {
      throw new IllegalStateException("List and vector must be the same size");
    }
    List<StrictBitVector> result = new ArrayList<>();
    for (int i = 0; i < r.getSize(); i++) {
      result.add(multiply(list.get(i), r.getBit(i, false)));
    }
    return result;
  }

  public static StrictBitVector bitwiseXor(List<StrictBitVector> toSum) {
    StrictBitVector sum = new StrictBitVector(toSum.get(0).toByteArray().clone());
    for (int i = 1; i < toSum.size(); i++) {
      sum.xor(toSum.get(i));
    }
    return sum;
  }

  public static boolean xorAll(StrictBitVector toSum) {
    boolean accumulator = toSum.getBit(0);
    for (int i = 1; i<toSum.getSize(); i++){
      accumulator ^= toSum.getBit(i);
    }
    return accumulator;
  }

  public static StrictBitVector xorIndex(List<List<StrictBitVector>> toSum, int index) {
    int size = toSum.get(0).size();
    for(List<StrictBitVector> l : toSum){
      if (l.size() != size){
        throw new IllegalStateException("Vectors must be the same size");
      }
    }
    List<StrictBitVector> atIndex = toSum.stream().map(l -> l.get(index)).collect(Collectors.toList());
    return bitwiseXor(atIndex);
  }

  public static StrictBitVector bitwiseAnd(StrictBitVector left, StrictBitVector right) {
    if (left.getSize() != right.getSize()) {
      throw new IllegalStateException("Vectors must be the same size");
    }
    StrictBitVector result = new StrictBitVector(left.getSize());
    for (int i = 0; i < left.getSize(); i++) {
      result.setBit(i, left.getBit(i,false) && right.getBit(i,false));
    }
    return result;
  }

  /**
   * Collapses a n x l matrix of StrictBitVectors into an list of StrictBitVectors of size l, by
   * xoring the n StrictBitVectors at matching indices.
   *
   * @param shares The shares
   * @param l the 'height' of the matrix
   * @return list of StrictBitVectors
   */
  public static List<StrictBitVector> xorMatchingIndices(List<List<StrictBitVector>> shares, int l) {
    List<StrictBitVector> resultingVectors = new ArrayList<>();
    for (int i = 0; i < l; i++) {
      StrictBitVector newVector = null;
      for (List<StrictBitVector> list : shares) {
        if (newVector == null) {
          newVector = list.get(i);
        } else {
          newVector.xor(list.get(i));
        }
      }
      resultingVectors.add(newVector);
    }
    return resultingVectors;
  }


  /**
   * Sends vector to others and receives others' vector.
   *
   * @param vector own vector
   */
  public static List<StrictBitVector> distributeVector(StrictBitVector vector, BitTripleResourcePool resourcePool, Network network) {

    List<byte[]> rawComms = new ArrayList<>();
    for (int otherId = 1; otherId <= resourcePool.getNoOfParties(); otherId++) {
      if (resourcePool.getMyId() != otherId) {
        if (resourcePool.getMyId() < otherId) {
          network.send(otherId,resourcePool.getStrictBitVectorSerializer().serialize(vector));
          rawComms.add(network.receive(otherId));
        } else {
          rawComms.add(network.receive(otherId));
          network.send(otherId,resourcePool.getStrictBitVectorSerializer().serialize(vector));
        }
      }
    }
    return rawComms.stream()
        .map(resourcePool.getStrictBitVectorSerializer()::deserialize)
        .collect(Collectors.toList());
  }

  public static List<StrictBitVector> mapToList(Map<Integer, StrictBitVector> map, int noOfParties, int myId) {
    List<StrictBitVector> result = new ArrayList<>();
    for(int i = 0; i< noOfParties; i++){
      if (i != myId){
        result.add(map.get(i));
      }
    }
    return result;
  }
}
