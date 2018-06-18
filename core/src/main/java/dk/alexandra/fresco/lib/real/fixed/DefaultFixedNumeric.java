package dk.alexandra.fresco.lib.real.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.real.RealNumeric;
import dk.alexandra.fresco.lib.real.SReal;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * An implementation of the {@link RealNumeric} ComputationDirectory based on a fixed point
 * representation of real numbers.
 */
public class DefaultFixedNumeric implements RealNumeric {

  protected final ProtocolBuilderNumeric builder;
  // TODO these should be cached static fields
  private final int precision;
  private final BigInteger scalingFactor;
  private final BigDecimal scalingFactorDecimal;

  public DefaultFixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    this.builder = builder;
    this.precision = precision;
    this.scalingFactor = BigInteger.ONE.shiftLeft(precision);
    this.scalingFactorDecimal = new BigDecimal(scalingFactor);
  }

  public DefaultFixedNumeric(ProtocolBuilderNumeric builder) {
    this(builder, builder.getRealNumericContext().getPrecision());
  }

  private BigInteger unscaled(BigDecimal value) {
    // TODO extremely inefficient
    return value.multiply(scalingFactorDecimal).setScale(0, RoundingMode.HALF_UP)
        .toBigIntegerExact();
  }

  private BigDecimal scaled(BigInteger value) {
    // TODO extremely inefficient
    return new BigDecimal(value).setScale(precision).divide(scalingFactorDecimal,
        RoundingMode.HALF_UP);
  }

  @Override
  public DRes<SReal> add(DRes<SReal> a, DRes<SReal> b) {
    return null;
  }

  @Override
  public DRes<SReal> add(BigDecimal a, DRes<SReal> b) {
    return null;
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, DRes<SReal> b) {
    return null;
  }

  @Override
  public DRes<SReal> sub(BigDecimal a, DRes<SReal> b) {
    return null;
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, BigDecimal b) {
    return null;
  }

  @Override
  public DRes<SReal> mult(DRes<SReal> a, DRes<SReal> b) {
    return null;
  }

  @Override
  public DRes<SReal> mult(BigDecimal a, DRes<SReal> b) {
    return null;
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, DRes<SReal> b) {
    return null;
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, BigDecimal b) {
    return null;
  }

  @Override
  public DRes<SReal> known(BigDecimal value) {
    return builder.seq(seq -> {
      DRes<SInt> input = seq.numeric().known(unscaled(value));
      return new SFixed(input, precision);
    });
  }

  @Override
  public DRes<SReal> fromSInt(DRes<SInt> value) {
    return null;
  }

  @Override
  public DRes<SReal> input(BigDecimal value, int inputParty) {
    return builder.seq(seq -> {
      BigInteger unscaled = (value != null) ? unscaled(value) : null;
      DRes<SInt> input = seq.numeric().input(unscaled, inputParty);
      return new SFixed(input, precision);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> secretShare) {
    return builder.seq(seq -> {
      SFixed floatX = (SFixed) secretShare.out();
      DRes<SInt> unscaled = floatX.getSInt();
      return seq.numeric().open(unscaled);
    }).seq((seq, unscaled) -> {
      // new scope to avoid scaling in lambda (since that would be recalculated every time value.out
      // is called)
      BigDecimal scaled = scaled(unscaled);
      return () -> scaled;
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> secretShare, int outputParty) {
    return builder.seq(seq -> {
      SFixed floatX = (SFixed) secretShare.out();
      DRes<SInt> unscaled = floatX.getSInt();
      return seq.numeric().open(unscaled, outputParty);
    }).seq((seq, unscaled) -> {
      // new scope to avoid scaling in lambda (since that would be recalculated every time value.out
      // is called)
      if (unscaled != null) {
        BigDecimal scaled = scaled(unscaled);
        return () -> scaled;
      } else {
        return null;
      }
    });
  }

  @Override
  public DRes<SInt> leq(DRes<SReal> x, DRes<SReal> y) {
    return null;
  }
}
