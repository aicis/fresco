package dk.alexandra.fresco.tools.cointossing;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.commitment.HashBasedCommitmentSerializer;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;

/**
 * Class implementing two-party coin-tossing. That is, agreement on a random
 * string of bits between two parties even if one of them is acting maliciously.
 * <br/>
 * The protocol uses the standard approach both parties picking a personal seed.
 * One party commits to it. The other party then sends its seed to the first
 * party. The first party opens it commitment. The parties computes the XOR of
 * the seeds and uses this as input to a PRG generating an arbitrary long,
 * common string.
 */
public class CoinTossing {
  private final int otherId;
  private final int myId;
  private final Drbg rand;
  private final ByteSerializer<HashBasedCommitment> serializer;
  private boolean initialized = false;
  private Drbg coinTossingPrg;

  /**
   * Constructs a coin-tossing protocol between two parties.
   *
   * @param myId
   *          The unique ID of the calling party
   * @param otherId
   *          The unique ID of the other party (not the calling party)
   *          participating in the protocol
   * @param rand
   *          Object used for randomness generation
   */
  public CoinTossing(int myId, int otherId, Drbg rand) {
    this.myId = myId;
    this.otherId = otherId;
    this.rand = rand;
    this.serializer = new HashBasedCommitmentSerializer();
  }

  /**
   * Initialize the coin-tossing functionality by making the parties agree on a seed.
   *
   * @param network
   *          The network instance
   */
  public void initialize(Network network) {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    // Make space for a seed by allocating as many bytes as needed, which is
    // exactly 32 bytes for AesCtrDrbg
    byte[] seed = new byte[32];
    rand.nextBytes(seed);
    byte[] otherSeed = exchangeSeeds(seed, network);
    ByteArrayHelper.xor(seed, otherSeed);
    this.coinTossingPrg = new AesCtrDrbg(seed);
    initialized = true;
  }

  /**
   * Constructs a common random string of {@code size} bits, rounded up to the
   * nearest factor of 8.
   *
   * @param size
   *          The amount of random bits needed. Must be at least 1.
   * @return The byte array consisting of uniformly random sampled bytes.
   */
  public StrictBitVector toss(int size) {
    if (!initialized) {
      throw new IllegalStateException("Not initialized");
    }
    // Construct byte array with enough space by rounding up the amount of bytes
    // required to store the tosses
    byte[] res = new byte[(size + 8 - 1) / 8];
    coinTossingPrg.nextBytes(res);
    return new StrictBitVector(res);
  }

  /**
   * Exchange the seed with the other party using a commitment protocol.
   *
   * @param seed
   *          The current party's seed
   * @param network
   *          The network instance
   * @return The other party's seed
   */
  private byte[] exchangeSeeds(byte[] seed, Network network) {
    // Let the party with the smallest id be the party receiving a commitment
    if (myId < otherId) {
      byte[] serializedComm = network.receive(otherId);
      HashBasedCommitment comm = serializer.deserialize(serializedComm);
      network.send(otherId, seed);
      byte[] opening = network.receive(otherId);
      return comm.open(opening);
    } else {
      HashBasedCommitment comm = new HashBasedCommitment();
      byte[] openInfo = comm.commit(rand, seed);
      network.send(otherId, serializer.serialize(comm));
      byte[] otherSeed = network.receive(otherId);
      network.send(otherId, openInfo);
      return otherSeed;
    }
  }
}
