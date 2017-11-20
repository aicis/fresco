package dk.alexandra.fresco.lib.list;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.Comparison;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Helper class for the find duplicates functionality.
 */
public class FindDuplicatesHelper {

  public FindDuplicatesHelper() {
  }

  private DRes<SInt> or(ProtocolBuilderNumeric builder, DRes<SInt> a,
      DRes<SInt> b) {
    Numeric numeric = builder.numeric();
    DRes<SInt> product = numeric.mult(a, b);
    DRes<SInt> add = numeric.add(a, b);
    return numeric.sub(add, product);
  }

  /**
   * annotates list1 with duplicate marks. To annotate list 2 also, run with the lists switched If a
   * horisontal join is desired, make sure that both lists are ordered initially, annotate both
   * lists and update fields by going through tables in lockstep.
   */
  public void findDuplicates(ProtocolBuilderNumeric builder, SIntListofTuples list1,
      SIntListofTuples list2) {
    for (int i = 0; i < list1.size(); i++) {
      int finalI = i;
      builder.seq(seq -> {
        Comparison comparison = seq.comparison();
        for (int j = 0; j < list2.size(); j++) {
          DRes<SInt> equals = comparison.equals(list1.getId(finalI), list2.getId(j));
          list1.setDuplicate(finalI,
              or(seq, list1.getDuplicate(finalI), equals));
        }
        return null;
      });
    }
  }


}
