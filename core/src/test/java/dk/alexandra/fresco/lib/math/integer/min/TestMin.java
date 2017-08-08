package dk.alexandra.fresco.lib.math.integer.min;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticFactory;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;

public class TestMin {

  BasicNumericFactory factory = new DummyArithmeticFactory(new BigInteger("23"), 8);
  
  @Test(expected = IllegalArgumentException.class)
  public void testMinimumProtocolTooShort(){
    List<Computation<SInt>> inputs = new ArrayList<Computation<SInt>>();
    inputs.add(new DummyArithmeticSInt(2));
    new Minimum(inputs);
  }

  @Test(expected = MPCException.class)
  public void testMinInfFracProtocolInconsistent1(){
    List<Computation<SInt>> inputN = new ArrayList<Computation<SInt>>();
    inputN.add(new DummyArithmeticSInt(2));
    inputN.add(new DummyArithmeticSInt(2));
    List<Computation<SInt>> inputD = new ArrayList<Computation<SInt>>();
    inputD.add(new DummyArithmeticSInt(2));
    inputD.add(new DummyArithmeticSInt(2));
    inputD.add(new DummyArithmeticSInt(2));
    List<Computation<SInt>> inputI = new ArrayList<Computation<SInt>>();
    inputI.add(new DummyArithmeticSInt(2));
    inputI.add(new DummyArithmeticSInt(2));
    inputI.add(new DummyArithmeticSInt(2));
    new MinInfFrac(inputN, inputD, inputI);
  }
  
  @Test(expected = MPCException.class)
  public void testMinInfFracProtocolInconsistent2(){
    List<Computation<SInt>> inputN = new ArrayList<Computation<SInt>>();
    inputN.add(new DummyArithmeticSInt(2));
    inputN.add(new DummyArithmeticSInt(2));
    inputN.add(new DummyArithmeticSInt(2));
    List<Computation<SInt>> inputD = new ArrayList<Computation<SInt>>();
    inputD.add(new DummyArithmeticSInt(2));
    inputD.add(new DummyArithmeticSInt(2));
    inputD.add(new DummyArithmeticSInt(2));
    List<Computation<SInt>> inputI = new ArrayList<Computation<SInt>>();
    inputI.add(new DummyArithmeticSInt(2));
    inputI.add(new DummyArithmeticSInt(2));
    new MinInfFrac(inputN, inputD, inputI);
  }
}
