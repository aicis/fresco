package dk.alexandra.fresco.suite.tinytables.datatypes;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class TestTinyTablesElement {

  @Test
  public void testToString() {
    TinyTablesElement e1 = TinyTablesElement.getTinyTablesElement(false);
    TinyTablesElement e2 = TinyTablesElement.getTinyTablesElement(true);
    assertThat(e1.toString(), is("TinyTablesElement:false"));
    assertThat(e2.toString(), is("TinyTablesElement:true"));
  }

}
