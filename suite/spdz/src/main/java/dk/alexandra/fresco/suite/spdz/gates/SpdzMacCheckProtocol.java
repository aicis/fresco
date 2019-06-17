package dk.alexandra.fresco.suite.spdz.gates;

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
import dk.alexandra.fresco.lib.generic.CommitmentComputation;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;

/**
 * Protocol which handles the MAC check internal to SPDZ. If this protocol reaches the end, no
 * malicious activity was detected and the storage is reset.
 */
public class SpdzMacCheckProtocol implements Computation<Void, ProtocolBuilderNumeric> {

  private final BigInteger modulus;
  private final List<SpdzSInt> closedValues;
  private final List<FieldElement> openedValues;
  private final FieldElement alpha;
  private final Function<byte[], Drbg> jointDrbgSupplier;
  private final int drbgSeedBitLength;

  /**
   * Protocol which handles the MAC check internal to SPDZ. If this protocol reaches the end, no
   * malicious activity was detected and the storage is reset.
   *
   * @param toCheck opened values and corresponding macs to check
   * @param modulus the global modulus used
   * @param jointDrbgSupplier supplier of DRBG to be used for joint randomness
   * @param alpha this party's key share
   * @param drbgSeedBitLength seed length for local DRBG
   */
  public SpdzMacCheckProtocol(
      final Pair<List<SpdzSInt>, List<FieldElement>> toCheck,
      final BigInteger modulus,
      final Function<byte[], Drbg> jointDrbgSupplier,
      final FieldElement alpha,
      final int drbgSeedBitLength) {
    this.closedValues = toCheck.getFirst();
    this.openedValues = toCheck.getSecond();
    this.modulus = modulus;
    this.jointDrbgSupplier = jointDrbgSupplier;
    this.alpha = alpha;
    this.drbgSeedBitLength = drbgSeedBitLength;
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    final AesCtrDrbg localDrbg = new AesCtrDrbg();
    final HashBasedCommitmentSerializer commitmentSerializer = new HashBasedCommitmentSerializer();

    return builder.seq(new CoinTossingComputation(drbgSeedBitLength / 8,
        commitmentSerializer,
        localDrbg))
        .seq((seq, seed) -> {
          FieldDefinition fieldDefinition = builder.getBasicNumericContext().getFieldDefinition();
          Drbg jointDrbg = jointDrbgSupplier.apply(seed);
          FieldElement[] rs = sampleRandomCoefficients(openedValues.size(), fieldDefinition,
              jointDrbg);
          FieldElement a = fieldDefinition.createElement(0);
          int index = 0;
          for (FieldElement openedValue : openedValues) {
            FieldElement openedValueHidden = openedValue.multiply(rs[index++]);
            a = a.add(openedValueHidden);
          }

          // compute gamma_i as the sum of all MAC's on the opened values times
          // r_j.
          FieldElement gamma = fieldDefinition.createElement(0);
          index = 0;
          for (SpdzSInt closedValue : closedValues) {
            FieldElement closedValueHidden = rs[index++].multiply(closedValue.getMac());
            gamma = gamma.add(closedValueHidden);
          }

          // compute delta_i as: gamma_i - alpha_i*a
          FieldElement delta = gamma.subtract(alpha.multiply(a));
          // Commit to delta and open it afterwards
          return seq.seq(
              new CommitmentComputation(commitmentSerializer, fieldDefinition.serialize(delta),
                  localDrbg));
        }).seq((seq, commitmentsRaw) -> {
          FieldDefinition fieldDefinition = builder.getBasicNumericContext().getFieldDefinition();
          List<FieldElement> commitments = fieldDefinition.deserializeList(commitmentsRaw);
          FieldElement deltaSum =
              commitments
                  .stream()
                  .reduce(fieldDefinition.createElement(0), FieldElement::add);

          if (!BigInteger.ZERO.equals(fieldDefinition.convertToUnsigned(deltaSum))) {
            throw new MaliciousException(
                "The sum of delta's was not 0. Someone was corrupting something amongst "
                    + openedValues.size()
                    + " macs. Sum was " + deltaSum.toString() + " Aborting!");
          }
          // clean up store before returning to evaluating such that we only
          // evaluate the next macs, not those we already checked.
          openedValues.clear();
          closedValues.clear();
          return null;
        });
  }

  private FieldElement[] sampleRandomCoefficients(int numCoefficients,
      FieldDefinition fieldDefinition, Drbg jointDrbg) {
    FieldElement[] coefficients = new FieldElement[numCoefficients];
    for (int i = 0; i < numCoefficients; i++) {
      byte[] bytes = new byte[modulus.bitLength() / Byte.SIZE];
      jointDrbg.nextBytes(bytes);
      coefficients[i] = fieldDefinition.createElement(new BigInteger(bytes));
    }
    return coefficients;
  }
}
