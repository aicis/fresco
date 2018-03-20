package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Superclass containing the common variables and methods for the sender and receiver parties of
 * random OT extension.
 */
public abstract class RotSharedImpl {
  private final CoinTossing ct;
  private final int comSecParam;
  private final MessageDigest digest;

  /**
   * Constructs a random OT extension super-class using an underlying correlated OT with errors
   * object.
   *
   * @param ct The coin tossing instance to use
   * @param digest The hash function to use
   * @param comSecParam The computational security parameter
   */
  public RotSharedImpl(CoinTossing ct, MessageDigest digest, int comSecParam) {
    this.ct = ct;
    this.digest = digest;
    this.comSecParam = comSecParam;
  }


  /**
   * Computes the inner product of two lists of StrictBitVector objects. The product will be based
   * on Galois multiplication in the binary extension field of the individual elements of the lists,
   * but without reducing modulo a reduction polynomial. Thus the inner product is computed purely
   * using shifts and XOR operations. <br/>
   * All elements of both lists MUST have equal size! And both lists MUST contain an equal amount of
   * entries!
   *
   * @param alist The first input list, with all elements of equal size
   * @param blist The second input list, with all elements of equal size
   * @return The inner product represented as a StrictBitVector
   */
  protected static StrictBitVector computeInnerProduct(List<StrictBitVector> alist,
      List<StrictBitVector> blist) {
    // All elements of each list MUST have equal size so we find the size using the first element.
    StrictBitVector res = new StrictBitVector(alist.get(0).getSize() + blist.get(0).getSize());
    List<StrictBitVector> products = IntStream.range(0, alist.size()).parallel()
        .mapToObj(i -> multiplyWithoutReduction(alist.get(i), blist.get(i)))
        .collect(Collectors.toList());
    products.stream().reduce(res, (a, b) -> {
      a.xor(b);
      return a;
    });
    return res;
  }

  /**
   * Computes the Galois product of two bit vectors, without reduction modulo a reduction
   * polynomial.
   *
   * @param avec The first bit vector
   * @param bvec The second bit vector
   * @return The product represented as a StrictBitVector
   */
  private static StrictBitVector multiplyWithoutReduction(StrictBitVector avec,
      StrictBitVector bvec) {
    byte[] res = new byte[(avec.getSize() + bvec.getSize()) / Byte.SIZE];
    byte[] avecBytes = avec.toByteArray();
    byte[] bvecBytes = bvec.toByteArray();
    byte[][] rotations = new byte[Byte.SIZE][bvecBytes.length + 1];
    System.arraycopy(bvecBytes, 0, rotations[0], 0, bvecBytes.length);
    for (int i = 1; i < Byte.SIZE; i++) {
      for (int j = 0; j < rotations[i].length - 1; j++) {
        int b = Byte.toUnsignedInt(bvecBytes[j]);
        rotations[i][j] ^= (byte) (b >> i);
        rotations[i][j + 1] = (byte) (b << Byte.SIZE - i);
      }
    }
    // multiply using the school book method (where addition is XOR)
    for (int i = 0; i < avec.getSize(); i++) {
      int bitIndex = i % Byte.SIZE;
      int byteIndex = i / Byte.SIZE;
      byte currentByte = (byte) (avecBytes[byteIndex] >> ((Byte.SIZE - 1) - (bitIndex)));
      if ((currentByte & 1) == 1) {
        byte[] currentRotation = rotations[bitIndex];
        for (int j = 0; j < currentRotation.length; j++) {
          res[byteIndex + j] ^= currentRotation[j];
        }
      }
    }
    return new StrictBitVector(res);
  }

  /**
   * Compute a SHA-256 digest of elements in a list, concatenated with their index in the list. Only
   * the first {@code size} elements of the list will be hashed.
   *
   * @param input The list of StrictBitVector elements to hash. All elements MUST have same length
   * @param size The amount of elements of the list, to hash. Must be less than or equal to the
   *        amount of elements in the list.
   * @return A list containing the hashed StrictBitVector as StrictBitVector objects
   */
  protected List<StrictBitVector> hashBitVector(List<StrictBitVector> input, int size) {
    List<StrictBitVector> res = new ArrayList<>(size);
    // Allocate a buffer to contain the index of the value to hash along with
    // the value itself.
    ByteBuffer indexBuffer = ByteBuffer.allocate((Integer.SIZE + input.get(0).getSize()) / 8);
    byte[] hash;
    for (int i = 0; i < size; i++) {
      indexBuffer.clear();
      // Move the index into the buffer
      indexBuffer.putInt(i);
      // Move the value to hash into the buffer
      indexBuffer.put(input.get(i).toByteArray());
      hash = digest.digest(indexBuffer.array());
      // Allocate the new bitvector, which contains 256 bits since SHA-256 is
      // used
      res.add(new StrictBitVector(hash));
    }
    return res;
  }

  /**
   * Agree on a list of {@code size} coin-tossed elements, represented by StrictBitVectors. Each
   * consisting of bits reflecting the computational security used at initialization of this class
   *
   * @param size The amount of elements in the resultant list
   * @return The list of coin-tossed elements
   */
  protected List<StrictBitVector> getChallenges(int size) {
    List<StrictBitVector> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      StrictBitVector currentToss = ct.toss(comSecParam);
      list.add(currentToss);
    }
    return list;
  }
}
