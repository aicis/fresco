package dk.alexandra.fresco.lib.collections;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.conditional.ConditionalSelect;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a lookup protocol using a linear number of equality protocols. A
 * lookup protocol is essentially a combination of a search and a conditional
 * select protocol. This does the search by simply comparing the lookup key to
 * all the keys in the list.
 * <p>
 * Guaranteed return value is the last value where the corresponding key matches
 * </p>
 */
public class LinearLookUp implements Computation<SInt, ProtocolBuilderNumeric> {

  private final DRes<SInt> lookUpKey;
  private final ArrayList<DRes<SInt>> keys;
  private final ArrayList<DRes<SInt>> values;
  private final DRes<SInt> notFoundValue;

  /**
   * Makes a new LinearLookUp
   *
   * @param lookUpKey the key to look up.
   * @param keys the list of keys to search among.
   * @param values the values corresponding to each key.
   * @param notFoundValue The value to return if not present.
   */
  public LinearLookUp(DRes<SInt> lookUpKey,
      ArrayList<DRes<SInt>> keys,
      ArrayList<DRes<SInt>> values,
      DRes<SInt> notFoundValue) {
    this.notFoundValue = notFoundValue;
    this.lookUpKey = lookUpKey;
    this.keys = keys;
    this.values = values;
  }

  @Override
  public DRes<SInt> buildComputation(ProtocolBuilderNumeric builder) {
    return builder.par((par) -> {
      int n = keys.size();
      List<DRes<SInt>> index = new ArrayList<>(n);
      for (DRes<SInt> key : keys) {
        index.add(par.comparison().equals(lookUpKey, key));
      }
      return () -> index;
    }).seq((seq, index) -> {
      DRes<SInt> outputValue = notFoundValue;
      for (int i = 0, valuesLength = values.size(); i < valuesLength; i++) {
        DRes<SInt> value = values.get(i);
        outputValue = seq.seq(new ConditionalSelect(index.get(i), value, outputValue));
      }
      return outputValue;
    });
  }
}