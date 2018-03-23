package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt128;
import org.junit.Test;

public class TestSpdz2kBuilder {

  @Test(expected = UnsupportedOperationException.class)
  public void getBigIntegerHelper() {
    new Spdz2kBuilder<CompUInt128>(null, null).getBigIntegerHelper();
  }

}
