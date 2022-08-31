package dk.alexandra.fresco.suite.crt.fixed;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.common.math.AdvancedNumeric;
import dk.alexandra.fresco.lib.fixed.AdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.DefaultAdvancedFixedNumeric;
import dk.alexandra.fresco.lib.fixed.SFixed;
import dk.alexandra.fresco.suite.crt.CRTNumericContext;
import dk.alexandra.fresco.suite.crt.protocols.RandomModP;
import dk.alexandra.fresco.suite.crt.protocols.Truncp;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * This class contains an implementation of {@link AdvancedFixedNumeric} made for the CRT protocol.
 * To use this, the calling application should in the setup phase call {@link
 * AdvancedFixedNumeric#load(Function)} with <code>CRTAdvancedFixedNumeric::new</code> as
 * parameter.
 */
public class CRTAdvancedFixedNumeric extends DefaultAdvancedFixedNumeric {

  private final BigInteger p;

  public CRTAdvancedFixedNumeric(ProtocolBuilderNumeric builder) {
    super(builder);
    this.p = ((CRTNumericContext) builder.getBasicNumericContext()).getLeftModulus();
  }

  @Override
  public DRes<SFixed> innerProduct(List<DRes<SFixed>> a, List<DRes<SFixed>> b) {
    return builder.par(par -> {
      List<DRes<SInt>> products = new ArrayList<>();
      for (int i = 0; i < a.size(); i++) {
        products.add(par.numeric().mult(a.get(i).out().getSInt(), b.get(i).out().getSInt()));
      }
      return DRes.of(products);
    }).seq((seq, products) -> new SFixed(
        new Truncp(AdvancedNumeric.using(seq).sum(products)).buildComputation(seq)));
  }

  @Override
  public DRes<SFixed> innerProductWithPublicPart(List<BigDecimal> a, List<DRes<SFixed>> b) {
    return builder.par(par -> {
      List<DRes<SInt>> products = new ArrayList<>();
      for (int i = 0; i < a.size(); i++) {
        products.add(par.numeric()
            .mult(a.get(i).multiply(new BigDecimal(p)).toBigInteger(), b.get(i).out().getSInt()));
      }
      return DRes.of(products);
    }).seq((seq, products) -> new SFixed(
        new Truncp(AdvancedNumeric.using(seq).sum(products)).buildComputation(seq)));
  }

  @Override
  public DRes<SFixed> random() {
    return builder.seq(seq -> new SFixed(seq.seq(new RandomModP())));
  }

  @Override
  public DRes<SInt> floor(DRes<SFixed> x) {
    return builder.seq(seq -> new Truncp(x.out().getSInt()).buildComputation(seq));
  }

}
