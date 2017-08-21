package dk.alexandra.fresco.suite.dummy.bool;

import dk.alexandra.fresco.framework.builder.binary.BasicBinaryFactory;
import dk.alexandra.fresco.framework.value.SBool;

public class DummyBooleanFactory implements BasicBinaryFactory {

  @Override
  public SBool getSBool() {
    return new DummyBooleanSBool();
  }

  @Override
  public SBool getKnownConstantSBool(boolean b) {
    return new DummyBooleanSBool(b);
  }

}
