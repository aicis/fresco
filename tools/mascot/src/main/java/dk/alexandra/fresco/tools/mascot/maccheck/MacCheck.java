package dk.alexandra.fresco.tools.mascot.maccheck;

import dk.alexandra.fresco.tools.mascot.field.FieldElement;

public interface MacCheck {

  public boolean check(FieldElement opened, FieldElement macKeyShare, FieldElement macShare);

}
