package dk.alexandra.fresco.suite.tinytables.datatypes;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestTinyTablesTriple {

  @Test
  public void testToString() {
    TinyTablesElement e = TinyTablesElement.getInstance(true);
    TinyTablesTriple trip = TinyTablesTriple.fromShares(e.getShare(), e.getShare(), e.getShare());
    assertThat(trip.toString(), is("TinyTablesTriple[" + e + "," + e + "," + e + "]"));
  }

}
