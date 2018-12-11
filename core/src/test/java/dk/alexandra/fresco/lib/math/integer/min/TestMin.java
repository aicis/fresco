package dk.alexandra.fresco.lib.math.integer.min;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.FieldElementBigInteger;
import dk.alexandra.fresco.framework.builder.numeric.ModulusBigInteger;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class TestMin {

  private ModulusBigInteger modulus = new ModulusBigInteger(10);

  @Test(expected = IllegalArgumentException.class)
  public void testMinimumProtocolTooShort() {
    List<DRes<SInt>> inputs = new ArrayList<>();
    inputs.add(createSInt(2));
    new Minimum(inputs);
  }

  @Test(expected = RuntimeException.class)
  public void testMinInfFracProtocolInconsistent1() {
    List<DRes<SInt>> inputN = new ArrayList<>();
    inputN.add(createSInt(2));
    inputN.add(createSInt(2));
    List<DRes<SInt>> inputD = new ArrayList<>();
    inputD.add(createSInt(2));
    inputD.add(createSInt(2));
    inputD.add(createSInt(2));
    List<DRes<SInt>> inputI = new ArrayList<>();
    inputI.add(createSInt(2));
    inputI.add(createSInt(2));
    inputI.add(createSInt(2));
    new MinInfFrac(inputN, inputD, inputI);
  }

  private DummyArithmeticSInt createSInt(int value) {
    return new DummyArithmeticSInt(new FieldElementBigInteger(value, modulus));
  }

  @Test(expected = RuntimeException.class)
  public void testMinInfFracProtocolInconsistent2() {
    List<DRes<SInt>> inputN = new ArrayList<>();
    inputN.add(createSInt(2));
    inputN.add(createSInt(2));
    inputN.add(createSInt(2));
    List<DRes<SInt>> inputD = new ArrayList<>();
    inputD.add(createSInt(3));
    inputD.add(createSInt(3));
    inputD.add(createSInt(3));
    List<DRes<SInt>> inputI = new ArrayList<>();
    inputI.add(createSInt(2));
    inputI.add(createSInt(2));
    new MinInfFrac(inputN, inputD, inputI);
  }
}
