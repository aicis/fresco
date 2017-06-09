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
package dk.alexandra.fresco.demo.inputsum;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;

/**
 * Tiny application for a two party case which computes the sum of the inputs,
 * and outputs the result.
 *
 * @author kasperdamgard
 */
public class SumAndOutputApplication implements Application {

  private static final long serialVersionUID = 4556762525827528147L;
  private InputApplication inputApp;
  private OInt output;

  public SumAndOutputApplication(InputApplication inputApp) {
    this.inputApp = inputApp;
  }

  @Override
  public ProtocolProducer prepareApplication(ProtocolFactory factory) {
    ProtocolProducer inputProtocol = inputApp.prepareApplication(factory);

    SInt[] ssInputs = inputApp.getSecretSharedInput();

    BasicNumericFactory fac = (BasicNumericFactory) factory;

    // create wire
    SInt sum = fac.getSInt();

    // create Sequence of protocols which eventually will compute the sum
    SequentialProtocolProducer sumProtocol = new SequentialProtocolProducer();

    sumProtocol.append(fac.getAddProtocol(ssInputs[0], ssInputs[1], sum));
    if (ssInputs.length > 2) {
      for (int i = 2; i < ssInputs.length; i++) {
        // Add sum and next secret shared input and store in sum.
        sumProtocol.append(fac.getAddProtocol(sum, ssInputs[i], sum));
      }
    }

    // create output wire
    this.output = fac.getOInt();
    NativeProtocol outputProtocol = fac.getOpenProtocol(sum,
        this.output);

    // Connect all protocols into a single protocol
    ProtocolProducer gp = new SequentialProtocolProducer(inputProtocol,
        sumProtocol, SingleProtocolProducer.wrap(outputProtocol));
    return gp;
  }

  public OInt getOutput() {
    return this.output;
  }
}