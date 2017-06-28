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
package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ConditionalSelectProtocol;
import dk.alexandra.fresco.lib.compare.eq.EqualityProtocol;
import dk.alexandra.fresco.lib.debug.MarkerFactory;
import dk.alexandra.fresco.lib.helper.CopyProtocolFactory;
import dk.alexandra.fresco.lib.math.integer.inv.InversionProtocolFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.EntrywiseProductFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactory;
import dk.alexandra.fresco.lib.math.integer.min.MinInfFracProtocol;
import dk.alexandra.fresco.lib.math.integer.min.MinimumFractionProtocol;
import dk.alexandra.fresco.lib.math.integer.min.MinimumProtocol;

public interface LPFactory
    extends InversionProtocolFactory, MarkerFactory, CopyProtocolFactory<SInt>,
    EntrywiseProductFactory, InnerProductFactory {

  /**
   * @param selector input
   * @param a input - choice 1 if selector is true (1)
   * @param b input - choice 2 if selector is false (0)
   * @param result output - either a or b.
   */
  ConditionalSelectProtocol getConditionalSelectProtocol(SInt selector, SInt a, SInt b,
      SInt result);

  /**
   * inputs are the as, m is the base recursion result, and cs are the outputs
   * of the intermediate recursions.
   *
   * @param as input
   * @param m output
   * @param cs outputs
   */
  MinimumProtocol getMinimumProtocol(SInt[] as, SInt m, SInt[] cs);

  /**
   * Finds the minimum in an list of fractions. Note fractions are given as
   * separate arrays of numerators and denominators.
   *
   * @param ns input - the numerators
   * @param ds input - the denominators
   * @param nm output - the numerator of the minimum fraction
   * @param dm output - the denominator of the minimum fraction
   * @param cs output - the index vector for indicating the minimum fraction
   */
  MinimumFractionProtocol getMinimumFractionProtocol(SInt[] ns, SInt[] ds, SInt nm, SInt dm,
      SInt[] cs);

  /**
   * @param x1 input
   * @param x2 input
   * @param result output - [1] (true) or [0] (false) (result of x1 <= x2)
   * @param longCompare - true indicates that we are comparing long numbers and should use twice the
   * bit length
   */
  ProtocolProducer getComparisonProtocol(SInt x1, SInt x2, SInt result, boolean longCompare);

  /**
   * Returns a protocol for equality
   *
   * @param bitLength the maximum bitlength of the two arguments
   * @param securityParam the security parameter
   * @param x input - a number
   * @param y input - a number
   * @param result output - [1] (true) or [0] (false) (result of x1 = x2)
   * @return a protocol for equality
   */
  EqualityProtocol getEqualityProtocol(int bitLength, int securityParam, SInt x, SInt y,
      SInt result);

  /**
   * Computes the index of the entering variable.
   *
   * @param tableau input - a tableau of dimension (m + 1) x (n + m + 1) i.e. the C matrix is of
   * dimension m x (n+m)
   * @param updateMatrix input - an updateMatrix of dimension (m + 1) x (m + 1)
   * @param enteringIndex output - an index vector indexing the minimum entry in the updated F
   * vector, corresponding the entering variable
   * @param minimum output - the minimum entry in the F vector
   */
  EnteringVariableProtocol getEnteringVariableProtocol(LPTableau tableau, Matrix<SInt> updateMatrix,
      SInt[] enteringIndex, SInt minimum);

  /**
   * Computes the index of the exiting variable along with values needed to
   * compute the update matrix for this iteration
   *
   * @param tableau input - a tableau of dimension (m + 1) x (n + m + 1) i.e. the C matrix is of
   * dimension m x (n+m)
   * @param updateMatrix input - an updateMatrix of dimension (m + 1) x (m + 1)
   * @param enteringIndex input - an index vector indexing the variable to leave the basis
   * @param exitingIndex output - an index vector indexing the most constraining constraint,
   * corresponding to the exiting variable
   * @param updateColumn output - the column used to generate the update matrix of this iteration
   * @param pivot output - the pivot element used to generate the update matrix of this iteration
   */
  ExitingVariableProtocol getExitingVariableProtocol(LPTableau tableau, Matrix<SInt> updateMatrix,
      SInt[] enteringIndex, SInt[] exitingIndex, SInt[] updateColumn, SInt pivot);


  /**
   * Finds the minimum in an list of fractions. Note fractions are given as
   * separate arrays of numerators and denominators. Also allows to indicate
   * certain fractions that should take a value of infinity in comparisons.
   * I.e., indicate fractions that should never be chosen as the minimum.
   *
   * @param ns input - the numerators
   * @param ds input - the denominators
   * @param infs input - should hold 0/1 values. If the i'th value is 1 the i'th fraction should be
   * regarded as having an infinity value in the comparisons
   * @param nm output - the numerator of the minimum fraction
   * @param dm output - the denominator of the minimum fraction
   * @param infm output - the infinity indicator of the minimum fraction. I.e., should only be 1 if
   * all fractions are regarded as infinity and 0 otherwise.
   * @param cs output - the index vector for indicating the minimum fraction
   */
  MinInfFracProtocol getMinInfFracProtocol(SInt[] ns, SInt[] ds, SInt[] infs, SInt nm, SInt dm,
      SInt infm, SInt[] cs);
}
