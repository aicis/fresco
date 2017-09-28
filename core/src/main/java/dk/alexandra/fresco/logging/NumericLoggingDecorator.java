package dk.alexandra.fresco.logging;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

public class NumericLoggingDecorator implements Numeric, PerformanceLogger {

  private Numeric delegate;
  private long addCount;
  private long subCount;
  private long bitCount;
  private long randElmCount;
  private long expCount;
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
  public DRes<SInt[]> getExponentiationPipe() {
    this.expCount++;
    return this.delegate.getExponentiationPipe();
  }

  @Override
  public void printPerformanceLog(int myId) {
    log.info("=== P"+myId+": Basic numeric operations logged - results ===");
    log.info("Multiplications: " + this.multCount);
    log.info("Additions: " + this.addCount);
    log.info("Subtractions: " + this.subCount);
    log.info("Random bits fetched: " + this.bitCount);
    log.info("Random elements fetched: " + this.randElmCount);
    log.info("Exponentiation pipes fetched: " + this.expCount);
  }

  @Override
  public void reset() {
    this.multCount = 0;
    this.addCount = 0;
    this.subCount = 0;
    this.bitCount = 0;
    this.randElmCount = 0;
    this.expCount = 0;
  }

  public void setDelegate(Numeric numeric) {
    this.delegate = numeric;
  }

}
