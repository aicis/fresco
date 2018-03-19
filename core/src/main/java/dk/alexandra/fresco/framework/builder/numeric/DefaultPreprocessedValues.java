package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
      ArrayList<DRes<SInt>> list = new ArrayList<>(pipeLength + 1);
      list.add(inverse);
      list.add(r);
      return () -> new IterationState(2, list);
    }).whileLoop((state) -> state.round <= pipeLength + 1, (seq, state) -> {
      DRes<SInt> last = state.value.get(state.round - 1);
      List<DRes<SInt>> newValues = state.value.stream()
          .limit(pipeLength / 2 + 1)
          .skip(1)
          .map(v -> seq.numeric().mult(last, v))
          .collect(Collectors.toList());
      state.value.addAll(newValues);
      return () ->
      new IterationState(2 * state.round - 1, state.value);
    }).seq((seq, state) -> () -> state.value);
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
