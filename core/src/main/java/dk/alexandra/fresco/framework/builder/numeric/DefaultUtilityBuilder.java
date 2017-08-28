package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.debug.ArithmeticOpenAndPrint;
import dk.alexandra.fresco.lib.debug.MarkerProtocolImpl;
import dk.alexandra.fresco.lib.lp.Matrix;
import java.io.PrintStream;
import java.util.List;

public class DefaultUtilityBuilder implements UtilityBuilder {

  private ProtocolBuilderNumeric builder;

  public DefaultUtilityBuilder(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }

  @Override
  public void openAndPrint(String label, Computation<SInt> number, PrintStream stream) {
    builder.seq(new ArithmeticOpenAndPrint(label, number, stream));
  }

  @Override
  public void openAndPrint(String label, List<Computation<SInt>> vector, PrintStream stream) {
    builder.seq(new ArithmeticOpenAndPrint(label, vector, stream));
  }

  @Override
  public void openAndPrint(String label, Matrix<Computation<SInt>> matrix, PrintStream stream) {
    builder.seq(new ArithmeticOpenAndPrint(label, matrix, stream));
  }

  @Override
  public void marker(String message, PrintStream stream) {
    builder.seq(new MarkerProtocolImpl(message, stream));
  }

}
