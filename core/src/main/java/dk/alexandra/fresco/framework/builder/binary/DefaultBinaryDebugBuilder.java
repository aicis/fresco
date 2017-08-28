package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.debug.BinaryMarkerProtocolImpl;
import dk.alexandra.fresco.lib.debug.BinaryOpenAndPrint;
import java.io.PrintStream;
import java.util.List;

public class DefaultBinaryDebugBuilder implements BinaryDebugBuilder {

  private ProtocolBuilderBinary builder;

  protected DefaultBinaryDebugBuilder(ProtocolBuilderBinary builder) {
    this.builder = builder;
  }

  @Override
  public void openAndPrint(String label, List<Computation<SBool>> toPrint) {
    builder.seq(new BinaryOpenAndPrint(label, toPrint, System.out));
  }

  @Override
  public void openAndPrint(String label, List<Computation<SBool>> toPrint, PrintStream stream) {
    builder.seq(new BinaryOpenAndPrint(label, toPrint, stream));
  }

  @Override
  public void marker(String message) {
    builder.seq(new BinaryMarkerProtocolImpl(message, System.out));
  }

  @Override
  public void marker(String message, PrintStream stream) {
    builder.seq(new BinaryMarkerProtocolImpl(message, stream));
  }



}
