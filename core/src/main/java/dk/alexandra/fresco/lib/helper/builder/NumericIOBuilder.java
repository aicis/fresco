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
package dk.alexandra.fresco.lib.helper.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.SIntFactory;
import dk.alexandra.fresco.lib.field.integer.generic.IOIntProtocolFactory;
import dk.alexandra.fresco.lib.helper.AbstractRepeatProtocol;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * A builder handling input/output related protocols for protocol suites supporting arithmetic.
 *
 * @author psn
 */
public class NumericIOBuilder extends AbstractProtocolBuilder {

  private IOIntProtocolFactory iof;
  private SIntFactory sif;

  /**
   * A convenient constructor when one factory implements all the needed interfaces (which will
   * usually be the case)
   *
   * @param factory a factory providing SInt/BigInteger and input/output ciruicts.
   */
  public <T extends IOIntProtocolFactory & SIntFactory> NumericIOBuilder(T factory) {
    super();
    this.iof = factory;
    this.sif = factory;
  }

  /**
   * Appends a protocol to input a matrix of BigIntegers.
   *
   * @param is the BigInteger values
   * @param targetID the party to input
   * @return SInt's that will be loaded with the corresponding inputs, by the appended protocol.
   */
  public SInt[][] inputMatrix(BigInteger[][] is, int targetID) {
    SInt[][] sis = new SInt[is.length][is[0].length];
    beginParScope();
    for (int i = 0; i < is.length; i++) {
      sis[i] = inputArray(is[i], targetID);
    }
    endCurScope();
    return sis;
  }

  /**
   * Appends a protocol to input a array of BigIntegers.
   *
   * @param is the BigInteger values
   * @param targetID the party to input
   * @return SInt's that will be loaded with the corresponding inputs, by the appended protocol.
   */
  public SInt[] inputArray(BigInteger[] is, int targetID) {
    SInt[] sis = new SInt[is.length];
    for (int i = 0; i < sis.length; i++) {
      sis[i] = sif.getSInt();
    }
    append(new InputArray(is, sis, targetID));
    return sis;
  }

  /**
   * A class to efficiently handle large amounts of inputs.
   *
   * @author psn
   */
  private class InputArray extends AbstractRepeatProtocol {

    BigInteger[] is;
    SInt[] sis;
    int length;
    int targetID;
    int i = 0;

    InputArray(BigInteger[] is, SInt[] sis, int targetID) {
      if (is.length != sis.length) {
        throw new IllegalArgumentException("Array dimensions do not match.");
      }
      this.is = is;
      this.length = sis.length;
      this.sis = sis;
      this.targetID = targetID;
    }

    @Override
    protected ProtocolProducer getNextProtocolProducer() {
      NativeProtocol<SInt, ?> input = null;
      if (i < length) {
        BigInteger oi = null;
        if (is != null) {
          oi = is[i];
        }
        input = iof.getCloseProtocol(targetID, oi, sis[i]);
        i++;
        return new SingleProtocolProducer<>(input);
      } else {
        return null;
      }

    }
  }

  /**
   * Appends a protocol to open an array of SInts. Output should be given to all parties.
   *
   * @param sis SInts to open
   * @return the OInts to be loaded with the opened SInts
   */
  public List<Computation<BigInteger>> outputArray(SInt sis[]) {
    OutputArray pp = new OutputArray(sis);
    append(pp);
    return pp.ois;
  }

  /**
   * A class to efficiently handle large amounts of outputs.
   *
   * @author psn
   */
  private class OutputArray extends AbstractRepeatProtocol {

    List<Computation<BigInteger>> ois;
    SInt[] sis;
    int i = 0;

    OutputArray(SInt[] sis) {
      this.sis = sis;
      ois = new ArrayList<>(sis.length);
    }

    @Override
    protected ProtocolProducer getNextProtocolProducer() {
      NativeProtocol<BigInteger, ?> output = null;
      if (i < sis.length) {
        output = iof.getOpenProtocol(sis[i]);
        ois.add(output);
        i++;
        return new SingleProtocolProducer<>(output);
      } else {
        return null;
      }
    }
  }

  /**
   * Appends a protocol to open a single SInt. Output should be given to all parties.
   *
   * @param si SInt to open
   * @return the BigInteger to be loaded with the opened SInt
   */
  public Computation<BigInteger> output(SInt si) {
    NativeProtocol<BigInteger, ?> openProtocol = iof.getOpenProtocol(si);
    append(openProtocol);
    return openProtocol;
  }

  @Override
  public void addProtocolProducer(ProtocolProducer gp) {
    append(gp);
  }
}
