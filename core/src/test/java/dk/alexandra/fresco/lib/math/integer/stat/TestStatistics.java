package dk.alexandra.fresco.lib.math.integer.stat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;

public class TestStatistics {


  
  @Test (expected = IllegalArgumentException.class)
  public void testCovarianceBadLength() {
    List<Computation<SInt>> input1 = new ArrayList<Computation<SInt>>();
    input1.add(new DummyArithmeticSInt(2));
    input1.add(new DummyArithmeticSInt(2));
    List<Computation<SInt>> input2 = new ArrayList<Computation<SInt>>();
    input2.add(new DummyArithmeticSInt(2));
    input2.add(new DummyArithmeticSInt(2));
    input2.add(new DummyArithmeticSInt(2));
    new Covariance(input1, input2);
    
    Assert.fail("Should not be reachable.");
  }

  @Test (expected = IllegalArgumentException.class)
  public void testCovarianceMatrixBadLength() {
    List<Computation<SInt>> input1 = new ArrayList<Computation<SInt>>();
    input1.add(new DummyArithmeticSInt(2));
    input1.add(new DummyArithmeticSInt(2));
    List<Computation<SInt>> input2 = new ArrayList<Computation<SInt>>();
    input2.add(new DummyArithmeticSInt(2));
    input2.add(new DummyArithmeticSInt(2));
    input2.add(new DummyArithmeticSInt(2));
    new CovarianceMatrix(Arrays.asList(input1, input2));
    Assert.fail("Should not be reachable.");
  }
  
}
