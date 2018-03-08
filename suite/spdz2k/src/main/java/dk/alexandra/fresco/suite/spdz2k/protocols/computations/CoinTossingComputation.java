package dk.alexandra.fresco.suite.spdz2k.protocols.computations;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import java.security.SecureRandom;

/**
 * Coin-tossing functionality.
 */
public class CoinTossingComputation implements Computation<byte[], ProtocolBuilderNumeric> {

  private final ByteSerializer<HashBasedCommitment> serializer;
  private final byte[] ownSeed;
  private final int noOfParties;

  public CoinTossingComputation(byte[] ownSeed, ByteSerializer<HashBasedCommitment> serializer,
      int noOfParties) {
    this.serializer = serializer;
    this.ownSeed = ownSeed;
    this.noOfParties = noOfParties;
  }

  public CoinTossingComputation(int seedLength, ByteSerializer<HashBasedCommitment> serializer,
      int noOfParties) {
    this(generateSeed(seedLength), serializer, noOfParties);
  }

  @Override
  public DRes<byte[]> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(new Spdz2kCommitmentComputation(serializer, ownSeed, noOfParties))
        .seq((seq, seeds) -> {
          byte[] jointSeed = new byte[ownSeed.length];
          for (byte[] seed : seeds) {
            ByteArrayHelper.xor(jointSeed, seed);
          }
          return () -> jointSeed;
        });
  }

  private static byte[] generateSeed(int seedLength) {
    byte[] seed = new byte[seedLength];
    new SecureRandom().nextBytes(seed);
    return seed;
  }

}
