package dk.alexandra.fresco.logging.binary;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.binary.Binary;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.logging.PerformanceLogger;
import java.util.HashMap;
import java.util.Map;

public class BinaryLoggingDecorator implements PerformanceLogger, Binary {

  public static final String BINARY_BASIC_XOR = "XOR_COUNT";
  public static final String BINARY_BASIC_AND = "AND_COUNT";
  public static final String BINARY_BASIC_RANDOM = "RANDOM_BIT_COUNT";
  private long xorCount;
  private long andCount;
  private long randBitCount;
  private Binary delegate;
  
  public BinaryLoggingDecorator(Binary delegate) {
    this.delegate = delegate;
  }
  
  @Override
  public DRes<SBool> known(boolean known) {
    return this.delegate.known(known);
  }

  @Override
  public DRes<SBool> input(boolean in, int inputter) {
    return this.delegate.input(in, inputter);
  }

  @Override
  public DRes<SBool> randomBit() {
    this.randBitCount++;
    return this.delegate.randomBit();
  }

  @Override
  public DRes<Boolean> open(DRes<SBool> toOpen) {
    return this.delegate.open(toOpen);
  }

  @Override
  public DRes<Boolean> open(DRes<SBool> toOpen, int towardsPartyId) {
    return this.delegate.open(toOpen, towardsPartyId);
  }

  @Override
  public DRes<SBool> and(DRes<SBool> left, DRes<SBool> right) {
    this.andCount++;
    return this.delegate.and(left, right);
  }

  @Override
  public DRes<SBool> xor(DRes<SBool> left, DRes<SBool> right) {
    this.xorCount++;
    return this.delegate.xor(left, right);
  }

  @Override
  public DRes<SBool> not(DRes<SBool> in) {
    return this.delegate.not(in);
  }

  @Override
  public void reset() {
    this.andCount = 0;
    this.xorCount = 0;
  }

  @Override
  public Map<String, Long> getLoggedValues() {
    Map<String, Long> values = new HashMap<>();
    values.put(BINARY_BASIC_XOR, this.xorCount);
    values.put(BINARY_BASIC_AND, this.andCount);
    values.put(BINARY_BASIC_RANDOM, this.randBitCount);
    return values;
  }

}
