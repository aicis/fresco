package dk.alexandra.fresco.lib.math.bool.mult;

import org.junit.Ignore;
import org.junit.Test;

import dk.alexandra.fresco.framework.value.SBool;

public class TestMult {

  @Ignore
  @Test(expected = IllegalArgumentException.class)
  public void testInconsistentLengthFullAdder2(){
    SBool[] left = new SBool[8];
    SBool[] right = new SBool[8];
    SBool[] output = new SBool[18];
    //fact.getBinaryMultProtocol(left, right, output);
  }
  
}
