package dk.alexandra.fresco.suite.tinytables.ot.datatypes;

import org.junit.Test;

public class TestOtInput {

  @Test (expected = IllegalArgumentException.class)
  public void testConstructorLengthNotMatching() {
    boolean[] input0 = new boolean[] { true, true, true };
    boolean[] input1 = new boolean[] { true, true };
    new OTInput(input0, input1);
  }

}
