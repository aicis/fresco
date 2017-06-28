/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.compare;


import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.ComparisonProtocolBuilder;
import java.math.BigInteger;

public class SortingProtocolBuilder extends ComparisonProtocolBuilder {

  public SortingProtocolBuilder(ComparisonProtocolFactory cpf, BasicNumericFactory bnf) {
    super(cpf, bnf);
    // TODO Auto-generated constructor stub
  }

  public SInt isSorted(SInt[] values) {
    //first compare the values pairwise
    SInt[] comparisons = new SInt[values.length - 1];
    //initialize comparisons array
    for (int i = 0; i < comparisons.length; i++) {
      comparisons[i] = getBnf().getSInt();
    }
    //build parallel comparison protocol
    beginParScope();
    //TODO maybe split this in chunks of reasonable sizes
    for (int i = 0; i < comparisons.length; i++) {
      comparisons[i] = compare(values[i], values[i + 1]);
    }
    endCurScope();
    //then multiply the results sequentially.
    //set result to 1
    SInt result = getBnf().getSInt();
    append(getBnf().getSInt(1, result));
    //multiply by values from comparison
    for (int i = 0; i < comparisons.length; i++) {
      append(getBnf().getMultProtocol(result, comparisons[i], result));
    }
    return result;
  }


  private static int FloorLog2(int value) {
    int result = -1;
    for (int i = 1; i < value; i <<= 1, ++result) {
      ;
    }
    return result;
  }

  final BigInteger minusOne = BigInteger.valueOf(-1L);

  public void compareAndSwap(int a, int b, SInt[] values) {
    //Non splitting version

    //Reporter.info(a+","+b);
    beginSeqScope();
    SInt c = getBnf().getSInt();
    SInt d = getBnf().getSInt();
    SInt comparison = compare(values[a], values[b]);

    //a = comparison*a+(1-comparison)*b ==> comparison*(a-b)+b
    //b = comparison*b+(1-comparison)*a ==>  -comparison*(a-b)+a

    append(getBnf().getSubtractProtocol(values[a], values[b], c));
    append(getBnf().getMultProtocol(comparison, c, c));
    append(getBnf().getMultProtocol(minusOne, c, d));

    beginParScope();
    append(getBnf().getAddProtocol(c, values[b], c));
    append(getBnf().getAddProtocol(d, values[a], d));
    endCurScope();

    values[a] = c;
    values[b] = d;
    endCurScope();
  }


  public void sort(SInt[] values) {
    //sort using Batcher´s Merge Exchange

    int t = FloorLog2(values.length);
    int p0 = (1 << t);
    int p = p0;

    do {
      int q = p0;
      int r = 0;
      int d = p;

      while (r == 0 || q != p) {
        //Reporter.info("--");
        beginParScope();

        if (r != 0) {
          d = q - p;
          q >>= 1;
        }

        for (int i = 0; i < values.length - d; i++) {

          if ((i & p) == r) {
            compareAndSwap(i, i + d, values);
          }
        }
        r = p;
        endCurScope();
      }
      p >>= 1;

    }
    while (p > 0);
  }
}
