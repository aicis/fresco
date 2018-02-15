package dk.alexandra.fresco.decimal.fixed.binary;

import dk.alexandra.fresco.decimal.BasicRealNumeric;
import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.decimal.utils.BigBinary;
import dk.alexandra.fresco.decimal.utils.Truncate;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;
import java.math.BigInteger;

public class BasicBinaryFixedNumeric implements BasicRealNumeric {

  private final int precision;
  private final ProtocolBuilderNumeric builder;

  /**
   * Creates a FixedNumeric which creates basic numeric operations
   *
   * @param builder The protocolbuilder used to construct underlying protocols.
   * @param precision Amount of digits after the dot.
   */
  public BasicBinaryFixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    this.builder = builder;
    this.precision = precision;
  }

  @Override
  public DRes<SReal> known(BigDecimal value) {
    return builder.seq(seq -> {
      BigBinary binary = new BigBinary(value, this.precision);
      DRes<SInt> input =
          seq.numeric().known(binary.unscaledValue());
      return new SBinaryFixed(input);
    });
  }

  @Override
  public DRes<SReal> input(BigDecimal value, int inputParty) {
    return builder.seq(seq -> {
      BigBinary binary = new BigBinary(value, this.precision);
      DRes<SInt> input =
          seq.numeric().input(binary.unscaledValue(), inputParty);
      return new SBinaryFixed(input);
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> secretShare) {
    return builder.seq(seq -> {
      DRes<SInt> sint = ((SBinaryFixed) secretShare.out()).getSInt();
      DRes<BigInteger> bi = seq.numeric().open(sint);
      return () -> new BigBinary(bi.out(), precision).toBigDecimal();
    });
  }

  @Override
  public DRes<BigDecimal> open(DRes<SReal> secretShare, int outputParty) {
    return builder.seq(seq -> {
      DRes<SInt> sint = ((SBinaryFixed) secretShare.out()).getSInt();
      DRes<BigInteger> bi = seq.numeric().open(sint, outputParty);
      return () -> {
        if (bi.out() != null) {
          return new BigBinary(bi.out(), precision).toBigDecimal();
        } else {
          return null;
        }
      };
    });
  }

  @Override
  public DRes<SReal> add(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      BigBinary aScaled = new BigBinary(a, this.precision);
      DRes<SInt> sint = ((SBinaryFixed) b.out()).getSInt();
      DRes<SInt> input = seq.numeric().add(aScaled.unscaledValue(), sint);
      return new SBinaryFixed(input);
    });
  }

  @Override
  public DRes<SReal> add(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      DRes<SInt> add =
          seq.numeric().add(((SBinaryFixed) a.out()).getSInt(), ((SBinaryFixed) b.out()).getSInt());
      return new SBinaryFixed(add);
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      DRes<SInt> sub =
          seq.numeric().sub(((SBinaryFixed) a.out()).getSInt(), ((SBinaryFixed) b.out()).getSInt());
      return new SBinaryFixed(sub);
    });
  }

  @Override
  public DRes<SReal> sub(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      BigBinary aScaled = new BigBinary(a, this.precision);
      DRes<SInt> sint = ((SBinaryFixed) b.out()).getSInt();
      DRes<SInt> input = seq.numeric().sub(aScaled.unscaledValue(), sint);
      return new SBinaryFixed(input);
    });
  }

  @Override
  public DRes<SReal> sub(DRes<SReal> a, BigDecimal b) {
    return builder.seq(seq -> {
      BigBinary bScaled = new BigBinary(b, this.precision);
      DRes<SInt> sint = ((SBinaryFixed) a.out()).getSInt();
      DRes<SInt> input = seq.numeric().sub(sint, bScaled.unscaledValue());
      return new SBinaryFixed(input);
    });
  }

  @Override
  public DRes<SReal> mult(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      DRes<SInt> sintA = ((SBinaryFixed) a.out()).getSInt();
      DRes<SInt> sintB = ((SBinaryFixed) b.out()).getSInt();
      DRes<SInt> input = seq.numeric().mult(sintA, sintB);
      DRes<SInt> output = seq.seq(new Truncate(2 * precision, input, this.precision));
      return new SBinaryFixed(output);
    });
  }

  @Override
  public DRes<SReal> mult(BigDecimal a, DRes<SReal> b) {
    return builder.seq(seq -> {
      BigBinary aScaled = new BigBinary(a, this.precision);
      DRes<SInt> sintB = ((SBinaryFixed) b.out()).getSInt();
      DRes<SInt> input = seq.numeric().mult(aScaled.unscaledValue(), sintB);
      DRes<SInt> output = seq.seq(new Truncate(2 * precision, input, this.precision));
      return new SBinaryFixed(output);
    });
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, DRes<SReal> b) {
    return builder.seq(seq -> {
      DRes<SInt> sintA = ((SBinaryFixed) a.out()).getSInt();
      DRes<SInt> sintB = ((SBinaryFixed) b.out()).getSInt();
      DRes<SInt> scaledA = seq.numeric().mult(BigInteger.valueOf(2).pow(precision), sintA);
      DRes<SInt> input = seq.advancedNumeric().div(scaledA, sintB);
      return new SBinaryFixed(input);
    });
  }

  @Override
  public DRes<SReal> div(DRes<SReal> a, BigDecimal b) {
    return builder.seq(seq -> {
      DRes<SInt> sintA = ((SBinaryFixed) a.out()).getSInt();
      BigBinary bScaled = new BigBinary(b, precision);
      DRes<SInt> input = seq.advancedNumeric().div(
          seq.numeric().mult(BigInteger.valueOf(2).pow(precision), sintA), bScaled.unscaledValue());
      return new SBinaryFixed(input);
    });
  }

  @Override
  public DRes<SReal> fromSInt(DRes<SInt> value) {
    DRes<SInt> scaled = builder.numeric().mult(BigInteger.ONE.shiftLeft(precision), value);
    return new SBinaryFixed(scaled);
  }


}
