package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.framework.value.SInt;

class DummySInt implements SInt {

  public DummySInt() {

  }

  @Override
  public SInt out() {
    return this;
  }
}
