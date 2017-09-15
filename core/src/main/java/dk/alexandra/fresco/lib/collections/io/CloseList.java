/*
 * Copyright (c) 2015, 2016, 2107 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/

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

  public CloseList(List<BigInteger> openInputs, int inputParty) {
    super();
    this.openInputs = openInputs;
    this.numberOfInputs = openInputs.size();
    this.inputParty = inputParty;
    this.isInputProvider = true;
  }

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
