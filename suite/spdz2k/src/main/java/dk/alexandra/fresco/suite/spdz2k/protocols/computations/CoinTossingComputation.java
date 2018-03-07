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

  public CoinTossingComputation(byte[] ownSeed, ByteSerializer<HashBasedCommitment> serializer) {
    this.serializer = serializer;
    this.ownSeed = ownSeed;
  }

  public CoinTossingComputation(int seedLength, ByteSerializer<HashBasedCommitment> serializer) {
    this(generateSeed(seedLength), serializer);
  }

  @Override
  public DRes<byte[]> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par(new Spdz2kCommitmentComputation(serializer, ownSeed))
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
