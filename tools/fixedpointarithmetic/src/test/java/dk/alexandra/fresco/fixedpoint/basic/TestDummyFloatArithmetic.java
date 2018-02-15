package dk.alexandra.fresco.fixedpoint.basic;

import dk.alexandra.fresco.decimal.RealNumericProvider;
import dk.alexandra.fresco.decimal.floating.FloatNumeric;

public class TestDummyFloatArithmetic extends RealAbstractDummyArithmeticTest {

  @Override
  RealNumericProvider getProvider() {
    return scope -> new FloatNumeric(scope);
  }
  
}
