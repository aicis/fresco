package dk.alexandra.fresco.suite.tinytables.datatypes;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

import org.junit.Test;

public class TestTinyTable {

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorTooManyArguments() {
    TinyTablesElement e = TinyTablesElement.getTinyTablesElement(false);
    new TinyTable(new TinyTablesElement[] { e, e, e, e, e });
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorTooFewArguments() {
    TinyTablesElement e = TinyTablesElement.getTinyTablesElement(false);
    new TinyTable(new TinyTablesElement[] { e, e, e, e, e });
  }

  @Test
  public void testToString() {
    TinyTablesElement e = TinyTablesElement.getTinyTablesElement(false);
    TinyTable table = new TinyTable(new TinyTablesElement[] { e, e, e, e });
    assertThat(table.toString(), is(
        "[[TinyTablesElement:false, TinyTablesElement:false], "
        + "[TinyTablesElement:false, TinyTablesElement:false]]"));
  }

}
