package dk.alexandra.fresco.lib.compare.zerotest;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZeroTestBruteforce implements Computation<SInt, ProtocolBuilderNumeric> {

  private final int maxLength;
  private final DRes<SInt> input;

  public ZeroTestBruteforce(int maxLength,
      DRes<SInt> input) {
    this.maxLength = maxLength;
    this.input = input;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    BigInteger one = BigInteger.ONE;
    return builder.seq((seq) ->
        seq.numeric().getExponentiationPipe()
    ).seq((seq, expPipe) -> {
      //Add one, mult and unmask
      Numeric numeric = seq.numeric();
      DRes<SInt> increased = numeric.add(one, input);
      DRes<SInt> maskedS = numeric.mult(increased, () -> expPipe[0]);
      DRes<BigInteger> open = seq.numeric().open(maskedS);
      return () -> new Pair<>(expPipe, open.out());
    }).seq((seq, pair) -> {
      // compute powers and evaluate polynomial
      SInt[] R = pair.getFirst();
      BigInteger maskedO = pair.getSecond();
      BigInteger[] maskedPowers = seq.getBigIntegerHelper().getExpFromOInt(maskedO, maxLength);
      return () -> new Pair<>(R, maskedPowers);
    }).par((par, pair) -> {
      SInt[] R = pair.getFirst();
      BigInteger[] maskedPowers = pair.getSecond();
      List<DRes<SInt>> powers = new ArrayList<>(maxLength);
      Numeric numeric = par.numeric();
      for (int i = 0; i < maxLength; i++) {
        SInt rpartPair = R[i + 1];
        powers.add(numeric.mult(maskedPowers[i], () -> rpartPair));
      }
      return () -> powers;
    }).seq((seq, powers) -> {
      BigInteger[] polynomialCoefficients = seq.getBigIntegerHelper()
          .getPoly(maxLength);
      BigInteger[] mostSignificantPolynomialCoefficients = new BigInteger[maxLength];
      System.arraycopy(polynomialCoefficients, 1,
          mostSignificantPolynomialCoefficients, 0, maxLength);
      DRes<SInt> tmp = seq.advancedNumeric()
          .openDot(Arrays.asList(mostSignificantPolynomialCoefficients), powers);
      return seq.numeric().add(polynomialCoefficients[0], tmp);
    });
  }
}
