package dk.alexandra.fresco.logging.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.binary.Comparison;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.logging.PerformanceLogger;
import java.util.List;
import org.slf4j.Logger;

public class BinaryComparisonLoggingDecorator implements PerformanceLogger, Comparison {

  private Comparison delegate;
  private int gtCount;
  private int eqCount;
  
  public BinaryComparisonLoggingDecorator(Comparison delegate) {
    this.delegate = delegate;
  }
  
  @Override
  public DRes<SBool> greaterThan(List<DRes<SBool>> inLeft, List<DRes<SBool>> inRight) {
    this.gtCount++;
    return this.delegate.greaterThan(inLeft, inRight);
  }

  @Override
  public DRes<SBool> equal(List<DRes<SBool>> inLeft, List<DRes<SBool>> inRight) {
    this.eqCount++;
    return this.delegate.equal(inLeft, inRight);
  }

  @Override
  public void printToLog(Logger log, int myId) {
    log.info("=== Binary comparison operations logged - results ===");
    log.info("Greater than: " + this.gtCount);
    log.info("Equals: " + this.eqCount);
  }

  @Override
  public void reset() {
    this.gtCount = 0;
    this.eqCount = 0;
  }
  
  public void setDelegate(Comparison comp) {
    this.delegate = comp;
  }

}
