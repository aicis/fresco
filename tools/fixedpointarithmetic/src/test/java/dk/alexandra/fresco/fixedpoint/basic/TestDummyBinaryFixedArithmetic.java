package dk.alexandra.fresco.fixedpoint.basic;

import dk.alexandra.fresco.decimal.RealNumericProvider;
import dk.alexandra.fresco.decimal.fixed.binary.BinaryFixedNumeric;


public class TestDummyBinaryFixedArithmetic extends RealAbstractDummyArithmeticTest {

  @Override
  RealNumericProvider getProvider() {
    return scope -> new BinaryFixedNumeric(scope, 16);
  }
  
}
