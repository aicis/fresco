package dk.alexandra.fresco.lib.generic;

import dk.alexandra.fresco.commitment.HashBasedCommitment;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.Drbg;
import java.security.SecureRandom;

/**
 * Coin-tossing functionality.
 */
public class CoinTossingComputation implements Computation<byte[], ProtocolBuilderNumeric> {

  private final ByteSerializer<HashBasedCommitment> serializer;
  private final byte[] ownSeed;
  private final Drbg localDrbg;


  public CoinTossingComputation(byte[] ownSeed, ByteSerializer<HashBasedCommitment> serializer,
      Drbg localDrbg) {
    this.serializer = serializer;
    this.ownSeed = ownSeed;
    this.localDrbg = localDrbg;
  }

  public CoinTossingComputation(int seedLength, ByteSerializer<HashBasedCommitment> serializer,
      Drbg localDrbg) {
    this(generateSeed(seedLength), serializer, localDrbg);
  }

  @Override
  public DRes<byte[]> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(new CommitmentComputation(serializer, ownSeed, localDrbg))
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
