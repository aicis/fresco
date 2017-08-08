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
package dk.alexandra.fresco.lib.debug;

import java.io.PrintStream;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;

//TODO refactor into new architecture
public class BinaryOpenAndPrint implements ProtocolProducer {

  private final SBool[] string;
  private OBool[] oString = null;
  private final PrintStream output;

  private int round = 0;
  private final String label;

  ProtocolProducer pp = null;

  private final BasicLogicFactory factory;


  public BinaryOpenAndPrint(String label, SBool[] string, BasicLogicFactory factory, PrintStream output) {
    this.string = string;
    this.factory = factory;
    this.label = label;
    this.output = output;
  }

  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (pp == null) {
      if (round == 0) {
          oString = new OBool[string.length];
          SequentialProtocolProducer seq = new SequentialProtocolProducer();
          for (int i = 0; i < string.length; i++) {
            oString[i] = factory.getOBool();
            seq.append(factory.getOpenProtocol(string[i], oString[i]));
          }
          pp = seq;
      } else {
        StringBuilder sb = new StringBuilder();
        sb.append(label);
        sb.append('\n');
        for (OBool entry : oString) {
          if (entry.getValue()) {
            sb.append(1);
          } else {
            sb.append(0);
          }
        }
        pp = new MarkerProtocolImpl(sb.toString(), output);
      }
    }
    
    if (pp.hasNextProtocols()) {
      pp.getNextProtocols(protocolCollection);
    } else {
      if(round == 0){
          round = 1;
          pp = null;
      }else {
          round = 2;
          pp = null;
      }
    }
  }

  @Override
  public boolean hasNextProtocols() {
    return round != 2;
  }
}
