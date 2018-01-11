package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.util.MathUtils;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation which should only be used if for some reason preprocessing is not
 * possible.
 */
public class DefaultPreprocessedValues implements PreprocessedValues {

  private ProtocolBuilderNumeric builder;

  public DefaultPreprocessedValues(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public DRes<List<DRes<SInt>>> getExponentiationPipe(int pipeLength) {
    return builder.seq(b -> {
      DRes<SInt> r = b.numeric().randomElement();
      DRes<SInt> inverse = b.advancedNumeric().invert(r);
      ArrayList<DRes<SInt>> list = new ArrayList<>();
      list.add(inverse);
      list.add(r);
      return () -> new IterationState(2, list);
    }).whileLoop(
        (state) -> state.round <= pipeLength + 1,
        (seq, state) -> {
          DRes<SInt> r = state.value.get(1);
          DRes<SInt> last = state.value.get(state.value.size() - 1);
          List<DRes<SInt>> values = new ArrayList<>(state.value);
          values.add(seq.numeric().mult(last, r));
          return () -> new IterationState(state.round + 1, values);
        }).seq((seq, state) -> () -> state.value);
  }

  @Override
  public DRes<SInt> getNextBit() {
    return builder.seq(b -> {
      DRes<SInt> r = b.numeric().randomElement();
      DRes<SInt> square = b.numeric().mult(r, r);
      DRes<BigInteger> opened = b.numeric().open(square);
      return () -> new Pair<>(r, opened);
    }).seq((seq, pair) -> {
      Numeric numeric = seq.numeric();
      BigInteger modulus = seq.getBasicNumericContext().getModulus();
      DRes<SInt> r = pair.getFirst();
      BigInteger openedSquare = pair.getSecond().out();
      BigInteger root = MathUtils.modularSqrt(openedSquare, modulus);
      BigInteger inverted = root.modInverse(modulus);
      DRes<SInt> divided = numeric.mult(inverted, r);
      BigInteger twoInverse = BigInteger.valueOf(2).modInverse(modulus);
      return numeric.mult(twoInverse, numeric.add(BigInteger.ONE, divided));
    });
  }

  private static final class IterationState {

    private final int round;
    private final List<DRes<SInt>> value;

    private IterationState(int round, List<DRes<SInt>> value) {
      this.round = round;
      this.value = value;
    }
  }
}
