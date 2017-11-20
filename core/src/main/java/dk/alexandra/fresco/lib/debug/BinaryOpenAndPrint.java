package dk.alexandra.fresco.lib.debug;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <b>NB: Use with caution as using this class will open values to all MPC parties.</b>
 * 
 * This class opens a binary number for debugging purposes and prints a message along with the
 * revealed values.
 * 
 */
public class BinaryOpenAndPrint implements
    dk.alexandra.fresco.framework.builder.Computation<Void, ProtocolBuilderBinary> {

  private final List<DRes<SBool>> string;
  private final PrintStream output;
  private final String label;

  public BinaryOpenAndPrint(String label, List<DRes<SBool>> string, PrintStream output) {
    this.string = string;

    this.label = label;
    this.output = output;
  }

  @Override
  public DRes<Void> buildComputation(ProtocolBuilderBinary builder) {
    return builder.seq(seq -> {
      List<SBool> unfolded =
          this.string.stream().map(DRes::out).collect(Collectors.toList());
      List<DRes<Boolean>> bools = new ArrayList<>();
      for (SBool b : unfolded) {
        bools.add(seq.binary().open(b));
      }
      return () -> bools;
    }).seq((seq, res) -> {
      StringBuilder sb = new StringBuilder();
      sb.append(label);
      sb.append('\n');
      for (DRes<Boolean> entry : res) {
        if (entry.out()) {
          sb.append(1);
        } else {
          sb.append(0);
        }
      }
      seq.debug().marker(sb.toString(), output);
      return null;
    });
  }
}
