package dk.alexandra.fresco.lib.math.bool.add;

import java.util.ArrayList;
import java.util.List;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.DRes;

import org.junit.Assert;
import org.junit.Test;


public class TestFullAdder {

  @Test(expected = IllegalArgumentException.class)
  public void testInconsistentLength() {
    
    List<DRes<SBool>> left = new ArrayList<>();
    List<DRes<SBool>> right = new ArrayList<>();
    
    left.add(null);
    left.add(null);
    right.add(null);
    new FullAdder(left, right, null);
    Assert.fail();
  }
  
}
