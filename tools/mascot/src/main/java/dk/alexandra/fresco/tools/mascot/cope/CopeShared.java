package dk.alexandra.fresco.tools.mascot.cope;

import java.math.BigInteger;

import dk.alexandra.fresco.tools.mascot.MascotContext;
import dk.alexandra.fresco.tools.mascot.TwoPartyProtocol;
import dk.alexandra.fresco.tools.mascot.utils.DummyPrg;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;

public class CopeShared extends TwoPartyProtocol {

  protected boolean initialized;
//  protected FieldElementPrg prf;
//  protected BigInteger prfCounter;
  
  public CopeShared(MascotContext ctx, Integer otherId) {
    super(ctx, otherId);
//    this.prf = new DummyPrg();
    this.initialized = false;
//    this.prfCounter = BigInteger.ZERO;
  }

}
