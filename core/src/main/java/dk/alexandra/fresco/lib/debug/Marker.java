package dk.alexandra.fresco.lib.debug;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import java.io.PrintStream;

/**
 * When evaluated, prints out the message from the constructor.
 *
 */
public class Marker implements Computation<Void, ProtocolBuilderNumeric> {

  private final String message;
  private final PrintStream output;


  public Marker(String message, PrintStream output) {
    this.message = message;
    if (output != null) {
      this.output = output;
    } else {
      this.output = System.out;
    }
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.seq(seq -> {
      output.println(message);
      return () -> null;
    });
  }
}
