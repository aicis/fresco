package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz.gates.SpdzNativeProtocol;
import java.util.LinkedList;
import java.util.List;

public class SpdzExponentiationPipeProtocol extends SpdzNativeProtocol<List<DRes<SInt>>> {

  private List<DRes<SInt>> result;
  private int pipeLength;
  
  public SpdzExponentiationPipeProtocol(int pipeLength) {
    this.pipeLength = pipeLength;
  }
  
  @Override
  public EvaluationStatus evaluate(int round, SpdzResourcePool resourcePool, Network network) {
    SInt[] pipe = resourcePool.getStore().getSupplier().getNextExpPipe();
    if(pipe.length < pipeLength+1) {
      throw new MPCException(
          "Preprocessed exponentiation pipe is not long enough. Create an exp pipe which has the required length of "
              + (pipeLength + 1)
              + ", or use the default protocol for generating the exponentiation pipes online");
    }
    this.result = new LinkedList<>();
    for(int i = 0; i < pipeLength+1; i++) {
      SInt r = pipe[i];
      this.result.add(() -> r);
    }
    return EvaluationStatus.IS_DONE;
  }

  @Override
  public List<DRes<SInt>> out() {
    return result;
  }
}
