package dk.alexandra.fresco.logging.arithmetic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.logging.PerformanceLogger;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class NumericLoggingDecorator implements Numeric, PerformanceLogger {

  public static final String ARITHMETIC_BASIC_MULT = "MULT_COUNT"; 
  public static final String ARITHMETIC_BASIC_ADD = "ADD_COUNT";
  public static final String ARITHMETIC_BASIC_SUB = "SUB_COUNT";
  public static final String ARITHMETIC_BASIC_BIT = "BIT_COUNT";
  public static final String ARITHMETIC_BASIC_RAND = "RANDOM_ELEMENT_COUNT";
  private Numeric delegate;
  private long addCount;
  private long subCount;
  private long bitCount;
  private long randElmCount;
  private long multCount;
  
  public NumericLoggingDecorator(Numeric delegate) {
    super();
    this.delegate = delegate;
  }

  @Override
  public DRes<SInt> add(DRes<SInt> a, DRes<SInt> b) {
    addCount++;
    return this.delegate.add(a, b);
  }

  @Override
  public DRes<SInt> add(BigInteger a, DRes<SInt> b) {
    return this.delegate.add(a, b);
  }

  @Override
  public DRes<SInt> sub(DRes<SInt> a, DRes<SInt> b) {
    subCount++;
    return this.delegate.sub(a, b);
  }

  @Override
  public DRes<SInt> sub(BigInteger a, DRes<SInt> b) {
    return this.delegate.sub(a, b);
  }

  @Override
  public DRes<SInt> sub(DRes<SInt> a, BigInteger b) {
    return this.delegate.sub(a, b);
  }

  @Override
  public DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b) {
    this.multCount++;
    return this.delegate.mult(a, b);
  }

  @Override
  public DRes<SInt> mult(BigInteger a, DRes<SInt> b) {
    return this.delegate.mult(a, b);
  }

  @Override
  public DRes<SInt> randomBit() {
    this.bitCount++;
    return this.delegate.randomBit();
  }

  @Override
  public DRes<SInt> randomElement() {
    this.randElmCount++;
    return this.delegate.randomElement();
  }

  @Override
  public DRes<SInt> known(BigInteger value) {
    return this.delegate.known(value);
  }

  @Override
  public DRes<SInt> input(BigInteger value, int inputParty) {
    return this.delegate.input(value, inputParty);
  }

  @Override
  public DRes<BigInteger> open(DRes<SInt> secretShare) {
    return this.delegate.open(secretShare);
  }

  @Override
  public DRes<BigInteger> open(DRes<SInt> secretShare, int outputParty) {
    return this.delegate.open(secretShare, outputParty);
  }

  @Override
  public void reset() {
    this.multCount = 0;
    this.addCount = 0;
    this.subCount = 0;
    this.bitCount = 0;
    this.randElmCount = 0;
  }

  @Override
  public Map<String, Long> getLoggedValues() {
    Map<String, Long> values = new HashMap<>();
    values.put(ARITHMETIC_BASIC_MULT, this.multCount);
    values.put(ARITHMETIC_BASIC_ADD, this.addCount);
    values.put(ARITHMETIC_BASIC_SUB, this.subCount);
    values.put(ARITHMETIC_BASIC_BIT, this.bitCount);
    values.put(ARITHMETIC_BASIC_RAND, this.randElmCount);
    return values;
  }

}
