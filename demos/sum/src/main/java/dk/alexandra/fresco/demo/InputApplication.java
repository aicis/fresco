package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
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
 */
public class InputApplication implements Application<List<SInt>, ProtocolBuilderNumeric> {

  private final List<Integer> inputs;

  public InputApplication(List<Integer> inputs) {
    this.inputs = inputs;
  }

  @Override
  public DRes<List<SInt>> buildComputation(ProtocolBuilderNumeric producer) {
    return 
    producer.par(par -> {
      Numeric numeric = par.numeric();
      List<DRes<SInt>> result = new ArrayList<>();
      for (int i = 0; i < inputs.size(); i++) {
        // create wires
        if (inputs.get(i) != null) {
          // My input
          result.add(numeric.input(BigInteger.valueOf(inputs.get(i)), par.getBasicNumericContext().getMyId()));
        } else {
          // Other party's input. I provide a null value
          result.add(numeric.input(null, 3 - par.getBasicNumericContext().getMyId()));
        }
      }
      return () -> result.stream().map(DRes::out).collect(Collectors.toList());
    });
  }
}
