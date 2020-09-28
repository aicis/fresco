package dk.alexandra.fresco.lib.debug;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import java.io.PrintStream;

/**
 * When evaluated, prints out the message from the constructor.
 *
 */
public class BinaryMarker implements Computation<Void, ProtocolBuilderBinary> {

  private final String message;
  private final PrintStream output;


  public BinaryMarker(String message, PrintStream output) {
    this.message = message;
    this.output = output;
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderBinary builder) {
    return builder.seq(seq -> {
      output.println(message);
      return null;
    });
  }
}
