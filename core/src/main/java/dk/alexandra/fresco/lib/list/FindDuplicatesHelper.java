package dk.alexandra.fresco.lib.list;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComparisonBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * @author mortenvchristiansen
 */
public class FindDuplicatesHelper {

  public FindDuplicatesHelper() {
  }

  private Computation<SInt> or(ProtocolBuilderNumeric builder, Computation<SInt> a,
      Computation<SInt> b) {
    NumericBuilder numeric = builder.numeric();
    Computation<SInt> product = numeric.mult(a, b);
    Computation<SInt> add = numeric.add(a, b);
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
      builder.createSequentialSub(seq -> {
        ComparisonBuilder comparison = seq.comparison();
        for (int j = 0; j < list2.size(); j++) {
          Computation<SInt> equals = comparison.equals(list1.getId(finalI), list2.getId(j));
          list1.setDuplicate(finalI,
              or(seq, list1.getDuplicate(finalI), equals));
        }
        return () -> null;
      });
    }
  }


}
