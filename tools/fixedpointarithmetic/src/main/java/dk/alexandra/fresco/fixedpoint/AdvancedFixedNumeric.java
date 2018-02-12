package dk.alexandra.fresco.fixedpoint;

import java.util.List;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationDirectory;
import dk.alexandra.fresco.framework.value.SInt;

public interface AdvancedFixedNumeric extends ComputationDirectory {

  /**
   * Calculate the exponential function of a secret value.
   * 
   * @param x Input
   * @return
   */
  public DRes<SFixed> exp(DRes<SFixed> x);
    
  /**
   * Create a random value between 0 and 1.
   * 
   * @return The random value 
   */
  public DRes<SFixed> random();
  
  /**
   * Compare two secret values. Returns a secret int that is 1 if x \leq y and 0 otherwise.
   *
   * @param x Secret value 1
   * @param y Secret value 2
   * @return A secret int that is 1 if x \leq y and 0 otherwise.
   */  
  DRes<SInt> leq(DRes<SFixed> x, DRes<SFixed> y); 

  /**
   * Calculate the sum of all terms in a list.
   * 
   * @param terms
   * @return
   */
  DRes<SFixed> sum(List<DRes<SFixed>> terms);
}
