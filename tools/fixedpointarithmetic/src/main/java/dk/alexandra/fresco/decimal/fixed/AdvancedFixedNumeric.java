package dk.alexandra.fresco.decimal.fixed;

import dk.alexandra.fresco.decimal.DefaultAdvancedRealNumeric;
import dk.alexandra.fresco.decimal.SReal;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

public class AdvancedFixedNumeric extends DefaultAdvancedRealNumeric {

  private ProtocolBuilderNumeric builder;
  private int precision;

  public AdvancedFixedNumeric(ProtocolBuilderNumeric builder, int precision) {
    super(builder, scope -> new FixedNumeric(scope, precision));
    this.builder = builder;
    this.precision = precision;
  }

  @Override
  public DRes<SInt> leq(DRes<SReal> x, DRes<SReal> y) {
    return builder.seq(seq -> {
      return seq.comparison().compareLEQ(((SFixed) x.out()).getSInt(),
          ((SFixed) y.out()).getSInt());
    });
  }

  @Override
  public DRes<SReal> innerProductWithPublicPart(List<BigDecimal> a, List<DRes<SReal>> b) {
    return builder.seq(seq -> {
      if (a.size() != b.size()) {
        throw new IllegalArgumentException("Vectors must have same size");
      }
      List<BigInteger> aInt =
          a.stream().map(x -> x.setScale(precision, RoundingMode.DOWN).unscaledValue())
              .collect(Collectors.toList());
      List<DRes<SInt>> bInt =
          b.stream().map(x -> ((SFixed) x.out()).getSInt()).collect(Collectors.toList());
      DRes<SInt> innerProductInt = seq.advancedNumeric().innerProductWithPublicPart(aInt, bInt);
      DRes<SInt> innerProductUnscaled =
          seq.advancedNumeric().div(innerProductInt, BigInteger.TEN.pow(precision));
      return new SFixed(innerProductUnscaled);
    });
  }

  @Override
  public DRes<SReal> innerProduct(List<DRes<SReal>> a, List<DRes<SReal>> b) {
    return builder.seq(seq -> {
      if (a.size() != b.size()) {
        throw new IllegalArgumentException("Vectors must have same size");
      }
      List<DRes<SInt>> aInt =
          a.stream().map(x -> ((SFixed) x.out()).getSInt()).collect(Collectors.toList());
      List<DRes<SInt>> bInt =
          b.stream().map(x -> ((SFixed) x.out()).getSInt()).collect(Collectors.toList());
      DRes<SInt> innerProductInt = seq.advancedNumeric().innerProduct(aInt, bInt);
      DRes<SInt> innerProductUnscaled =
          seq.advancedNumeric().div(innerProductInt, BigInteger.TEN.pow(precision));
      return new SFixed(innerProductUnscaled);
    });
  }

}
