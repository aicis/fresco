package dk.alexandra.fresco.tools.bitTriples.elements;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import org.junit.Assert;
import org.junit.Test;

public class MultiplicationTripleTest {

  private MultiplicationTriple makeTriple() {
    return new MultiplicationTriple(
        new AuthenticatedElement(true, new StrictBitVector(8)),
        new AuthenticatedElement(true, new StrictBitVector(8)),
        new AuthenticatedElement(true, new StrictBitVector(8)));
  }

  @Test
  public void testToString() {
    System.out.println(makeTriple().toString());
    Assert.assertTrue(makeTriple().toString().contains("MultiplicationTriple"));
    Assert.assertTrue(
        makeTriple()
            .toString()
            .contains("left=AuthenticatedElement [share=true, mac=StrictBitVector [bits=[0]]]"));
    Assert.assertTrue(
        makeTriple()
            .toString()
            .contains("right=AuthenticatedElement [share=true, mac=StrictBitVector [bits=[0]]]"));
    Assert.assertTrue(
        makeTriple()
            .toString()
            .contains("product=AuthenticatedElement [share=true, mac=StrictBitVector [bits=[0]]]"));
  }

  @Test
  public void add() {
    MultiplicationTriple sum = makeTriple().add(makeTriple());
    Assert.assertFalse(sum.getLeft().getBit());
    Assert.assertFalse(sum.getRight().getBit());
    Assert.assertFalse(sum.getProduct().getBit());
  }
}
