package dk.alexandra.fresco.suite.spdz.maccheck;

import dk.alexandra.fresco.commitment.HashBasedCommitmentSerializer;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.generic.CoinTossingComputation;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzCommitProtocol;
import dk.alexandra.fresco.suite.spdz.gates.SpdzOpenCommitProtocol;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaliciousSpdzMacCheckComputation implements Computation<Void, ProtocolBuilderNumeric> {

  private final SecureRandom rand;
  private final MessageDigest digest;
  private final BigInteger modulus;
  private final List<SpdzSInt> closedValues;
  private MaliciousSpdzCommitProtocol comm;
  private MaliciousSpdzOpenCommitProtocol openComm;
  private Map<Integer, FieldElement> commitments;
  private final List<FieldElement> openedValues;
  private final FieldElement alpha;
  private Drbg jointDrbg;

  public static boolean corruptCommitRound = false;
  public static boolean corruptOpenCommitRound = false;

  public MaliciousSpdzMacCheckComputation(
      final SecureRandom rand,
      final MessageDigest digest,
      final Pair<List<SpdzSInt>, List<FieldElement>> toCheck,
      final BigInteger modulus,
      final Drbg jointDrbg,
      final FieldElement alpha) {
    this.rand = rand;
    this.digest = digest;
    this.closedValues = toCheck.getFirst();
    this.openedValues = toCheck.getSecond();
    this.modulus = modulus;
    this.jointDrbg = jointDrbg;
    this.alpha = alpha;
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    final int noOfParties = builder.getBasicNumericContext().getNoOfParties();
    final AesCtrDrbg localDrbg = new AesCtrDrbg();
    final HashBasedCommitmentSerializer commitmentSerializer = new HashBasedCommitmentSerializer();
    final FieldDefinition definition = builder.getBasicNumericContext().getFieldDefinition();

    return builder.seq(new CoinTossingComputation(32,
        commitmentSerializer,
        noOfParties,
        localDrbg))
        .seq((seq, seed) -> {
          this.jointDrbg = new AesCtrDrbg(seed);
          FieldElement[] rs = sampleRandomCoefficients(openedValues.size(), definition);
          FieldElement a = definition.createElement(0);
          int index = 0;
          for (FieldElement openedValue : openedValues) {
            FieldElement openedValueHidden = openedValue.multiply(rs[index++]);
            a = a.add(openedValueHidden);
          }

          // compute gamma_i as the sum of all MAC's on the opened values times
          // r_j.
          FieldElement gamma = definition.createElement(0);
          index = 0;
          for (SpdzSInt closedValue : closedValues) {
            FieldElement closedValueHidden = rs[index++].multiply(closedValue.getMac());
            gamma = gamma.add(closedValueHidden);
          }

          // compute delta_i as: gamma_i - alpha_i*a
          FieldElement delta = gamma.subtract(alpha.multiply(a));
          // Commit to delta and open it afterwards

          SpdzCommitment deltaCommitment = new SpdzCommitment(digest, delta, rand,
              modulus.bitLength());
          commitments = new HashMap<>();
          Map<Integer, byte[]> comms = new HashMap<>();
          comm = new MaliciousSpdzCommitProtocol(deltaCommitment, comms,
              corruptCommitRound);
          return seq.seq((subSeq) -> subSeq.append(comm))
              .seq((subSeq, commitProtocol) ->
                  subSeq.append(
                      new MaliciousSpdzOpenCommitProtocol(deltaCommitment, comms, commitments,
                          corruptOpenCommitRound)));
        }).seq((seq, openComm) -> {
          if (!comm.out()) {
            throw new MaliciousException(
                "Malicious activity detected: Broadcast of commitments was not validated.");
          }
          if (!openComm) {
            throw new MaliciousException(
                "Malicious activity detected: Opening commitments failed.");
          }
          FieldElement deltaSum =
              commitments.values()
                  .stream()
                  .reduce(definition.createElement(0), FieldElement::add);

          if (!BigInteger.ZERO.equals(definition.convertToUnsigned(deltaSum))) {
            throw new MaliciousException(
                "The sum of delta's was not 0. Someone was corrupting something amongst "
                    + openedValues.size()
                    + " macs. Sum was " + deltaSum.toString() + " Aborting!");
          }
          // clean up store before returning to evaluating such that we only evaluate the next macs,
          // not those we already checked.
          openedValues.clear();
          closedValues.clear();
          return null;
        });
  }

  private FieldElement[] sampleRandomCoefficients(int numCoefficients,
      FieldDefinition fieldDefinition) {
    FieldElement[] coefficients = new FieldElement[numCoefficients];
    for (int i = 0; i < numCoefficients; i++) {
      byte[] bytes = new byte[modulus.bitLength() / Byte.SIZE];
      jointDrbg.nextBytes(bytes);
      coefficients[i] = fieldDefinition.createElement(new BigInteger(bytes));
    }
    return coefficients;
  }
}
