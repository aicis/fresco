package dk.alexandra.fresco.lib.math.bool.mult;

import org.junit.Test;

import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.dummy.bool.DummyFactory;

public class TestMult {

  @Test(expected = IllegalArgumentException.class)
  public void testInconsistentLengthFullAdder2(){
    SBool[] left = new SBool[8];
    SBool[] right = new SBool[8];
    SBool[] output = new SBool[18];
    DummyFactory fact = new DummyFactory();
    fact.getBinaryMultProtocol(left, right, output);
  }
  
}
