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
      return () -> new IterationState(list);
    }).whileLoop((state) -> state.values.size() <= pipeLength + 1, (seq, state) -> {
      return seq.par(par -> {
        DRes<SInt> last = state.values.get(state.values.size() - 1);
        int limit = pipeLength + 2 - state.values.size();
        List<DRes<SInt>> newValues = state.values.stream().skip(1).limit(limit)
            .map(v -> par.numeric().mult(last, v)).collect(Collectors.toList());
        state.values.addAll(newValues);
        return () -> new IterationState(state.values);
      });

    }).seq((seq, state) -> () -> state.values);
  }

  private static final class IterationState {

    private final List<DRes<SInt>> values;

    private IterationState(List<DRes<SInt>> values) {
      this.values = values;
    }
  }
}
