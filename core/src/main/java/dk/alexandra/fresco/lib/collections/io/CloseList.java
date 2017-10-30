package dk.alexandra.fresco.lib.collections.io;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.ComputationParallel;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CloseList implements ComputationParallel<List<DRes<SInt>>, ProtocolBuilderNumeric> {

  private final List<BigInteger> openInputs;
  private final int numberOfInputs;
  private final int inputParty;
  private final boolean isInputProvider;

  /**
   * See {@link dk.alexandra.fresco.framework.builder.numeric.Collections#closeList(List, int)
   * closeList}.
   */
  public CloseList(List<BigInteger> openInputs, int inputParty) {
    super();
    this.openInputs = openInputs;
    this.numberOfInputs = openInputs.size();
    this.inputParty = inputParty;
    this.isInputProvider = true;
  }

  /**
   * See {@link dk.alexandra.fresco.framework.builder.numeric.Collections#closeList(int, int)
   * closeList}.
   */
  public CloseList(int numberOfInputs, int inputParty) {
    super();
    this.openInputs = new ArrayList<>();
    this.numberOfInputs = numberOfInputs;
    this.inputParty = inputParty;
    this.isInputProvider = false;
  }

  private List<DRes<SInt>> buildAsProvider(Numeric nb) {
    return openInputs.stream().map(openInput -> nb.input(openInput, inputParty))
        .collect(Collectors.toList());
  }

  private List<DRes<SInt>> buildAsReceiver(Numeric nb) {
    List<DRes<SInt>> closed = new ArrayList<>();
    for (int i = 0; i < numberOfInputs; i++) {
      closed.add(nb.input(null, inputParty));
    }
    return closed;
  }

  @Override
  public DRes<List<DRes<SInt>>> buildComputation(ProtocolBuilderNumeric builder) {
    Numeric nb = builder.numeric();
    // for each input value, call input
    List<DRes<SInt>> closedInputs = isInputProvider ? buildAsProvider(nb) : buildAsReceiver(nb);
    return () -> closedInputs;
  }
}
