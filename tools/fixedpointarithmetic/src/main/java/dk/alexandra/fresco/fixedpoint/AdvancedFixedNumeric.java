package dk.alexandra.fresco.fixedpoint;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;

public interface AdvancedFixedNumeric extends ComputationDirectory {

  public DRes<SFixed> exp(DRes<SFixed> x);
  
}
