package dk.alexandra.fresco.suite.spdz;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import java.math.BigInteger;
import org.junit.Test;

public class TestSpdzTriple {

  @Test
  public void testEquals(){
    SpdzElement a = new SpdzElement(BigInteger.ONE, BigInteger.ZERO, BigInteger.ONE);
    SpdzElement b = new SpdzElement(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ONE);
    SpdzTriple element = new SpdzTriple(a, b, a);
    
    assertTrue(element.equals(element));
    assertFalse(element.equals("This is a String"));
    assertFalse(element.equals(null));
    
    SpdzTriple element2 = new SpdzTriple(a, null, a);
    assertFalse(element.equals(element2));
    element2 = new SpdzTriple(a, a, a);
    assertFalse(element.equals(element2));
    element = new SpdzTriple(a, null, a);
    assertFalse(element.equals(element2));
    element2 = new SpdzTriple(a, null, a);
    assertTrue(element.equals(element2));
    
    element2 = new SpdzTriple(a, null, null);
    assertFalse(element.equals(element2));
    element2 = new SpdzTriple(a, null, b);
    assertFalse(element.equals(element2));
    element = new SpdzTriple(a, null, null);
    assertFalse(element.equals(element2));
    element2 = new SpdzTriple(a, null, null);
    assertTrue(element.equals(element2));
    
    element = new SpdzTriple(null, b, a);
    element2 = new SpdzTriple(a, b, a);
    assertFalse(element.equals(element2));
    element2 = new SpdzTriple(null, b, a);
    assertTrue(element.equals(element2));
    element = new SpdzTriple(b, b, a);
    assertFalse(element.equals(element2));
  }
  
}
