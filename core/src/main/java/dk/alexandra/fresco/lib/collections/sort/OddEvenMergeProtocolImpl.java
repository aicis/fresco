/*******************************************************************************
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.collections.sort;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.List;

/**
 * An implementation of the OddEvenMergeProtocol. We use batchers algorithm. For now, this
 * implementation only supports lists where the size is a power of 2. (i.e. 4, 8, 16 etc.)
 *
 */
public class OddEvenMergeProtocolImpl implements
    ComputationBuilder<List<Pair<List<Computation<SBool>>, List<Computation<SBool>>>>, ProtocolBuilderBinary> {

  private List<Pair<List<Computation<SBool>>, List<Computation<SBool>>>> numbers;

  public OddEvenMergeProtocolImpl(
      List<Pair<List<Computation<SBool>>, List<Computation<SBool>>>> unsortedNumbers) {
    super();
    this.numbers = unsortedNumbers;
  }

  @Override
  public Computation<List<Pair<List<Computation<SBool>>, List<Computation<SBool>>>>> buildComputation(
      ProtocolBuilderBinary builder) {
    return builder.seq(seq -> {
      sort(0, numbers.size() - 1, seq);
      return () -> numbers;
    });
  }

  private void sort(int i, int j, ProtocolBuilderBinary builder) {
    if (j - i > 1) {
      sort(i, j / 2, builder);
      sort(j / 2 + 1, j, builder);
      merge(i, (j - i + 1), 1, builder);
    } else {
      compareAndSwapAtIndices(i, j, builder);
    }
  }

  private void compareAndSwapAtIndices(int i, int j, ProtocolBuilderBinary builder) {
    builder.seq(seq -> {
      return seq.advancedBinary().keyedCompareAndSwap(numbers.get(i), numbers.get(j));
    }).seq((seq, res) -> {
      numbers.set(i, res.get(0));
      numbers.set(j, res.get(1));
      return null;
    });
  }

  private void merge(int first, int length, int step, ProtocolBuilderBinary builder) {
    int doubleStep = step * 2;
    if (length > 2) {
      builder.seq((seq) -> {
        int newLength = length / 2;
        merge(first, newLength, doubleStep, seq);
        merge(first + step, length - newLength, doubleStep, seq);
        for (int i = 1; i < length - 2; i += 2) {
          int low = first + i * step;
          int high = low + step;
          compareAndSwapAtIndices(low, high, seq);
        }
        return null;
      });
    } else if (length == 2) {
      compareAndSwapAtIndices(first, first + step, builder);
    }
  }
}
