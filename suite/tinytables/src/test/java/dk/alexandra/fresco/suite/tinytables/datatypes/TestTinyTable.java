package dk.alexandra.fresco.suite.tinytables.datatypes;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestTinyTable {

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorTooManyArguments() {
    TinyTablesElement e = TinyTablesElement.getInstance(false);
    new TinyTable(new TinyTablesElement[] { e, e, e, e, e });
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorTooFewArguments() {
    TinyTablesElement e = TinyTablesElement.getInstance(false);
    new TinyTable(new TinyTablesElement[] { e, e });
  }

  @Test
  public void testToString() {
    TinyTablesElement e = TinyTablesElement.getInstance(false);
    TinyTable table = new TinyTable(new TinyTablesElement[] { e, e, e, e });
    assertThat(table.toString(), is(String.format("[[%s, %s], [%s, %s]]", e, e, e, e)));
  }

}
