/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.demo;

import dk.alexandra.fresco.demo.helpers.DemoNumericApplication;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.BuildStep;
import dk.alexandra.fresco.framework.builder.numeric.NumericBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Demo application. Takes a number of inputs and converts them to secret shared
 * inputs by having party 1 input them all.
 *
 * @author Kasper Damgaard
 */
public class InputApplication extends DemoNumericApplication<List<SInt>> {

  private int[] inputs;
  private int length;

  public InputApplication(int[] inputs) {
    this.inputs = inputs;
    this.length = inputs.length;
  }

  public InputApplication(int length) {
    this.length = length;
  }

  @Override
  public Computation<List<SInt>> prepareApplication(ProtocolBuilderNumeric producer) {
    return createBuildStep(producer);
  }

  public BuildStep<ProtocolBuilderNumeric, List<SInt>, ?> createBuildStep(
      ProtocolBuilderNumeric producer) {
    return producer.par(par -> {
      NumericBuilder numeric = par.numeric();
      List<Computation<SInt>> result = new ArrayList<>(length);
      for (int i = 0; i < this.length; i++) {
        //create wires
        if (this.inputs != null) {
          result.add(numeric.input(BigInteger.valueOf(this.inputs[i]), 1));
        } else {
          result.add(numeric.input(null, 1));
        }
      }
      return () -> result.stream().map(Computation::out).collect(Collectors.toList());
    });
  }
}