package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.builder.binary.BasicBinaryFactory;
import dk.alexandra.fresco.framework.value.SBool;

public class DummyFactory implements BasicBinaryFactory, ProtocolFactory {

  private int counter = 0;

  @Override
  public SBool getSBool() {
    return new DummySBool("" + counter++);
  }

  @Override
  public SBool[] getSBools(int amount) {
    SBool[] res = new SBool[amount];
    for (int i = 0; i < amount; i++) {
      res[i] = getSBool();
    }
    return res;
  }

  @Override
  public SBool getKnownConstantSBool(boolean b) {
    return new DummySBool("" + counter++, b);
  }

  @Override
  public SBool[] getKnownConstantSBools(boolean[] bools) {
    SBool[] res = new SBool[bools.length];
    for (int i = 0; i < bools.length; i++) {
      res[i] = getKnownConstantSBool(bools[i]);
    }
    return res;
  }

}
