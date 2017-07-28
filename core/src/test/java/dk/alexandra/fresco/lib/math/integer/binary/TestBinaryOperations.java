package dk.alexandra.fresco.lib.math.integer.binary;

import java.math.BigInteger;

import org.junit.Test;

import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticFactory;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;

public class TestBinaryOperations {

  BasicNumericFactory factory = new DummyArithmeticFactory(BigInteger.ONE, 80);
  RandomAdditiveMaskFactory randFactory = new RandomAdditiveMaskFactoryImpl(factory,factory);
  RightShiftFactory fact = new RightShiftFactoryImpl(factory,
      randFactory, (LocalInversionFactory)factory);
  
  @Test(expected = IllegalArgumentException.class)
  public void testRepeatedRightShiftBadLength1() {
    fact.getRepeatedRightShiftProtocol(new DummyArithmeticSInt(), -2, new DummyArithmeticSInt());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRepeatedRightShiftBadLength2() {
    fact.getRepeatedRightShiftProtocol(new DummyArithmeticSInt(), 4, new DummyArithmeticSInt(), new SInt[2]);
  }
  
}
