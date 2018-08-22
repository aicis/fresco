package dk.alexandra.fresco.suite.tinytables.storage;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesTriple;
import dk.alexandra.fresco.suite.tinytables.util.TinyTablesTripleGenerator;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class TestBatchTinyTablesTripleProvider {

  @Test
  public void testNextTriple() {
    TinyTablesTripleGenerator fac = new TinyTablesTripleGenerator(0, null, null) {

      @Override
      public List<TinyTablesTriple> generate(int amount) {
        List<TinyTablesTriple> triples = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
          triples.add(TinyTablesTriple.fromShares(true, true, true));
        }
        return triples;
      }
    };
    BatchTinyTablesTripleProvider gen = new BatchTinyTablesTripleProvider(fac, 10);
    for (int i = 0; i < 20; i++) {
      assertThat(gen.getNextTriple(), is(TinyTablesTriple.fromShares(true, true, true)));
    }
  }

}
