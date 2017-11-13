package dk.alexandra.fresco.lib.math.bool.add;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.List;


/**
 * This class implements a Full Adder protocol for Binary protocols.
 * It takes the naive approach of linking 1-Bit-Full Adders together to implement
 * a generic length adder.
 */
public class FullAdder 
    implements Computation<List<DRes<SBool>>, ProtocolBuilderBinary> {

  private List<DRes<SBool>> lefts, rights;
  private DRes<SBool> inCarry;

  public FullAdder(List<DRes<SBool>> lefts, 
      List<DRes<SBool>> rights, 
      DRes<SBool> inCarry) {
    
    if (lefts.size() != rights.size()) {
      throw new IllegalArgumentException("input lists for Full Adder must be of same length.");
    }
    this.lefts = lefts;
    this.rights = rights;
    this.inCarry = inCarry;
  }
  
  
  @Override
  public DRes<List<DRes<SBool>>> buildComputation(ProtocolBuilderBinary builder) {

    List<DRes<SBool>> result = new ArrayList<DRes<SBool>>(); 
    
    return builder.seq(seq -> {
      int idx = this.lefts.size() -1;
      IterationState is = new IterationState(idx, seq.advancedBinary().oneBitFullAdder(lefts.get(idx), rights.get(idx), inCarry));
      return is;
    }).whileLoop(
        (state) -> state.round >= 1,
        (seq, state) -> {
          int idx = state.round -1;
          
          result.add(0, state.value.out().getFirst());
          IterationState is = new IterationState(idx, seq.advancedBinary().oneBitFullAdder(lefts.get(idx), rights.get(idx), state.value.out().getSecond()));
          return is;
        }
    ).seq((seq, state) -> {
      result.add(0, state.value.out().getFirst());
      result.add(0, state.value.out().getSecond());
      return () -> result;
      }
    );
  }
  
  private static final class IterationState implements DRes<IterationState> {

    private int round;
    private final DRes<Pair<SBool, SBool>> value;

    private IterationState(int round,
        DRes<Pair<SBool, SBool>> value) {
      this.round = round;
      this.value = value;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }
}
