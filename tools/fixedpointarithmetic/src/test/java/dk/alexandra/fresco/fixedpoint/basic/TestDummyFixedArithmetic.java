package dk.alexandra.fresco.fixedpoint.basic;

import dk.alexandra.fresco.decimal.RealNumericProvider;
import dk.alexandra.fresco.decimal.fixed.FixedNumeric;

public class TestDummyFixedArithmetic extends RealAbstractDummyArithmeticTest {

  @Override
  public RealNumericProvider getProvider() {
    return scope -> new FixedNumeric(scope, 5);
  }

}
