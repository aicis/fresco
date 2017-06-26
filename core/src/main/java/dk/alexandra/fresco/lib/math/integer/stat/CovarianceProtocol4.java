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
 */
package dk.alexandra.fresco.lib.math.integer.stat;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class CovarianceProtocol4
    implements Function<SequentialProtocolBuilder, Computation<SInt>> {

  private final List<Computation<SInt>> data1;
  private final List<Computation<SInt>> data2;
  private final Computation<SInt> mean1;
  private final Computation<SInt> mean2;


  public CovarianceProtocol4(List<Computation<SInt>> data1, List<Computation<SInt>> data2,
      Computation<SInt> mean1, Computation<SInt> mean2) {
    this.data1 = data1;
    this.data2 = data2;

    if (data1.size() != data2.size()) {
      throw new IllegalArgumentException("Must have same sample size.");
    }

    this.mean1 = mean1;
    this.mean2 = mean2;
  }

  public CovarianceProtocol4(List<Computation<SInt>> data1, List<Computation<SInt>> data2) {
    this(data1, data2, null, null);
  }

  @Override
  public Computation<SInt> apply(SequentialProtocolBuilder builder) {
    return builder.seq((seq) -> null
    ).par(
        (ignored, seq) -> {
          if (mean1 == null) {
            return seq.createSequentialSub(new MeanProtocol4(data1));
          } else {
            return mean1;
          }
        },
        (ignored, seq) -> {
          if (mean2 == null) {
            return seq.createSequentialSub(new MeanProtocol4(data1));
          } else {
            return mean2;
          }
        }
    ).par((means, par) -> {
      SInt mean1 = means.getFirst();
      SInt mean2 = means.getSecond();
      //Implemented using two iterators instead of indexed loop to avoid enforcing RandomAccess lists
      Iterator<Computation<SInt>> iterator1 = data1.iterator();
      Iterator<Computation<SInt>> iterator2 = data2.iterator();
      List<Computation<SInt>> terms = new ArrayList<>(data1.size());
      while (iterator1.hasNext()) {
        Computation<SInt> value1 = iterator1.next();
        Computation<SInt> value2 = iterator2.next();
        Computation<SInt> term = par.createSequentialSub((seq) -> {
          NumericBuilder numeric = seq.numeric();
          Computation<SInt> tmp1 = numeric.sub(value1, mean1);
          Computation<SInt> tmp2 = numeric.sub(value2, mean2);
          return numeric.mult(tmp1, tmp2);
        });
        terms.add(term);
      }
      return () -> terms;
    }).seq((terms, seq) ->
        seq.createSequentialSub(new MeanProtocol4(terms, data1.size() - 1))
    );
  }

}
