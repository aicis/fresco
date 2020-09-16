package dk.alexandra.fresco.lib.common.compare.eq;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.common.compare.bool.eq.BinaryEquality;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;


public class TestBinaryEquality {

  @Test(expected = IllegalArgumentException.class)
  public void testInconsistentLength() {
    
    List<DRes<SBool>> left = new ArrayList<>();
    List<DRes<SBool>> right = new ArrayList<>();
    
    left.add(null);
    left.add(null);
    right.add(null);
    new BinaryEquality(left, right);
    Assert.fail();
  }
  
}
