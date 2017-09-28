package dk.alexandra.fresco.logging;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Comparison;
import dk.alexandra.fresco.framework.value.SInt;

public class ComparisonLoggerDecorator implements Comparison, PerformanceLogger {

  private Comparison delegate;
  private long eqCount;
  private long leqCount;
  private long signCount;
  private long comp0Count;
  
  public ComparisonLoggerDecorator(Comparison delegate) {
    super();
    this.delegate = delegate;
  }

  @Override
  public DRes<SInt> equals(int bitLength, DRes<SInt> x, DRes<SInt> y) {
    eqCount++;
    return this.delegate.equals(x, y);
  }

  @Override
  public DRes<SInt> compareLEQ(DRes<SInt> x1, DRes<SInt> x2) {
    leqCount++;
    return this.delegate.compareLEQ(x1, x2);
  }

  @Override
  public DRes<SInt> compareLEQLong(DRes<SInt> x1, DRes<SInt> x2) {
    leqCount++;
    return this.delegate.compareLEQLong(x1, x2);
  }

  @Override
  public DRes<SInt> equals(DRes<SInt> x, DRes<SInt> y) {
    eqCount++;
    return this.delegate.equals(x, y);
  }

  @Override
  public DRes<SInt> sign(DRes<SInt> x) {
    signCount++;
    return this.delegate.sign(x);
  }

  @Override
  public DRes<SInt> compareZero(DRes<SInt> x, int bitLength) {
    comp0Count++;
    return this.delegate.compareZero(x, bitLength);
  }

  @Override
  public void printPerformanceLog(int myId) {
    log.info("=== P"+myId+": Comparison operations logged - results ===");
    log.info("EQ: " + this.eqCount);
    log.info("LEQ: " + this.leqCount);
    log.info("Compute sign: " + this.signCount);
    log.info("Compare to 0: " + this.comp0Count);
  }

  @Override
  public void reset() {
    eqCount = 0;
    leqCount = 0;
    signCount = 0;
    comp0Count = 0;
  }

  public void setDelegate(Comparison comp) {
    this.delegate = comp;
  }

}
