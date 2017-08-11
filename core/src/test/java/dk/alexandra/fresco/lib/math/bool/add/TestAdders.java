package dk.alexandra.fresco.lib.math.bool.add;

import org.junit.Ignore;
import org.junit.Test;

import dk.alexandra.fresco.framework.value.SBool;

public class TestAdders {

  @Ignore
  @Test(expected = IllegalArgumentException.class)
  public void testInconsistentLengthFullAdder1(){
    SBool[] left = new SBool[8];
    SBool[] right = new SBool[9];
    SBool[] output = new SBool[9];
    
    //fact.getFullAdderProtocol(left, right, new DummySBool(""), output, new DummySBool(""));
  }

  @Ignore
  @Test(expected = IllegalArgumentException.class)
  public void testInconsistentLengthFullAdder2(){
    SBool[] left = new SBool[8];
    SBool[] right = new SBool[8];
    SBool[] output = new SBool[9];
    
    //fact.getFullAdderProtocol(left, right, new DummySBool(""), output, new DummySBool(""));
  }
  @Ignore
  @Test(expected = IllegalArgumentException.class)
  public void testInconsistentLengthIncrement(){
    SBool[] left = new SBool[8];

    SBool[] output = new SBool[7];
    
   // fact.getBitIncrementerProtocol(left, new DummySBool(""), output);
  }
}
