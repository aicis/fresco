package dk.alexandra.fresco.tools.bitTriples.utils;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.bitTriples.BitTripleResourcePool;
import dk.alexandra.fresco.tools.bitTriples.prg.BytePrg;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

  public static StrictBitVector sum(List<StrictBitVector> toSum) {
    StrictBitVector sum = new StrictBitVector(toSum.get(0).toByteArray().clone());
    for (int i = 1; i < toSum.size(); i++) {
      sum.xor(toSum.get(i));
    }
    return sum;
  }

  public static StrictBitVector xor(StrictBitVector a, StrictBitVector b){
    StrictBitVector result = new StrictBitVector(a.toByteArray().clone());
    result.xor(b);
    return result;
  }

  public static boolean sum(StrictBitVector toSum) {
    boolean accumulator = toSum.getBit(0,false);
    for (int i = 1; i<toSum.getSize(); i++){
      accumulator ^= toSum.getBit(i,false);
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
    return sum(atIndex);
  }

  public static StrictBitVector and(StrictBitVector a, StrictBitVector b) {
    if (a.getSize() != b.getSize()) {
      throw new IllegalStateException("Vectors must be the same size");
    }
    StrictBitVector result = new StrictBitVector(a.getSize());
    for (int i = 0; i < a.getSize(); i++) {
      result.setBit(i, a.getBit(i,false) && b.getBit(i,false),false);
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
  public static StrictBitVector openVector(StrictBitVector vector, BitTripleResourcePool resourcePool, Network network) {

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

    StrictBitVector received = sum(rawComms.stream()
        .map(resourcePool.getStrictBitVectorSerializer()::deserialize)
        .collect(Collectors.toList()));
    received.xor(vector);
    return received;
  }

  public static List<StrictBitVector> mapToList(Map<Integer, StrictBitVector> map) {
    List<StrictBitVector> result = new ArrayList<>();
    for(int i = 1; i<= map.size()+1; i++){
        StrictBitVector toAdd = map.get(i);
        if(toAdd != null){
          result.add(map.get(i));
        }
    }
    return result;
  }



  /**
   * Generates a random StrictBitVector with exactly c 1's, and the rest 0's.
   *
   * @param c number of 1's.
   * @param size size of .
   * @return
   */
  public static StrictBitVector generateRandomIndices(int c, int size, BitTripleResourcePool resourcePool, BytePrg jointSampler) {
      SecureRandom random = ExceptionConverter.safe(
          () -> SecureRandom.getInstance("SHA1PRNG"),
          "Configuration error, SHA1PRNG is needed for BitTriple");
      StrictBitVector v = jointSampler.getNext(resourcePool.getPrgSeedBitLength());
      random.setSeed(v.toByteArray());

      StrictBitVector strictBitVector = new StrictBitVector(size);
      return setBits(strictBitVector, random, c);
  }
  /**
   * Runs through the given vector, and sets c bits, that have previously not been set.
   *
   * @param vector vector
   * @param random random
   * @param c number of bits to be set
   * @return The bitvector with c new bits set.
   */
  protected static StrictBitVector setBits(StrictBitVector vector, Random random, int c) {
    if (c <= 0) {
      return vector;
    }
    int index = random.nextInt(vector.getSize());
    if (vector.getBit(index, false)) {
      return setBits(vector, random, c);
    } else {
      vector.setBit(index, true, false);
      return setBits(vector, random, c - 1);
    }
  }


}
