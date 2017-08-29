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
import java.math.BigInteger;

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
   * Appends a protocol to input a single BigInteger
   *
   * @param i the BigInteger value
   * @param targetID the party to input
   * @return the SInt to be loaded with the input
   */
  public SInt input(BigInteger i, int targetID) {
    SInt si = sif.getSInt();
    append(iof.getCloseProtocol(targetID, i, si));
    return si;
  }

  /**
   * Appends a protocol to input a single BigInteger
   *
   * @param i the integer value
   * @param targetID the party to input
   * @return the SInt to be loaded with the input
   */
  public SInt input(int i, int targetID) {
    SInt si = sif.getSInt();
    append(iof.getCloseProtocol(targetID, BigInteger.valueOf(i), si));
    return si;
  }

  /**
   * Appends a protocol to input a single value from an other party. I.e., the value is not given.
   *
   * @param targetID the id of the party inputting.
   * @return SInt to be loaded with the input.
   */
  public SInt input(int targetID) {
    SInt si = sif.getSInt();
    append(iof.getCloseProtocol(targetID, null, si));
    return si;
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
