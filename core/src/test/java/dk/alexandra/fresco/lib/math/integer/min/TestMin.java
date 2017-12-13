package dk.alexandra.fresco.lib.math.integer.min;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class TestMin {

  @Test(expected = IllegalArgumentException.class)
  public void testMinimumProtocolTooShort(){
    List<DRes<SInt>> inputs = new ArrayList<>();
    inputs.add(new DummyArithmeticSInt(2));
    new Minimum(inputs);
  }

  @Test(expected = RuntimeException.class)
  public void testMinInfFracProtocolInconsistent1(){
    List<DRes<SInt>> inputN = new ArrayList<>();
    inputN.add(new DummyArithmeticSInt(2));
    inputN.add(new DummyArithmeticSInt(2));
    List<DRes<SInt>> inputD = new ArrayList<>();
    inputD.add(new DummyArithmeticSInt(2));
    inputD.add(new DummyArithmeticSInt(2));
    inputD.add(new DummyArithmeticSInt(2));
    List<DRes<SInt>> inputI = new ArrayList<>();
    inputI.add(new DummyArithmeticSInt(2));
    inputI.add(new DummyArithmeticSInt(2));
    inputI.add(new DummyArithmeticSInt(2));
    new MinInfFrac(inputN, inputD, inputI);
  }

  @Test(expected = RuntimeException.class)
  public void testMinInfFracProtocolInconsistent2(){
    List<DRes<SInt>> inputN = new ArrayList<>();
    inputN.add(new DummyArithmeticSInt(2));
    inputN.add(new DummyArithmeticSInt(2));
    inputN.add(new DummyArithmeticSInt(2));
    List<DRes<SInt>> inputD = new ArrayList<>();
    inputD.add(new DummyArithmeticSInt(2));
    inputD.add(new DummyArithmeticSInt(2));
    inputD.add(new DummyArithmeticSInt(2));
    List<DRes<SInt>> inputI = new ArrayList<>();
    inputI.add(new DummyArithmeticSInt(2));
    inputI.add(new DummyArithmeticSInt(2));
    new MinInfFrac(inputN, inputD, inputI);
  }
}
