package dk.alexandra.fresco.lib.math.integer.linalg;
/*
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.RandomFieldElementFactory;
import dk.alexandra.fresco.lib.lp.LPFactory;
import dk.alexandra.fresco.lib.lp.LPFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticFactory;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;

public class TestInnerProduct {

  @Test (expected = IllegalArgumentException.class)
  public void testBadLengthOpen() {
        BasicNumericFactory basicNumericFactory = new DummyArithmeticFactory(new BigInteger("3"), 8);
    InnerProductFactory innerProductFactory = new InnerProductFactoryImpl(basicNumericFactory);
    
    SInt[] input1 = new SInt[3];
    OInt[] input2 = new OInt[4];
    innerProductFactory.getInnerProductProtocol(input1, input2, new DummyArithmeticSInt());
    Assert.fail("Should not be reachable.");
  }
  
  @Test (expected = IllegalArgumentException.class)
  public void testBadLengthClosed() {
    BasicNumericFactory basicNumericFactory = new DummyArithmeticFactory(new BigInteger("3"), 8);
    InnerProductFactory innerProductFactory = new InnerProductFactoryImpl(basicNumericFactory);
    
    SInt[] input1 = new SInt[3];
    SInt[] input2 = new SInt[4];
    innerProductFactory.getInnerProductProtocol(input1, input2, new DummyArithmeticSInt());
    Assert.fail("Should not be reachable.");
  }

  @Test (expected = IllegalArgumentException.class)
  public void testAltBadLengthClosed() {
    BasicNumericFactory provider = new DummyArithmeticFactory(new BigInteger("3"), 8);
    LocalInversionFactory localInvFactory = (LocalInversionFactory) provider;
    NumericBitFactory numericBitFactory = (NumericBitFactory) provider;
    ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory) provider;
    PreprocessedExpPipeFactory expFactory = (PreprocessedExpPipeFactory) provider;
    RandomFieldElementFactory randFactory = (RandomFieldElementFactory) provider;
    LPFactory copyFactory = new LPFactoryImpl(80, provider, localInvFactory, numericBitFactory,
        expFromOIntFactory, expFactory, randFactory);

    SInt[] input1 = new SInt[3];
    SInt[] input2 = new SInt[4];
    new AltInnerProductProtocolImpl(input1, input2, new DummyArithmeticSInt(), provider, copyFactory);
    Assert.fail("Should not be reachable.");
  }

  @Test (expected = IllegalArgumentException.class)
  public void testBadLengthEntrywiseClosed1() {
    BasicNumericFactory basicNumericFactory = new DummyArithmeticFactory(new BigInteger("3"), 8);
    
    SInt[] input1 = new SInt[3];
    SInt[] input2 = new SInt[4];
    SInt[] results = new SInt[4];
    new EntrywiseProductProtocolImpl(input1, input2, results, basicNumericFactory);
    
    Assert.fail("Should not be reachable.");
  }  

  @Test (expected = IllegalArgumentException.class)
  public void testBadLengthEntrywiseClosed2() {
    BasicNumericFactory basicNumericFactory = new DummyArithmeticFactory(new BigInteger("3"), 8);
    
    SInt[] input1 = new SInt[3];
    SInt[] input2 = new SInt[3];
    SInt[] results = new SInt[4];
    new EntrywiseProductProtocolImpl(input1, input2, results, basicNumericFactory);
    
    Assert.fail("Should not be reachable.");
  }

  @Test (expected = IllegalArgumentException.class)
  public void testBadLengthEntrywiseOpen1() {
    BasicNumericFactory basicNumericFactory = new DummyArithmeticFactory(new BigInteger("3"), 8);
    
    SInt[] input1 = new SInt[3];
    OInt[] input2 = new OInt[4];
    SInt[] results = new SInt[4];
    new EntrywiseProductProtocolImpl(input1, input2, results, basicNumericFactory);
    
    Assert.fail("Should not be reachable.");
  }  

  @Test (expected = IllegalArgumentException.class)
  public void testBadLengthEntrywiseOpen2() {
    BasicNumericFactory basicNumericFactory = new DummyArithmeticFactory(new BigInteger("3"), 8);
    
    SInt[] input1 = new SInt[3];
    OInt[] input2 = new OInt[3];
    SInt[] results = new SInt[4];
    new EntrywiseProductProtocolImpl(input1, input2, results, basicNumericFactory);
    
    Assert.fail("Should not be reachable.");
  }
  
}
*/