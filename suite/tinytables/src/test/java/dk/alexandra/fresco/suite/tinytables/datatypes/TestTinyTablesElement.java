package dk.alexandra.fresco.suite.tinytables.datatypes;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestTinyTablesElement {

  @Test
  public void testToString() {
    TinyTablesElement e1 = TinyTablesElement.getInstance(false);
    TinyTablesElement e2 = TinyTablesElement.getInstance(true);
    assertThat(e1.toString(), is("TinyTablesElement[share=false]"));
    assertThat(e2.toString(), is("TinyTablesElement[share=true]"));
  }

}
