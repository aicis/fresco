package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.SimpleProtocolProducer;
import java.util.ArrayList;
import java.util.List;

public class InnerProductNewApi extends SimpleProtocolProducer implements Computation<SInt> {

  private BuilderFactoryNumeric<SInt> bnf;
  private SInt[] a;
  private SInt[] b;
  private Computation<SInt> c;

  InnerProductNewApi(BuilderFactoryNumeric<SInt> bnf, SInt[] a, SInt[] b) {
    super();
    this.a = a;
    this.b = b;
    this.bnf = bnf;
  }

  @Override
  protected ProtocolProducer initializeProtocolProducer() {
    // Root sequential scope ... makes sense   
    ProtocolBuilder<SInt> pb = ProtocolBuilder.createRoot(bnf, seq -> {
      c =
          seq.par(
              par -> {
                List<Computation<SInt>> temp = new ArrayList<>();
                NumericBuilder<SInt> parNumericBuilder = par.createNumericBuilder();
                for (int i = 0; i < a.length; i++) {
                  temp.add(parNumericBuilder.mult(a[i], b[i]));
                }
                return () -> temp;
              }).seq(
              (addents, subSeq) -> {
                NumericBuilder<SInt> numericBuilder = subSeq.createNumericBuilder();
                // Not sure how to do this correctly using the bnf1.get(0, bnf1.getSInt()) Computation?
                // PFF - neither am I - hence the old API
                Computation<SInt> c = seq.getSIntFactory().getSInt(0);
                for (Computation<SInt> aTemp : addents) {
                  // Not sure how I would do this using Computations? The AddList seems overkill.
                  // PFF - no it is not...
                  c = numericBuilder.add(c, aTemp);
                }
                return c;
              }
          );
    });
    return pb.build();
  }

  @Override
  public SInt out() {
    return c.out();
  }
}
