package dk.alexandra.fresco.lib.math.integer.min;

import java.math.BigInteger;

import org.junit.Test;

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

public class TestMin {

  BasicNumericFactory factory = new DummyArithmeticFactory(new BigInteger("23"), 8);
  NumericBitFactory preprocessedNumericBitFactory = (NumericBitFactory) factory;
  ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory)factory;
  PreprocessedExpPipeFactory preprocessedExpPipeFactory = (PreprocessedExpPipeFactory)factory;
  LocalInversionFactory localInversionFactory = (LocalInversionFactory) factory;
  LPFactory lpFactory = new LPFactoryImpl(80, factory, localInversionFactory,
      preprocessedNumericBitFactory, expFromOIntFactory, preprocessedExpPipeFactory,
      (RandomFieldElementFactory)factory);
  
  @Test(expected = IllegalArgumentException.class)
  public void testMinimumProtocolTooShort(){
    lpFactory.getMinimumProtocol(new SInt[1], new DummyArithmeticSInt(), new SInt[2]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMinimumProtocolInconsistent(){
    lpFactory.getMinimumProtocol(new SInt[4], new DummyArithmeticSInt(), new SInt[6]);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testMinimumFracProtocolTooShort(){
    lpFactory.getMinimumFractionProtocol(new SInt[1], new SInt[1], new DummyArithmeticSInt(), new DummyArithmeticSInt(), new SInt[2]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMinimumFracProtocolInconsistent1(){
    lpFactory.getMinimumFractionProtocol(new SInt[3], new SInt[4], new DummyArithmeticSInt(), new DummyArithmeticSInt(), new SInt[3]);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testMinimumFracProtocolInconsistent2(){
    lpFactory.getMinimumFractionProtocol(new SInt[4], new SInt[4], new DummyArithmeticSInt(), new DummyArithmeticSInt(), new SInt[3]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMinInfFracProtocolInconsistent1(){
    lpFactory.getMinInfFracProtocol(new SInt[3], new SInt[4], new SInt[4],
        new DummyArithmeticSInt(), new DummyArithmeticSInt(), new DummyArithmeticSInt(), new SInt[3]);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testMinInfFracProtocolInconsistent2(){
    lpFactory.getMinInfFracProtocol(new SInt[4], new SInt[4], new SInt[4],
        new DummyArithmeticSInt(), new DummyArithmeticSInt(), new DummyArithmeticSInt(), new SInt[3]);
  }
}
