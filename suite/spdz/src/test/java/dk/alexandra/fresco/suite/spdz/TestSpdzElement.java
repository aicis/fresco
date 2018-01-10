package dk.alexandra.fresco.suite.spdz;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import java.math.BigInteger;
import org.junit.Test;

public class TestSpdzElement {

  @Test
  public void testEquals(){
    SpdzElement element = new SpdzElement(BigInteger.valueOf(25), BigInteger.valueOf(15), BigInteger.valueOf(251));
    
    assertTrue(element.equals(element));
    assertFalse(element.equals("This is a String"));
    assertFalse(element.equals(null));
    
    SpdzElement element2 = new SpdzElement(BigInteger.valueOf(25), null, BigInteger.valueOf(251));
    assertFalse(element.equals(element2));
    element2 = new SpdzElement(BigInteger.valueOf(25), BigInteger.valueOf(11), BigInteger.valueOf(251));
    assertFalse(element.equals(element2));
    element = new SpdzElement(BigInteger.valueOf(25), null, BigInteger.valueOf(251));
    assertFalse(element.equals(element2));
    element2 = new SpdzElement(BigInteger.valueOf(25), null, BigInteger.valueOf(251));
    assertTrue(element.equals(element2));
    
    element2 = new SpdzElement(BigInteger.valueOf(25), null, null);
    assertFalse(element.equals(element2));
    element2 = new SpdzElement(BigInteger.valueOf(25), null, BigInteger.valueOf(23));
    assertFalse(element.equals(element2));
    element = new SpdzElement(BigInteger.valueOf(25), null, null);
    assertFalse(element.equals(element2));
    element2 = new SpdzElement(BigInteger.valueOf(25), null, null);
    assertTrue(element.equals(element2));
    
    element = new SpdzElement(null, BigInteger.valueOf(11), BigInteger.valueOf(13));
    element2 = new SpdzElement(BigInteger.valueOf(25), BigInteger.valueOf(11), BigInteger.valueOf(13));
    assertFalse(element.equals(element2));
    element2 = new SpdzElement(null, BigInteger.valueOf(11), BigInteger.valueOf(13));
    assertTrue(element.equals(element2));
    element = new SpdzElement(BigInteger.valueOf(25), BigInteger.valueOf(11), BigInteger.valueOf(13));
    assertFalse(element.equals(element2));
  }
  
}
