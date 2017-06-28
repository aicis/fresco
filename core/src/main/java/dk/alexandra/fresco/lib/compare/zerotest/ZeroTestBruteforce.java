package dk.alexandra.fresco.lib.compare.zerotest;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZeroTestBruteforce implements ComputationBuilder<SInt> {

  private final BuilderFactoryNumeric factoryNumeric;
  private final int maxLength;
  private final Computation<SInt> input;

  public ZeroTestBruteforce(BuilderFactoryNumeric factoryNumeric, int maxLength,
      Computation<SInt> input) {
    this.factoryNumeric = factoryNumeric;
    this.maxLength = maxLength;
    this.input = input;
  }

  @Override
  public Computation<SInt> build(SequentialProtocolBuilder builder) {
    BigInteger one = BigInteger.ONE;
    return builder.seq((seq) ->
        seq.numeric().getExponentiationPipe()
    ).seq((expPipe, seq) -> {
      //Add one, mult and unmask
      NumericBuilder numeric = seq.numeric();
      Computation<SInt> increased = numeric.add(one, input);
      Computation<SInt> maskedS = numeric.mult(increased, expPipe[0]);
      Computation<BigInteger> open = seq.numeric().open(maskedS);
      return () -> new Pair<>(expPipe, open.out());
    }).seq((pair, seq) -> {
      // compute powers and evaluate polynomial
      SInt[] R = pair.getFirst();
      BigInteger maskedO = pair.getSecond();
      Computation<BigInteger[]> maskedPowers = seq.numeric().getExpFromOInt(maskedO, maxLength);
      return () -> new Pair<>(R, maskedPowers.out());
    }).par((pair, par) -> {
      SInt[] R = pair.getFirst();
      BigInteger[] maskedPowers = pair.getSecond();
      List<Computation<SInt>> powers = new ArrayList<>(maxLength);
      NumericBuilder numeric = par.numeric();
      for (int i = 0; i < maxLength; i++) {
        powers.add(numeric.mult(maskedPowers[i], R[i + 1]));
      }
      return () -> powers;
    }).seq((powers, seq) -> {
      BigInteger modulus = seq.getBasicNumericFactory().getModulus();
      //TODO Get MichOIntGenerators through the factory to allow caching it for the entire application
      BigInteger[] polynomialCoefficients = new MiscOIntGenerators()
          .getPoly(maxLength, modulus);
      BigInteger[] mostSignificantPolynomialCoefficients = new BigInteger[maxLength];
      System.arraycopy(polynomialCoefficients, 1,
          mostSignificantPolynomialCoefficients, 0, maxLength);
      Computation<SInt> tmp = seq.createAdvancedNumericBuilder()
          .openDot(Arrays.asList(mostSignificantPolynomialCoefficients), powers);
      return seq.numeric().add(polynomialCoefficients[0], tmp);
    });
  }
}
