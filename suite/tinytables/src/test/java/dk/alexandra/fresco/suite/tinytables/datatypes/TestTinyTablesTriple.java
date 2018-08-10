package dk.alexandra.fresco.suite.tinytables.datatypes;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestTinyTablesTriple {

  @Test
  public void testToString() {
    TinyTablesTriple trip = TinyTablesTriple.fromShares(true, true, true);
    assertThat(trip.toString(),
        is("TinyTablesTriple:(TinyTablesElement:true,"
            + "TinyTablesElement:true,TinyTablesElement:true)"));
  }

}
