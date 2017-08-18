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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilderParallel;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.ParallelNumericBuilder;
import dk.alexandra.fresco.framework.value.SInt;

/**
 * Implements a close operation on a list of BigIntegers.
 */
public class CloseList implements ComputationBuilderParallel<List<Computation<SInt>>> {

  private final List<BigInteger> openInputs;
  private final int numberOfInputs;
  private final int inputParty;
  private final boolean isInputProvider;

  /**
   * Makes a new CloseList.
   * 
   * This should be called by the party providing input.
   *
   * @param openInputs the inputs to close.
   */
  public CloseList(List<BigInteger> openInputs, int inputParty) {
    super();
    this.openInputs = openInputs;
    this.numberOfInputs = openInputs.size();
    this.inputParty = inputParty;
    this.isInputProvider = true;
  }

  /**
   * Makes a new CloseList.
   * 
   * This should be called by parties not providing input.
   *
   * @param openInputs the inputs to close.
   */
  public CloseList(int numberOfInputs, int inputParty) {
    super();
    this.openInputs = new ArrayList<>();
    this.numberOfInputs = numberOfInputs;
    this.inputParty = inputParty;
    this.isInputProvider = false;
  }

  private List<Computation<SInt>> buildAsProvider(NumericBuilder nb) {
    return openInputs.stream().map(openInput -> nb.input(openInput, inputParty))
        .collect(Collectors.toList());
  }
  
  private List<Computation<SInt>> buildAsReceiver(NumericBuilder nb) {
    List<Computation<SInt>> closed = new ArrayList<>();
    for (int i = 0; i < numberOfInputs; i++) {
      closed.add(nb.input(null, inputParty));
    }
    return closed;
  }

  @Override
  public Computation<List<Computation<SInt>>> build(ParallelNumericBuilder par) {
    NumericBuilder nb = par.numeric();
    // for each input value, call input
    List<Computation<SInt>> closedInputs =
        isInputProvider ? buildAsProvider(nb) : buildAsReceiver(nb);
    return () -> closedInputs;
  }
}
