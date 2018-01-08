package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.helpers.DemoNumericApplication;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * Tiny application for a two party case which computes the sum of the inputs, and outputs the
 * result.
 *
 * @author kasperdamgard
 */
public class SumAndOutputApplication extends DemoNumericApplication<BigInteger> {


  private InputApplication inputApp;

  public SumAndOutputApplication(InputApplication inputApp) {
    this.inputApp = inputApp;
  }

  @Override
  public DRes<BigInteger> buildComputation(ProtocolBuilderNumeric producer) {
    return producer.seq(inputApp).seq((seq, inputs) -> {
      DRes<SInt> sum = null;
      for (SInt input : inputs) {
        if (sum == null) {
          sum = input;
        } else {
          sum = seq.numeric().add(sum, input);
        }
      }
      return seq.numeric().open(sum);
    });
  }
}
