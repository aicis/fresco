package dk.alexandra.fresco.tools.mascot.maccheck;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public interface MacCheck {

  public void check(FieldElement opened, FieldElement macKeyShare, FieldElement macShare)
      throws MPCException;

}
