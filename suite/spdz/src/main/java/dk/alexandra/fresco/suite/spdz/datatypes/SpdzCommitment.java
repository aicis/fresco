package dk.alexandra.fresco.suite.spdz.datatypes;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

public class SpdzCommitment {

  private final FieldElement value;
  private final MessageDigest hash;
  private final byte[] randomBytes;
  private byte[] commitment;

  /**
   * Commit to a specific value.
   *
   * @param hash The hashing algorithm to use
   * @param value The value to commit to use
   * @param rand The randomness to use
   * @param modulusBitLength modulus bit length
   */
  public SpdzCommitment(
      MessageDigest hash, FieldElement value, Random rand, int modulusBitLength) {
    this.value = value;
    this.hash = hash;
    this.randomBytes = new byte[modulusBitLength / 8 + 1];
    rand.nextBytes(randomBytes);
  }

  /**
   * Compute a commitment.
   *
   * @return If a commitment has already been computed, the existing commitment is returned.
   */
  public byte[] computeCommitment(ByteSerializer<FieldElement> definition) {
    if (commitment != null) {
      return commitment;
    }
    hash.update(definition.serialize(value));
    hash.update(this.randomBytes);
    commitment = hash.digest();
    return this.commitment;
  }

  public FieldElement getValue() {
    return this.value;
  }

  public byte[] getRandomness() {
    return this.randomBytes;
  }

  @Override
  public String toString() {
    return "SpdzCommitment["
        + "v:" + this.value + ", "
        + "r:" + Arrays.toString(this.randomBytes) + ", "
        + "commitment:" + Arrays.toString(this.commitment) + "]";
  }
}
