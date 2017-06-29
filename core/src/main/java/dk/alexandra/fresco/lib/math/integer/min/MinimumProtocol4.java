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
package dk.alexandra.fresco.lib.math.integer.min;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComparisonBuilder;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.NumericBuilder;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialProtocolBuilder;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ConditionalSelect;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MinimumProtocol4 implements ComputationBuilder<Pair<List<Computation<SInt>>, SInt>> {

  private final List<Computation<SInt>> xs;
  private final int k;

  public MinimumProtocol4(List<Computation<SInt>> xs) {
    this.k = xs.size();
    if (this.k < 2) {
      throw new IllegalArgumentException("Minimum protocol. k should never be less than 2.");
    }
    this.xs = xs;
  }


  @Override
  public Computation<Pair<List<Computation<SInt>>, SInt>> build(SequentialProtocolBuilder builder) {
    BigInteger one = BigInteger.ONE;
    if (this.k == 2) {
      ComparisonBuilder comparison = builder.comparison();
      NumericBuilder numeric = builder.numeric();
      Computation<SInt> firstValue = this.xs.get(0);
      Computation<SInt> secondValue = this.xs.get(1);
      Computation<SInt> firstCompare = comparison.compare(firstValue, secondValue);
      Computation<SInt> minimum = builder
          .createSequentialSub(new ConditionalSelect(firstCompare, firstValue, secondValue));
      Computation<SInt> secondCompare = numeric
          .sub(one, firstCompare);
      return () -> new Pair<>(
          Arrays.asList(firstCompare, secondCompare),
          minimum.out());
    } else if (this.k == 3) {
      ComparisonBuilder comparison = builder.comparison();
      NumericBuilder numeric = builder.numeric();
      Computation<SInt> firstValue = this.xs.get(0);
      Computation<SInt> secondValue = this.xs.get(1);
      Computation<SInt> thirdValue = this.xs.get(2);
      Computation<SInt> c1_prime = comparison.compare(firstValue, secondValue);

      Computation<SInt> m1 = builder
          .createSequentialSub(new ConditionalSelect(c1_prime, firstValue, secondValue));

      Computation<SInt> c2_prime = comparison.compare(m1, thirdValue);

      Computation<SInt> m2 = builder
          .createSequentialSub(new ConditionalSelect(c2_prime, m1, thirdValue));

      Computation<SInt> firstComparison = numeric.mult(c1_prime, c2_prime);
      Computation<SInt> secondComparison = numeric.sub(c2_prime, firstComparison);
      Computation<SInt> tmp = numeric.sub(one, firstComparison);
      Computation<SInt> thirdComparison = numeric.sub(tmp, secondComparison);
      return () -> new Pair<>(
          Arrays.asList(firstComparison, secondComparison, thirdComparison),
          m2.out());
    } else {
      return builder.seq((seq) -> {
        int k1 = k / 2;
        List<Computation<SInt>> x1 = xs.subList(0, k1);
        List<Computation<SInt>> x2 = xs.subList(k1, k);
        return Pair.lazy(x1, x2);
      }).par(
          (pair, seq) ->
              seq.createSequentialSub(
                  new MinimumProtocol4(pair.getFirst())),
          (pair, seq) ->
              seq.createSequentialSub(
                  new MinimumProtocol4(pair.getSecond()))
      ).seq((pair, seq) -> {
        ComparisonBuilder comparison = seq.comparison();
        NumericBuilder numeric = seq.numeric();
        Pair<List<Computation<SInt>>, SInt> minimum1 = pair.getFirst();
        Pair<List<Computation<SInt>>, SInt> minimum2 = pair.getSecond();
        SInt m1 = minimum1.getSecond();
        SInt m2 = minimum2.getSecond();

        Computation<SInt> compare = comparison.compare(m1, m2);
        Computation<SInt> oneMinusCompare = numeric.sub(one, compare);
        Computation<SInt> m = seq
            .createSequentialSub(new ConditionalSelect(compare, m1, m2));
        Computation<List<Computation<SInt>>> enteringIndexes = seq.par((par) -> {
          NumericBuilder parNumeric = par.numeric();
          List<Computation<SInt>> cs = new ArrayList<>(k);
          for (Computation<SInt> c : minimum1.getFirst()) {
            cs.add(parNumeric.mult(c, compare));
          }
          for (Computation<SInt> c : minimum2.getFirst()) {
            cs.add(parNumeric.mult(c, oneMinusCompare));
          }
          return () -> cs;
        });
        return () -> {
          List<Computation<SInt>> out = enteringIndexes.out();
          SInt out1 = m.out();
          return new Pair<>(out, out1);
        };
      });
    }
  }
}
