package dk.alexandra.fresco.lib.math.integer.stat;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactory;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactoryImpl;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactoryImpl;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsFactory;
import dk.alexandra.fresco.lib.conversion.IntegerToBitsFactoryImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.binary.BitLengthFactory;
import dk.alexandra.fresco.lib.math.integer.binary.BitLengthFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactory;
import dk.alexandra.fresco.lib.math.integer.binary.RightShiftFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.division.DivisionFactory;
import dk.alexandra.fresco.lib.math.integer.division.DivisionFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExponentiationFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticFactory;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;

public class TestStatistics {

  private BasicNumericFactory basicNumericFactory = new DummyArithmeticFactory(new BigInteger("3"), 8);
  private NumericBitFactory preprocessedNumericBitFactory = (NumericBitFactory) basicNumericFactory;
  private ExpFromOIntFactory expFromOIntFactory = (ExpFromOIntFactory)basicNumericFactory;
  private PreprocessedExpPipeFactory preprocessedExpPipeFactory = (PreprocessedExpPipeFactory)basicNumericFactory;
  private RandomAdditiveMaskFactory randomAdditiveMaskFactory = new RandomAdditiveMaskFactoryImpl(basicNumericFactory, preprocessedNumericBitFactory);
  private LocalInversionFactory localInversionFactory = (LocalInversionFactory) basicNumericFactory;
  private RightShiftFactory rightShiftFactory = new RightShiftFactoryImpl(basicNumericFactory, randomAdditiveMaskFactory, localInversionFactory);
  private IntegerToBitsFactory integerToBitsFactory = new IntegerToBitsFactoryImpl(basicNumericFactory, rightShiftFactory);
  private BitLengthFactory bitLengthFactory = new BitLengthFactoryImpl(basicNumericFactory, integerToBitsFactory);
  private ExponentiationFactory exponentiationFactory = new ExponentiationFactoryImpl(basicNumericFactory, integerToBitsFactory);
  private ComparisonProtocolFactory comparisonFactory = new ComparisonProtocolFactoryImpl(80, basicNumericFactory, localInversionFactory, preprocessedNumericBitFactory, expFromOIntFactory, preprocessedExpPipeFactory);
  private DivisionFactory euclidianDivisionFactory = new DivisionFactoryImpl(basicNumericFactory, rightShiftFactory, bitLengthFactory, exponentiationFactory, comparisonFactory);
  private StatisticsFactory statisticsFactory = new StatisticsFactoryImpl(basicNumericFactory, euclidianDivisionFactory);

  
  @Test (expected = IllegalArgumentException.class)
  public void testBadLength() {
    SInt[] input1 = new SInt[3];
    SInt[] input2 = new SInt[4];
    statisticsFactory.getCovarianceProtocol(input1, input2, new DummyArithmeticSInt());
    Assert.fail("Should not be reachable.");
  }

  @Test (expected = IllegalArgumentException.class)
  public void testBadLengthMatrix() {
    SInt[] input1 = new SInt[3];
    SInt[] input2 = new SInt[4];
    SInt[][] input = new SInt[][]{input1, input2};
    SInt[] mean = new SInt[2];
    SInt[][] result = new SInt[2][2];
    statisticsFactory.getCovarianceMatrixProtocol(input, mean, result);
    Assert.fail("Should not be reachable.");
  }
  
}
