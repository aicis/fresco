package dk.alexandra.fresco.tools.commitment;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.generic.BroadcastComputation;
import dk.alexandra.fresco.lib.generic.InsecureBroadcastProtocol;
import java.util.List;

public class MaliciousCommitmentComputation extends CommitmentComputation {

  private final ByteSerializer<HashBasedCommitment> commitmentSerializer;
  private final byte[] value;
  private final int noOfParties;
  private final Drbg localDrbg;

  public MaliciousCommitmentComputation(
      ByteSerializer<HashBasedCommitment> commitmentSerializer,
      byte[] value, int noOfParties, Drbg localDrbg) {
    super(commitmentSerializer, value, localDrbg);
    this.commitmentSerializer = commitmentSerializer;
    this.value = value;
    this.noOfParties = noOfParties;
    this.localDrbg = localDrbg;
  }

  @Override
  public DRes<List<byte[]>> buildComputation(ProtocolBuilderNumeric builder) {
    HashBasedCommitment ownCommitment = new HashBasedCommitment();
    byte[] ownOpening = ownCommitment.commit(localDrbg, value);
    return builder.seq(
        seq -> {
          if (noOfParties > 2) {
            return new BroadcastComputation<ProtocolBuilderNumeric>(
                commitmentSerializer.serialize(ownCommitment))
                .buildComputation(seq);
          } else {
            // when there are only two parties, parties can't send mutually inconsistent messages
            // so no extra validation is necessary
            return seq.append(new InsecureBroadcastProtocol<>(
                commitmentSerializer.serialize(ownCommitment)));
          }
        })
        .seq((seq, rawCommitments) -> {
          // tamper with opening
          ownOpening[1] = (byte) (ownOpening[1] ^ 1);
          DRes<List<byte[]>> res = seq.append(new InsecureBroadcastProtocol<>(ownOpening));
          final Pair<DRes<List<byte[]>>, List<byte[]>> dResListPair = new Pair<>(res,
              rawCommitments);
          return () -> dResListPair;
        })
        .seq((seq, pair) -> null);
  }
}
