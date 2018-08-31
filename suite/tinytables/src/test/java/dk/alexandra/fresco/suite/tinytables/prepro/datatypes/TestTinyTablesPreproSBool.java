package dk.alexandra.fresco.suite.tinytables.prepro.datatypes;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import org.junit.Test;

public class TestTinyTablesPreproSBool {

  @Test
  public void testToString() {
    TinyTablesElement e = TinyTablesElement.getInstance(true);
    TinyTablesPreproSBool sb =
        new TinyTablesPreproSBool(e);
    assertThat(sb.toString(), is(String.format("TinyTablesPreproSBool[value=%s]", e)));
  }

}
