package dk.alexandra.fresco.lib.math.bool.add;

import org.junit.Test;

import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.dummy.bool.DummyFactory;
import dk.alexandra.fresco.suite.dummy.bool.DummySBool;

public class TestAdders {

  @Test(expected = IllegalArgumentException.class)
  public void testInconsistentLengthFullAdder1(){
    SBool[] left = new SBool[8];
    SBool[] right = new SBool[9];
    SBool[] output = new SBool[9];
    DummyFactory fact = new DummyFactory();
    fact.getFullAdderProtocol(left, right, new DummySBool(""), output, new DummySBool(""));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInconsistentLengthFullAdder2(){
    SBool[] left = new SBool[8];
    SBool[] right = new SBool[8];
    SBool[] output = new SBool[9];
    DummyFactory fact = new DummyFactory();
    fact.getFullAdderProtocol(left, right, new DummySBool(""), output, new DummySBool(""));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInconsistentLengthIncrement(){
    SBool[] left = new SBool[8];

    SBool[] output = new SBool[7];
    DummyFactory fact = new DummyFactory();
    fact.getBitIncrementerProtocol(left, new DummySBool(""), output);
  }
}
