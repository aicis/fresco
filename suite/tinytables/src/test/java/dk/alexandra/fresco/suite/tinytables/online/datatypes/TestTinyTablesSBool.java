package dk.alexandra.fresco.suite.tinytables.online.datatypes;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import org.junit.Test;

public class TestTinyTablesSBool {

  @Test
  public void testToString() {
    TinyTablesElement falseElem = TinyTablesElement.getInstance(false);
    TinyTablesElement trueElem = TinyTablesElement.getInstance(true);
    TinyTablesSBool falseSBool = TinyTablesSBool.getInstance(falseElem);
    TinyTablesSBool trueSBool = TinyTablesSBool.getInstance(trueElem);
    assertThat(falseSBool.toString(), is("TinyTablesSBool[value=" + falseElem + "]"));
    assertThat(trueSBool.toString(), is("TinyTablesSBool[value=" + trueElem + "]"));
  }

}
