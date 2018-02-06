package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 *  Given two values, produce a sorted list of the values 
 *
 */
public class CompareAndSwap implements Computation<List<List<DRes<SBool>>>, ProtocolBuilderBinary> {

  private List<DRes<SBool>> left;
  private List<DRes<SBool>> right;

  public CompareAndSwap(List<DRes<SBool>> left, List<DRes<SBool>> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public DRes<List<List<DRes<SBool>>>> buildComputation(ProtocolBuilderBinary builder) {
    return builder.seq(seq -> {
      return seq.comparison().greaterThan(right, left);
    }).par((par, data) -> {
     
      List<DRes<SBool>> first = left.stream()
          .map(e -> {return par.advancedBinary().condSelect(data, e, right.get(left.indexOf(e)));})
          .collect(Collectors.toList());

      List<DRes<SBool>> second = right.stream()
          .map(e -> {return par.advancedBinary().condSelect(data, e, left.get(right.indexOf(e)));})
          .collect(Collectors.toList());

      List<List<DRes<SBool>>> result = new ArrayList<List<DRes<SBool>>>();
      result.add(first);
      result.add(second);
      return () -> result; 
    });
  }
}
