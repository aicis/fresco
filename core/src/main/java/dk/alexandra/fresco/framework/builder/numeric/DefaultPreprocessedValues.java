package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.BuildStep;
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
    if (pipeLength < 0) {
      throw new IllegalArgumentException(
          "Can not create an exponentiation pipe of length less than 0");
    }
    BuildStep<Void, ProtocolBuilderNumeric, List<DRes<SInt>>> firstStep =
        builder.seq(b -> {
          DRes<SInt> r = b.numeric().randomElement();
          DRes<SInt> inverse = b.advancedNumeric().invert(r);
          List<DRes<SInt>> values = new ArrayList<>(pipeLength + 2);
          values.add(inverse);
          values.add(r);
          return () -> values;
        });
    if (pipeLength == 0) {
      return firstStep;
    } else if (pipeLength == 1) {
      return firstStep.seq((b, values) -> {
        DRes<SInt> r = values.get(values.size() - 1);
        values.add(b.numeric().mult(r, r));
        return () -> values;
      });
    } else {
      return firstStep.whileLoop((values) -> values.size() < pipeLength + 2, (seq, values) -> {
        return seq.par(par -> {
          DRes<SInt> last = values.get(values.size() - 1);
          int limit = pipeLength + 2 - values.size();
          List<DRes<SInt>> newValues = values.stream().skip(1).limit(limit)
              .map(v -> par.numeric().mult(last, v)).collect(Collectors.toList());
          values.addAll(newValues);
          return () -> values;
        });
      });
    }
  }
}
