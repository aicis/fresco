package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.SimpleProtocolProducer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class InnerProductNewApi extends SimpleProtocolProducer implements Computation<SInt> {

  private BuilderFactoryNumeric bnf;
  private SInt[] a;
  private SInt[] b;
  private Computation<SInt> c;

  InnerProductNewApi(BuilderFactoryNumeric bnf, SInt[] a, SInt[] b) {
    super();
    this.a = a;
    this.b = b;
    this.bnf = bnf;
  }

  @Override
  protected ProtocolProducer initializeProtocolProducer() {
    // Root sequential scope ... makes sense   
    ProtocolBuilder pb = ProtocolBuilderNumeric.createApplicationRoot(bnf, seq ->
        // Parallel scope for multiplication ... makes sense
        c = seq.par(par -> {
          List<Computation<SInt>> temp = new ArrayList<>();
          for (int i = 0; i < a.length; i++) {
            temp.add(par.numeric().mult(a[i], b[i]));
          }
          return () -> temp;
        }).seq((addents, subSeq) -> {
          Computation<SInt> c = subSeq.numeric().known(BigInteger.valueOf(0));
          for (Computation<SInt> aTemp : addents) {
            c = subSeq.numeric().add(c, aTemp);
          }
          return c;
        })
    );
    return pb.build();
  }

  @Override
  public SInt out() {
    return c.out();
  }
}
