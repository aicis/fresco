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
package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.SimpleProtocolProducer;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import java.util.Arrays;

public class RankProtocol extends SimpleProtocolProducer {

  private final SInt[] numerators;
  private final SInt[] denominators;
  private final SInt numerator;
  private final SInt denominator;
  private final SInt rank;
  private BasicNumericFactory numericFactory;
  private LPFactory lpFactory;

  public RankProtocol(SInt[] numerators, SInt[] denominators, SInt numerator, SInt denominator,
      SInt rank, BasicNumericFactory numericFactory, LPFactory lpFactory) {
    this.numerators = numerators;
    this.denominators = denominators;
    this.numerator = numerator;
    this.denominator = denominator;
    this.rank = rank;
    this.numericFactory = numericFactory;
    this.lpFactory = lpFactory;
  }

  public RankProtocol(SInt[] values, SInt rankValue, SInt rank, BasicNumericFactory numericFactory,
      LPFactory lpFactory) {
    this(values, null, rankValue, null, rank, numericFactory, lpFactory);
  }

  @Override
  protected ProtocolProducer initializeProtocolProducer() {
    NumericProtocolBuilder build = new NumericProtocolBuilder(numericFactory);
    SInt[] compLeft = null;
    SInt[] compRight = null;
    build.beginParScope();
    if (denominators == null) {
      compLeft = numerators;
    } else {
      compLeft = build.scale(denominator, numerators);
    }
    if(denominator == null) {
      compRight = new SInt[compLeft.length];
      Arrays.fill(compRight, numerator);
    } else {
      compRight = build.scale(numerator, denominators);
    }
    build.endCurScope();
    SInt[] comparisonResults = build.getSIntArray(numerators.length);
    build.beginParScope();
    for (int i = 0; i < numerators.length; i++) {
      ProtocolProducer comp = lpFactory
          .getComparisonProtocol(compLeft[i], compRight[i], comparisonResults[i], true);
      build.addProtocolProducer(comp);
    }
    build.endCurScope();
    SInt result = build.sum(comparisonResults);
    return new SequentialProtocolProducer(build.getProtocol(),
        lpFactory.getCopyProtocol(result, rank));
  }
}
