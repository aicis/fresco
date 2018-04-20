package dk.alexandra.fresco.logging.arithmetic;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Comparison;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.logging.PerformanceLogger;
import java.util.HashMap;
import java.util.Map;

public class ComparisonLoggerDecorator implements Comparison, PerformanceLogger {

  public static final String ARITHMETIC_COMPARISON_COMP0 = "COMP0_COUNT";
  public static final String ARITHMETIC_COMPARISON_LEQ = "LEQ_COUNT";
  public static final String ARITHMETIC_COMPARISON_SIGN = "SIGN_COUNT";
  public static final String ARITHMETIC_COMPARISON_EQ = "EQ_COUNT";

  private Comparison delegate;
  private long eqCount;
  private long leqCount;
  private long ltCount;
  private long signCount;
  private long comp0Count;
  
  public ComparisonLoggerDecorator(Comparison delegate) {
    super();
    this.delegate = delegate;
  }

  @Override
  public DRes<SInt> equals(DRes<SInt> x, DRes<SInt> y) {
    eqCount++;
    return this.delegate.equals(x, y);
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
  public DRes<SInt> compareLT(DRes<SInt> x1, DRes<SInt> x2, ComparisonAlgorithm algorithm) {
    ltCount++;
    return this.delegate.compareLT(x1, x2, algorithm);
  }

  @Override
  public DRes<SInt> compareLEQLong(DRes<SInt> x1, DRes<SInt> x2) {
    leqCount++;
    return this.delegate.compareLEQLong(x1, x2);
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
  public void reset() {
    eqCount = 0;
    leqCount = 0;
    ltCount = 0;
    signCount = 0;
    comp0Count = 0;
  }

  @Override
  public Map<String, Long> getLoggedValues() {
    // TODO add ltCount
    Map<String, Long> values = new HashMap<>();
    values.put(ARITHMETIC_COMPARISON_EQ, this.eqCount);
    values.put(ARITHMETIC_COMPARISON_LEQ, this.leqCount);
    values.put(ARITHMETIC_COMPARISON_SIGN, this.signCount);
    values.put(ARITHMETIC_COMPARISON_COMP0, this.comp0Count);
    return values;
  }

}
