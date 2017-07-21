package dk.alexandra.fresco.lib.compare;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticFactory;

public class TestSortingProtocolBuilder {

  @Test
  public void testCreateSortSequence() {
    DummyArithmeticFactory fact = new DummyArithmeticFactory(BigInteger.ONE, 80);
    
    SortingProtocolBuilder builder = new SortingProtocolBuilder(null, fact);
    List<Map<Integer, Integer>> sortSequence = builder.createsortSequence(50);
    //TODO att: PSN the tested method is not used outside of this test.
    //Not sure what should be tested.
  }
  
}
