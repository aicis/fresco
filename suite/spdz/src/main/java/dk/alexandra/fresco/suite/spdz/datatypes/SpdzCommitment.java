package dk.alexandra.fresco.suite.spdz.datatypes;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Random;

public class SpdzCommitment {

  private BigInteger value;
  private BigInteger randomness;
  private BigInteger commitment;
  private Random rand;
  private MessageDigest hash;

  /**
   * Commit to a specific value.
   * @param hash The hashing algorithm to use
   * @param value The value to commit to use
   * @param rand The randomness to use
   */
  public SpdzCommitment(MessageDigest hash, BigInteger value, Random rand) {
    this.value = value;
    this.rand = rand;
    this.hash = hash;
  }

  /**
   * Compute a commitment. 
   * @param modulus The modulus to use
   * @return If a commitment has already been computed, the existing commitment is returned.
   */
  public BigInteger computeCommitment(BigInteger modulus) {
    if (this.commitment != null) {
      return this.commitment;
    }
    hash.update(value.toByteArray());
    this.randomness = new BigInteger(modulus.bitLength(), rand);
    hash.update(this.randomness.toByteArray());
    this.commitment = new BigInteger(hash.digest()).mod(modulus);
    return this.commitment;
  }

  public BigInteger getValue() {
    return this.value;
  }

  public BigInteger getRandomness() {
    return this.randomness;
  }

  @Override
  public String toString() {
    return "SpdzCommitment[v:" + this.value + ", r:" + this.randomness + ", commitment:"
        + this.commitment + "]";
  }
}
