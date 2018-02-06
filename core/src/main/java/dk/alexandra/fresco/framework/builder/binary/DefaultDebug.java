package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.debug.BinaryMarker;
import dk.alexandra.fresco.lib.debug.BinaryOpenAndPrint;
import java.io.PrintStream;
import java.util.List;

public class DefaultDebug implements Debug {

  private ProtocolBuilderBinary builder;

  protected DefaultDebug(ProtocolBuilderBinary builder) {
    this.builder = builder;
  }

  @Override
  public void openAndPrint(String label, List<DRes<SBool>> toPrint) {
    openAndPrint(label, toPrint, System.out);
  }

  @Override
  public void openAndPrint(String label, List<DRes<SBool>> toPrint, PrintStream stream) {
    builder.seq(new BinaryOpenAndPrint(label, toPrint, stream));
  }

  @Override
  public void marker(String message) {
    marker(message, System.out);
  }

  @Override
  public void marker(String message, PrintStream stream) {
    builder.seq(new BinaryMarker(message, stream));
  }



}
