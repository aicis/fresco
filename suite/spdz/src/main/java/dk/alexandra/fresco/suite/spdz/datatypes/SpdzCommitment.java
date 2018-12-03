package dk.alexandra.fresco.suite.spdz.datatypes;

import dk.alexandra.fresco.framework.builder.numeric.BigIntegerI;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Random;

public class SpdzCommitment {

  private BigIntegerI value;
  private BigIntegerI randomness;
  private BigIntegerI commitment;
  private Random rand;
  private MessageDigest hash;

  /**
   * Commit to a specific value.
   *
   * @param hash The hashing algorithm to use
   * @param value The value to commit to use
   * @param rand The randomness to use
   */
  public SpdzCommitment(MessageDigest hash, BigIntegerI value, Random rand) {
    this.value = value;
    this.rand = rand;
    this.hash = hash;
  }

  /**
   * Compute a commitment.
   *
   * @param modulus The modulus to use
   * @return If a commitment has already been computed, the existing commitment is returned.
   */
  public BigIntegerI computeCommitment(BigInteger modulus, ByteSerializer<BigIntegerI> serializer) {
    if (commitment != null) {
      return commitment;
    }
    hash.update(value.toByteArray());
    byte[] randomBytes = new byte[modulus.bitLength() / 8 + 1];
    rand.nextBytes(randomBytes);
    BigIntegerI deserialize = serializer.deserialize(randomBytes);
    randomness = deserialize.copy();
    randomness.mod(modulus);

    hash.update(this.randomness.toByteArray());
    commitment = serializer.deserialize(hash.digest());
    commitment.mod(modulus);

    return this.commitment;
  }

  public BigIntegerI getValue() {
    return this.value;
  }

  public BigIntegerI getRandomness() {
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
