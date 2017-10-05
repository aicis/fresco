package dk.alexandra.fresco.framework.builder.numeric;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.LinkedList;
import java.util.List;

/**
 * Default implementation which should only be used if for some reason preprocessing is not
 * possible.
 * 
 * @author Kasper Damgaard
 *
 */
public class DefaultPreprocessedValues implements PreprocessedValues {

  private ProtocolBuilderNumeric builder;
  
  public DefaultPreprocessedValues(ProtocolBuilderNumeric builder) {
    this.builder = builder;
  }
  
  @Override
  public DRes<List<DRes<SInt>>> getExponentiationPipe(int pipeLength) {
    return builder.seq(b -> {
      DRes<SInt> r = b.numeric().randomElement();
      DRes<SInt> r_inv = b.advancedNumeric().invert(r);
      LinkedList<DRes<SInt>> list = new LinkedList<>();
      list.add(r_inv);
      list.add(r);
      return () -> new IterationState(2, list);
    }).whileLoop(
      (state) -> state.round <= pipeLength+1, 
      (seq, state) -> {      
        DRes<SInt> r = state.value.get(1);
        DRes<SInt> last = state.value.getLast();
        state.value.add(seq.numeric().mult(last, r));
        state.round++;
        return state;
    }).seq((seq, state) -> {
      return () -> state.value;
    });    
  }

  private static final class IterationState implements DRes<IterationState> {

    private int round;
    private final LinkedList<DRes<SInt>> value;

    private IterationState(int round, LinkedList<DRes<SInt>> value) {
      this.round = round;
      this.value = value;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }
}
