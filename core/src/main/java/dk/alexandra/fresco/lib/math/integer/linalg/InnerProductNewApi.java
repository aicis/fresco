package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.SimpleProtocolProducer;

public class InnerProductNewApi extends SimpleProtocolProducer implements Computation<SInt> {

  BasicNumericFactory<SInt> bnf;
  SInt[] a;
  SInt[] b;
  SInt c;

  public InnerProductNewApi(BasicNumericFactory<SInt> bnf, SInt[] a, SInt[] b) {
    super();
    this.a = a;
    this.b = b;
    this.bnf = bnf;
  }

  @Override
  protected ProtocolProducer initializeProtocolProducer() {
    // Root sequential scope ... makes sense   
    ProtocolBuilder<SInt> pb = ProtocolBuilder.createRoot(bnf, seq -> { 
      // Do I really need a new subfactory is that not what "seq" is supposed to be?
      BasicNumericFactory<SInt> bnf1 = seq.createAppendingBasicNumericFactory();
      // Not sure how to do this correctly using the bnf1.get(0, bnf1.getSInt()) Computation?
      c = bnf1.getSInt(0); 
      // How to not get a warning here?
      Computation<? extends SInt>[] temp = new Computation[a.length];
      // Parallel scope for multiplication ... makes sense
      seq.createParallelSubFactory(par -> { 
        // Do I need to make a new subfactory here? Seems redundant. 
        BasicNumericFactory<SInt> bnf2 = par.createAppendingBasicNumericFactory();
        for (int i = 0; i < a.length; i++) {
          temp[i] = bnf2.mult(a[i], b[i]);
        }
      });
      // An other sequential scope. Without this I get a nullpointer not sure why?
      seq.createSequentialSubFactory(seq2 -> { 
        BasicNumericFactory<SInt> bnf2 = seq2.createAppendingBasicNumericFactory();
        for (int i = 0; i < temp.length; i++) {
          // Not sure how I would do this using Computations? The AddList seems overkill.
          bnf2.getAddProtocol(c, temp[i].out(), c);
        }
      });
    });
    return pb.build();
  }

  @Override
  public SInt out() {
    return c;
  }
}
