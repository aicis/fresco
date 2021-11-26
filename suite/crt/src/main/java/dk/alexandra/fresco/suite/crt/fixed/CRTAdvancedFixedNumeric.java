package dk.alexandra.fresco.suite.crt.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.DefaultAdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.protocols.BitLengthProtocol;
import dk.alexandra.fresco.suite.crt.protocols.LEQProtocol;
import dk.alexandra.fresco.suite.crt.protocols.Truncp;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CRTAdvancedFixedNumeric extends DefaultAdvancedFixedNumeric {

  private final BigInteger p;

  public CRTAdvancedFixedNumeric(ProtocolBuilderNumeric builder) {
    super(builder);
    this.p = ((CRTNumericContext) builder.getBasicNumericContext()).getP();
  }

  @Override
  public DRes<SFixed> innerProduct(List<DRes<SFixed>> a, List<DRes<SFixed>> b) {
    return builder.par(par -> {
      List<DRes<SInt>> products = new ArrayList<>();
      for (int i = 0; i < a.size(); i++) {
        products.add(par.numeric().mult(a.get(i).out().getSInt(), b.get(i).out().getSInt()));
      }
      return DRes.of(products);
    }).seq((seq, products) -> new SFixed(new Truncp(AdvancedNumeric.using(seq).sum(products)).buildComputation(seq)));
  }

  @Override
  public DRes<SFixed> innerProductWithPublicPart(List<BigDecimal> a, List<DRes<SFixed>> b) {
    return builder.par(par -> {
      List<DRes<SInt>> products = new ArrayList<>();
      for (int i = 0; i < a.size(); i++) {
        products.add(par.numeric().mult(a.get(i).multiply(new BigDecimal(p)).toBigInteger(), b.get(i).out().getSInt()));
      }
      return DRes.of(products);
    }).seq((seq, products) -> new SFixed(new Truncp(AdvancedNumeric.using(seq).sum(products)).buildComputation(seq)));
  }

  @Override
  public DRes<SFixed> exp(DRes<SFixed> x) {
    return null;
  }

  @Override
  public DRes<SFixed> random() {
    return null;
  }

  @Override
  public DRes<SFixed> log(DRes<SFixed> x) {
    return null;
  }

  @Override
  public DRes<SFixed> sqrt(DRes<SFixed> x) {
    return null;
  }

  @Override
  public DRes<Pair<DRes<SFixed>, DRes<SInt>>> normalize(DRes<SFixed> x) {
    return builder.seq(
      new BitLengthProtocol(x.out().getSInt(), p.bitLength() * 3)
    ).seq((seq, b) ->
      Pair.lazy(new SFixed(seq.seq(new Truncp(seq.numeric().mult(b.getFirst(),
          x.out().getSInt())))), b.getSecond())
    );
  }

  @Override
  public DRes<SFixed> reciprocal(DRes<SFixed> x) {
    return null;
  }

  @Override
  public DRes<SFixed> twoPower(DRes<SInt> x) {
    return null;
  }

  @Override
  public DRes<SInt> floor(DRes<SFixed> x) {
    return builder.seq(seq -> new Truncp(x.out().getSInt()).buildComputation(seq));
  }

  @Override
  public DRes<SInt> sign(DRes<SFixed> x) {
    return builder.seq(seq -> new LEQProtocol(x.out().getSInt(), seq.numeric().known(0)).buildComputation(seq));
  }
}
