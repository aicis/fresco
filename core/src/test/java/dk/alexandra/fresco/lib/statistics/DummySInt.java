package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.framework.value.SInt;

@SuppressWarnings("serial")
class DummySInt implements SInt {

  public DummySInt() {

  }

  @Override
  public byte[] getSerializableContent() {
    return null;
  }

  @Override
  public void setSerializableContent(byte[] val) {
  }

  @Override
  public boolean isReady() {
    return false;
  }

  @Override
  public SInt out() {
    return this;
  }
}
