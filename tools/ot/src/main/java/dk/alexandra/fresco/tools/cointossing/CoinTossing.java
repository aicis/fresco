package dk.alexandra.fresco.tools.cointossing;

import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.commitment.Commitment;
import dk.alexandra.fresco.tools.commitment.FailedCommitmentException;
import dk.alexandra.fresco.tools.commitment.MaliciousCommitmentException;

/**
 * Class implementing two-party coin-tossing. That is, agreement on a random
 * string of bits between two parties even if one of them is acting maliciously.
 * 
 * The protocol uses the standard approach both parties picking a personal seed.
 * One party commits to it. The other party then sends its seed to the first
 * party. The first party opens it commitment. The parties computes the XOR of
 * the seeds and uses this as input to a PRG generating an arbitrary long,
 * common string.
 * 
 * @author jot2re
 *
 */
public class CoinTossing {
  private int otherId;
  private int myId;
  private int kbitLength;
  private Random rand;
  private Network network;
  private boolean initialized = false;
  private SecureRandom prg;

  /**
   * Constructs a coin-tossing protocol between two parties.
   * 
   * @param myId
   *          The unique ID of the calling party
   * @param otherId
   *          The unique ID of the other party (not the calling party)
   *          participating in the protocol
   * @param kbitLength
   *          The computational security parameter
   * @param rand
   *          Object used for randomness generation
   * @param network
   *          The network instance
   */
  public CoinTossing(int myId, int otherId, int kbitLength, Random rand,
      Network network) {
    if (kbitLength < 1 || rand == null || network == null) {
      throw new IllegalArgumentException("Illegal constructor parameters");
    }
    if (kbitLength % 8 != 0) {
      throw new IllegalArgumentException(
          "Computational security parameter must be divisible by 8");
    }
    this.myId = myId;
    this.otherId = otherId;
    this.kbitLength = kbitLength;
    this.rand = rand;
    this.network = network;
  }

  /**
   * Initialize the coin-tossing functionality by making the parties agree on a
   * seed.
   * 
   * @throws FailedCoinTossingException
   *           An internal, non-malicious, error occurred.
   * @throws MaliciousCommitmentException
   *           The other party acted maliciously in the underlying commitment
   *           protocol.
   * @throws FailedCommitmentException
   *           An internal, non-malicious, error occurred in the underlying
   *           commitment protocol.
   */
  public void initialize()
      throws MaliciousCommitmentException, FailedCommitmentException,
      FailedCoinTossingException {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    try {
      byte[] seed = new byte[(kbitLength + 8 - 1) / 8];
      rand.nextBytes(seed);
      byte[] otherSeed = exchangeSeeds(seed);
      ByteArrayHelper.xor(seed, otherSeed);
      // TODO should be changed to something that uses SHA-256
      this.prg = SecureRandom.getInstance("SHA1PRNG");
      prg.setSeed(seed);
      initialized = true;
    } catch (IOException e) {
      throw new FailedCoinTossingException(
          "Coin-tossing failed. No malicious behaviour detected. "
              + "Failure was caused by the following communication error: "
              + e.getMessage());
    } catch (NoSuchAlgorithmException | ClassNotFoundException e) {
      throw new FailedCoinTossingException(
          "Coin-tossing failed. No malicious behaviour detected. "
              + "Failure was caused by the following internal error: "
              + e.getMessage());
    }
  }

  /**
   * Constructs a common random string of "size" bits, rounded up to the nearest
   * factor of 8.
   * 
   * @param size
   *          The amount of random bits needed
   * @return The byte array consisting of uniformly random sampled bytes.
   */
  public StrictBitVector toss(int size) {
    if (size < 1) {
      throw new IllegalArgumentException("At least one coin must be tossed.");
    }
    if (!initialized) {
      throw new IllegalStateException("Not initialized");
    }
    byte[] res = new byte[(size + 8 - 1) / 8];
    prg.nextBytes(res);
    return new StrictBitVector(res, size);
  }

  /**
   * Exchange the seed with the other party using a commitment protocol.
   * 
   * @param seed
   *          The current party's seed
   * @return The other party's seed
   * @throws MaliciousCommitmentException
   *           The other party acted maliciously in the underlying commitment
   *           protocol.
   * @throws IOException
   *           An internal, non-malicious, error occurred.
   * @throws NoSuchAlgorithmException
   *           An internal, non-malicious, error occurred.
   * @throws ClassNotFoundException
   *           An internal, non-malicious, error occurred.
   * @throws FailedCommitmentException
   *           An internal, non-malicious, error occurred in the underlying
   *           commitment protocol.
   */
  private byte[] exchangeSeeds(byte[] seed)
      throws MaliciousCommitmentException, IOException,
      NoSuchAlgorithmException, ClassNotFoundException,
      FailedCommitmentException {
    // Let the party with the smallest id be the party receiving a commitment
    if (myId < otherId) {
      Commitment comm = Commitment.receiveCommitment(otherId, network);
      network.send(otherId, seed);
      byte[] opening = network.receive(otherId);
      return (byte[]) comm.open(ByteArrayHelper.deserialize(opening));
    } else {
      Commitment comm = new Commitment(kbitLength);
      Serializable openInfo = comm.commit(rand, seed);
      Commitment.sendCommitment(comm, otherId, network);
      byte[] otherSeed = network.receive(otherId);
      network.send(otherId, ByteArrayHelper.serialize(openInfo));
      return otherSeed;
    }
  }
}