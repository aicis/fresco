package dk.alexandra.fresco.lib.list;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;

public class TestSIntListOfTuples {

  @Test (expected=RuntimeException.class)
  public void testLengthCheck() {
    SIntListofTuples list = new SIntListofTuples(2);
    list.add(new ArrayList<DRes<SInt>>(), null);
    Assert.fail("Should not be reachable.");
  }
   
}
