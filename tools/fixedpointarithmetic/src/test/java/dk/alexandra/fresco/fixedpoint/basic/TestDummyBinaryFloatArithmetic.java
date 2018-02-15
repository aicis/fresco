package dk.alexandra.fresco.fixedpoint.basic;

import dk.alexandra.fresco.decimal.RealNumericProvider;
import dk.alexandra.fresco.decimal.floating.binary.BinaryFloatNumeric;


public class TestDummyBinaryFloatArithmetic extends RealAbstractDummyArithmeticTest {

  @Override
  RealNumericProvider getProvider() {
    return scope -> new BinaryFloatNumeric(scope);
  }
  
}
