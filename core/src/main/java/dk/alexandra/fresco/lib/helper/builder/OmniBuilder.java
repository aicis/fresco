/*
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

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.value.SIntFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.generic.IOIntProtocolFactory;

/**
 * This builder can be used for all possible functionality in FRESCO. It encapsulates all other
 * builders and creates them lazily. This means that application creators are not forced to create
 * all sorts of factories and cast the factory manually. This is done for the user whenever
 * requested.
 *
 * Note that using this builder does not mean that the application will always be sound. It still
 * requires that the underlying protocol suite used supports the factory methods that you use.
 *
 * This builder is the parent of all it's builders, meaning that all builders use this builders
 * internal protocol stack. Thus, all calls on any builder that adds protocols or new scopes will
 * use this builders stack. This means an application development user just writes the application
 * linearly without thinking about which builder stack is now used.
 *
 * @author Kasper
 */
public class OmniBuilder extends AbstractProtocolBuilder {

  private ProtocolFactory factory;
  private NumericIOBuilder numericIOBuilder;
  private NumericProtocolBuilder numericProtocolBuilder;

  //Used in various protocols - typically for comparisons.
  //TODO: Better explanation as to what this is, and what it means for performance/security.

  public OmniBuilder(BuilderFactoryNumeric factory) {
    this.factory = factory.getProtocolFactory();
  }

  /**
   * Builder used for inputting and outputting values. Note that inputting values using this builder
   * often requires at least two different applications or a switch on the party ID. Currently
   * expects that the constructor given factory implements all interfaces listed below: -
   * IOIntProtocolFactory - SIntFactory - OIntFactory
   *
   * Which is a subset of the BasicNumericFactory interface.
   */
  public NumericIOBuilder getNumericIOBuilder() {
    if (numericIOBuilder == null) {
      numericIOBuilder = new NumericIOBuilder(
          (IOIntProtocolFactory & SIntFactory) factory);
      numericIOBuilder.setParentBuilder(this);
    }
    return numericIOBuilder;
  }


  /**
   * Builder used for basic numeric functionality such as add, mult and sub.
   * Currently expects that the constructor given factory implements all interfaces listed below:
   * - BasicNumericFactory
   */
  public NumericProtocolBuilder getNumericProtocolBuilder() {
    if (numericProtocolBuilder == null) {
      numericProtocolBuilder = new NumericProtocolBuilder((BasicNumericFactory) factory);
      numericProtocolBuilder.setParentBuilder(this);
    }
    return numericProtocolBuilder;
  }

  @Override
  public void addProtocolProducer(ProtocolProducer pp) {
    append(pp);
  }

}
