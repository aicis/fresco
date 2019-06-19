package dk.alexandra.fresco.suite.spdz.maccheck;

import dk.alexandra.fresco.commitment.HashBasedCommitmentSerializer;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.lib.generic.CoinTossingComputation;
import dk.alexandra.fresco.lib.generic.CommitmentComputation;
import dk.alexandra.fresco.lib.generic.MaliciousCommitmentComputation;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;
import java.util.List;

public class MaliciousSpdzMacCheckComputation implements Computation<Void, ProtocolBuilderNumeric> {

  private final BigInteger modulus;
  private final List<SpdzSInt> closedValues;
  private final List<FieldElement> openedValues;
  private final FieldElement alpha;
  private Drbg jointDrbg;

  public static boolean corruptCommitRound = false;
  public static boolean corruptOpenCommitRound = false;

  MaliciousSpdzMacCheckComputation(
      final Pair<List<SpdzSInt>, List<FieldElement>> toCheck,
      final BigInteger modulus,
      final Drbg jointDrbg,
      final FieldElement alpha) {
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
        localDrbg))
        .seq((seq, seed) -> {
          this.jointDrbg = new AesCtrDrbg(seed);
          FieldDefinition fieldDefinition = builder.getBasicNumericContext().getFieldDefinition();
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
          if (corruptCommitRound) {
            // tamper with delta
            delta = delta.add(fieldDefinition.createElement(1));
          }

          if (corruptOpenCommitRound) {
            // tamper with opening
            return seq.seq(
                new MaliciousCommitmentComputation(commitmentSerializer,
                    fieldDefinition.serialize(delta),
                    noOfParties, localDrbg));
          } else {
            return seq.seq(
                new CommitmentComputation(commitmentSerializer,
                    fieldDefinition.serialize(delta),
                    localDrbg));
          }
        }).seq((seq, ignored) -> null);
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
