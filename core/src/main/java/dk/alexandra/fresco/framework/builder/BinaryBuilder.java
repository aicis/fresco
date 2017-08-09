package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SBool;

public interface BinaryBuilder {

  Computation<SBool> known(Boolean value);

  Computation<SBool> input(Boolean value, int inputParty);

  Computation<SBool> randomBit();
  
  Computation<Boolean> open(Computation<SBool> secretShare);
  
  Computation<Boolean> open(Computation<SBool> secretShare, int outputParty);

  
  Computation<SBool> and(Computation<SBool> a, Computation<SBool> b);
  
  Computation<SBool> and(Computation<SBool> a, Boolean b);
  
  Computation<SBool> xor(Computation<SBool> a, Computation<SBool> b);
  
  Computation<SBool> xor(Computation<SBool> a, Boolean b);
  
  Computation<SBool> not(Computation<SBool> a);

}
