package dk.alexandra.fresco.suite.tinytables.datatypes;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestTinyTablesElementVector {

  @Test
  public void testGet() {
    byte[] shares = new byte[] { 0x00, 0x01 };
    TinyTablesElementVector vector = new TinyTablesElementVector(shares, Byte.SIZE * shares.length);
    for (int i = 0; i < Byte.SIZE * shares.length; i++) {
      assertThat(vector.get(i).getShare(), is(i == 8));
    }
  }

}
