package dk.alexandra.fresco.suite.spdz.datatypes;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import java.security.MessageDigest;
import java.util.Random;

public class SpdzCommitment {

  private final FieldElement value;
  private FieldElement randomness;
  private FieldElement commitment;
  private final MessageDigest hash;
  private final Random rand;
  private final byte[] randomBytes;

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
    this.rand = rand;
    this.hash = hash;
    this.randomBytes = new byte[modulusBitLength / 8 + 1];
    rand.nextBytes(randomBytes);
  }

  /**
   * Compute a commitment.
   *
   * @return If a commitment has already been computed, the existing commitment is returned.
   */
  public FieldElement computeCommitment(FieldDefinition definition) {
    if (commitment != null) {
      return commitment;
    }
    hash.update(definition.serialize(value));
    rand.nextBytes(randomBytes);
    randomness = definition.deserialize(randomBytes);

    hash.update(definition.serialize(this.randomness));
    commitment = definition.deserialize(hash.digest());

    return this.commitment;
  }

  public FieldElement getValue() {
    return this.value;
  }

  public FieldElement getRandomness() {
    return this.randomness;
  }

  @Override
  public String toString() {
    return "SpdzCommitment["
        + "v:" + this.value + ", "
        + "r:" + this.randomness + ", "
        + "commitment:" + this.commitment + "]";
  }
}
