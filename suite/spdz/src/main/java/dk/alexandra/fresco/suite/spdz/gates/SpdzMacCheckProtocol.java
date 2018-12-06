package dk.alexandra.fresco.suite.spdz.gates;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.BigInt;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzCommitment;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;

/**
 * Protocol which handles the MAC check internal to SPDZ. If this protocol reaches the end, no
 * malicious activity was detected and the storage is reset.
 */
public class SpdzMacCheckProtocol implements Computation<Void, ProtocolBuilderNumeric> {

  private final SecureRandom rand;
  private final MessageDigest digest;
  private final BigInteger modulus;
  private final Drbg jointDrbg;
  private final List<SpdzSInt> closedValues;
  private final List<FieldElement> openedValues;
  private final FieldElement alpha;

  /**
   * Protocol which handles the MAC check internal to SPDZ. If this protocol reaches the end, no
   * malicious activity was detected and the storage is reset.
   *
   * @param rand A secure randomness source
   * @param digest A secure hash used for the commitment scheme
   * @param modulus The global modulus used.
   */
  public SpdzMacCheckProtocol(
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
    return builder
        .seq(seq -> {
          BigInteger[] rs = sampleRandomCoefficients(openedValues.size(), jointDrbg, modulus);
          BigInteger a = BigInteger.ZERO;
          int index = 0;
          for (FieldElement openedValue : openedValues) {
            a = a.add(openedValue.asBigInteger().multiply(rs[index++])).mod(modulus);
          }

          // compute gamma_i as the sum of all MAC's on the opened values times
          // r_j.
          BigInteger gamma = BigInteger.ZERO;
          index = 0;
          for (SpdzSInt closedValue : closedValues) {
            gamma = gamma.add(rs[index++].multiply(closedValue.getMac().asBigInteger())).mod(modulus);
          }

          // compute delta_i as: gamma_i - alpha_i*a
          BigInteger delta = gamma.subtract(alpha.asBigInteger().multiply(a)).mod(modulus);
          // Commit to delta and open it afterwards
          // TODO This should not be loaded directly here.
          SpdzCommitment deltaCommitment = new SpdzCommitment(digest,
              BigInt.fromConstant(delta, modulus), rand);
          return seq.seq((subSeq) -> subSeq.append(new SpdzCommitProtocol(deltaCommitment)))
              .seq((subSeq, commitProtocol) ->
                  subSeq.append(new SpdzOpenCommitProtocol(deltaCommitment, commitProtocol)));
        }).seq((seq, commitments) -> {
          BigInteger deltaSum =
              commitments.values()
                  .stream()
                  .map(FieldElement::asBigInteger)
                  .reduce(BigInteger.ZERO, BigInteger::add)
                  .mod(modulus);

          if (!deltaSum.equals(BigInteger.ZERO)) {
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

  private BigInteger[] sampleRandomCoefficients(int numCoefficients, Drbg jointDrbg,
      BigInteger modulus) {
    BigInteger[] coefficients = new BigInteger[numCoefficients];
    for (int i = 0; i < numCoefficients; i++) {
      byte[] bytes = new byte[modulus.bitLength() / Byte.SIZE];
      jointDrbg.nextBytes(bytes);
      coefficients[i] = new BigInteger(bytes).mod(modulus);
    }
    return coefficients;
  }
}
