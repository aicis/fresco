package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.collections.Matrix;
import dk.alexandra.fresco.lib.debug.ArithmeticOpenAndPrint;
import dk.alexandra.fresco.lib.debug.NumericMarker;
import java.io.PrintStream;
import java.util.List;

/**
 * Default way of producing the protocols within the interface. This default class can be
 * overwritten when implementing {@link BuilderFactoryNumeric} if the protocol suite has a better
 * and more efficient way of constructing the protocols.
 */
public class DefaultDebug implements Debug {

  private ProtocolBuilderNumeric builder;

  public DefaultDebug(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public void openAndPrint(String label, DRes<SInt> number, PrintStream stream) {
    builder.seq(new ArithmeticOpenAndPrint(label, number, stream));
  }

  @Override
  public void openAndPrint(String label, List<DRes<SInt>> vector, PrintStream stream) {
    builder.seq(new ArithmeticOpenAndPrint(label, vector, stream));
  }

  @Override
  public void openAndPrint(String label, Matrix<DRes<SInt>> matrix, PrintStream stream) {
    builder.seq(new ArithmeticOpenAndPrint(label, matrix, stream));
  }

  @Override
  public void marker(String message, PrintStream stream) {
    builder.seq(new NumericMarker(message, stream));
  }

}
