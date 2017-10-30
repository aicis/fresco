package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.helpers.DemoNumericApplication;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.BuildStep;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Demo application. Takes a number of inputs and converts them to secret shared inputs by having
 * party 1 input them all.
 *
 * @author Kasper Damgaard
 */
public class InputApplication extends DemoNumericApplication<List<SInt>> {

  private int[] inputs;
  private int length;

  public InputApplication(int[] inputs) {
    this.inputs = inputs;
    this.length = inputs.length;
  }

  public InputApplication(int length) {
    this.length = length;
  }

  @Override
  public DRes<List<SInt>> buildComputation(ProtocolBuilderNumeric producer) {
    return createBuildStep(producer);
  }

  public BuildStep<?, ProtocolBuilderNumeric, List<SInt>> createBuildStep(
      ProtocolBuilderNumeric producer) {
    return producer.par(par -> {
      Numeric numeric = par.numeric();
      List<DRes<SInt>> result = new ArrayList<>(length);
      for (int i = 0; i < this.length; i++) {
        // create wires
        if (this.inputs != null) {
          result.add(numeric.input(BigInteger.valueOf(this.inputs[i]), 1));
        } else {
          result.add(numeric.input(null, 1));
        }
      }
      return () -> result.stream().map(DRes::out).collect(Collectors.toList());
    });
  }
}
