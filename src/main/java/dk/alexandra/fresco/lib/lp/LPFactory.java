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

import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocol;
import dk.alexandra.fresco.lib.compare.ConditionalSelectProtocol;
import dk.alexandra.fresco.lib.compare.eq.EqualityProtocol;
import dk.alexandra.fresco.lib.debug.MarkerFactory;
import dk.alexandra.fresco.lib.helper.CopyProtocolFactory;
import dk.alexandra.fresco.lib.math.integer.inv.InversionProtocolFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.EntrywiseProductFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductProtocol;
import dk.alexandra.fresco.lib.math.integer.min.MinimumProtocol;
import dk.alexandra.fresco.lib.math.integer.min.MinInfFracProtocol;
import dk.alexandra.fresco.lib.math.integer.min.MinimumFractionProtocol;

public interface LPFactory
		extends InversionProtocolFactory, MarkerFactory, CopyProtocolFactory<SInt>, EntrywiseProductFactory {

	/**
	 * 
	 * @param selector
	 *            input
	 * @param a
	 *            input - choice 1 if selector is true (1)
	 * @param b
	 *            input - choice 2 if selector is false (0)
	 * @param result
	 *            output - either a or b.
	 * @return
	 */
	public ConditionalSelectProtocol getConditionalSelectProtocol(SInt selector, SInt a, SInt b, SInt result);

	/**
	 * inputs are the as, m is the base recursion result, and cs are the outputs
	 * of the intermediate recursions.
	 * 
	 * @param as
	 *            input
	 * @param m
	 *            output
	 * @param cs
	 *            outputs
	 * @return
	 */
	public MinimumProtocol getMinimumProtocol(SInt[] as, SInt m, SInt[] cs);

	/**
	 * Finds the minimum in an list of fractions. Note fractions are given as
	 * separate arrays of numerators and denominators.
	 * 
	 * @param ns
	 *            input - the numerators
	 * @param ds
	 *            input - the denominators
	 * @param nm
	 *            output - the numerator of the minimum fraction
	 * @param dm
	 *            output - the denominator of the minimum fraction
	 * @param cs
	 *            output - the index vector for indicating the minimum fraction
	 * @return
	 */
	public MinimumFractionProtocol getMinimumFractionProtocol(SInt[] ns, SInt[] ds, SInt nm, SInt dm, SInt[] cs);

	/**
	 * 
	 * @param x1
	 *            input
	 * @param x2
	 *            input
	 * @param result
	 *            output - [1] (true) or [0] (false) (result of x1 <= x2)
	 * @param longCompare
	 *            - true indicates that we are comparing long numbers and should
	 *            use twice the bit length
	 * @return
	 */
	public ComparisonProtocol getComparisonProtocol(SInt x1, SInt x2, SInt result, boolean longCompare);

	/**
	 * Returns a protocol for equality
	 * 
	 * @param bitlength
	 *            the maximum bitlength of the two arguments
	 * @param securityParam
	 *            the security parameter
	 * @param x1
	 *            input - a number
	 * @param x2
	 *            input - a number
	 * @param result
	 *            output - [1] (true) or [0] (false) (result of x1 = x2)
	 * 
	 * @return a protocol for equality
	 */
	public EqualityProtocol getEqualityProtocol(int bitLength, int securityParam, SInt x, SInt y, SInt result);

	/**
	 * Computes the index of the entering variable.
	 * 
	 * @param tableau
	 *            input - a tableau of dimension (m + 1) x (n + m + 1) i.e. the
	 *            C matrix is of dimension m x (n+m)
	 * @param updateMatrix
	 *            input - an updateMatrix of dimension (m + 1) x (m + 1)
	 * @param enteringIndex
	 *            output - an index vector indexing the minimum entry in the
	 *            updated F vector, corresponding the entering variable
	 * @param minimum
	 *            output - the minimum entry in the F vector
	 * @return
	 */
	public EnteringVariableProtocol getEnteringVariableProtocol(LPTableau tableau, Matrix<SInt> updateMatrix,
			SInt[] enteringIndex, SInt minimum);

	/**
	 * Computes the index of the exiting variable along with values needed to
	 * compute the update matrix for this iteration
	 * 
	 * @param tableau
	 *            input - a tableau of dimension (m + 1) x (n + m + 1) i.e. the
	 *            C matrix is of dimension m x (n+m)
	 * @param updateMatrix
	 *            input - an updateMatrix of dimension (m + 1) x (m + 1)
	 * @param enteringIndex
	 *            input - an index vector indexing the variable to leave the
	 *            basis
	 * @param exitingIndex
	 *            output - an index vector indexing the most constraining
	 *            constraint, corresponding to the exiting variable
	 * @param updateColumn
	 *            output - the column used to generate the update matrix of this
	 *            iteration
	 * @param pivot
	 *            output - the pivot element used to generate the update matrix
	 *            of this iteration
	 * @return
	 */
	public ExitingVariableProtocol getExitingVariableProtocol(LPTableau tableau, Matrix<SInt> updateMatrix,
			SInt[] enteringIndex, SInt[] exitingIndex, SInt[] updateColumn, SInt pivot);

	/**
	 * @param oldUpdateMatrix
	 *            input - the current update matrix
	 * @param L
	 *            input - the index vector of the exiting variable
	 * @param C
	 *            input - the column to be inserted into the update matrix of
	 *            this iteration
	 * @param p
	 *            input - the pivot element
	 * @param p_prime
	 *            input - the previous pivot element
	 * @param newUpdateMatrix
	 *            output - the new update matrix
	 * @param lambdas
	 *            The lambda values after the update has been run.
	 * @return
	 */
	public UpdateMatrixProtocol getUpdateMatrixProtocol(Matrix<SInt> oldUpdateMatrix, SInt[] L, SInt[] C, SInt p,
			SInt p_prime, Matrix<SInt> newUpdateMatrix, SInt[] lambdas);

	/**
	 * @param aVector
	 *            input - an n-dimensional vector
	 * @param bVector
	 *            input - an n-dimensional vector
	 * @param result
	 *            output - the inner product of the two input input vectors
	 * @return a protocol computing the inner product of two vectors
	 */
	public InnerProductProtocol getInnerProductProtocol(SInt[] aVector, SInt[] bVector, SInt result);

	/**
	 * Computes the optimal value after the last simplex iteration
	 * 
	 * @param updateMatrix
	 *            input - the current update matrix
	 * @param B
	 *            input - the B vector of the tableau
	 * @param pivot
	 *            input - the previous pivot element
	 * @param optimalValue
	 *            output - optimal value
	 * @return
	 */
	public OptimalValueProtocol getOptimalValueProtocol(Matrix<SInt> updateMatrix, SInt[] B, SInt pivot,
			SInt optimalValue);

	/**
	 * Computes the numerator of the optimal value after the last simplex
	 * iteration (the pivot element being the denominator)
	 * 
	 * @param updateMatrix
	 *            input - the current update matrix
	 * @param B
	 *            input - the B vector of the tableau
	 * @param optimalNumerator
	 *            output - optimal numerator
	 * @return
	 */
	public OptimalNumeratorProtocol getOptimalNumeratorProtocol(Matrix<SInt> updateMatrix, SInt[] B,
			SInt optimalNumerator);

	/**
	 * Ranks a rational value against a list of rational values. I.e. for a
	 * value x/y finds the number of of values u/v in a list so that u/v <= x/y.
	 * 
	 * @param numerators
	 *            input - a list of numerators
	 * @param denominators
	 *            input - a list of denominators
	 * @param numerator
	 *            input - the numerator of the rational to rank
	 * @param denominator
	 *            input - the denominator of the rational to rank
	 * @param rank
	 *            output - the rank
	 * @return
	 */
	public RankProtocol getRankProtocol(SInt[] numerators, SInt[] denominators, SInt numerator, SInt denominator,
			SInt rank);

	/**
	 * Ranks a value against a list of values. I.e. for a value x finds the
	 * number of of values u in a list so that u <= x.
	 * 
	 * @param values
	 *            input - a list of values
	 * @param rankValue
	 *            input - a value to rank against the list
	 * @param rank
	 *            output - will contain the rank of the value to be ranked
	 * @return
	 */
	public RankProtocol getRankProtocol(SInt[] values, SInt rankValue, SInt rank);

	/**
	 * Returns a protocol solving an LP-problem from an initial tableau, update
	 * matrix and value for the previous pivot. (Usually the update matrix and
	 * pivot will be initialized to the identity matrix and the value one
	 * respectively). The update matrix and pivot will be update through the
	 * computation, and used to compute the optimal value of the LP-problem
	 * after the protocol has been evaluated.
	 * 
	 * @param tableau
	 * @param updateMatrix
	 * @param pivot
	 * @param lambdas
	 * @return an LPSolverprotocol
	 */
	public LPSolverProtocol getLPSolverProtocol(LPTableau tableau, Matrix<SInt> updateMatrix, SInt pivot, SInt[] lambdas);

	/**
	 * Finds the minimum in an list of fractions. Note fractions are given as
	 * separate arrays of numerators and denominators. Also allows to indicate
	 * certain fractions that should take a value of infinity in comparisons.
	 * I.e., indicate fractions that should never be chosen as the minimum.
	 * 
	 * @param ns
	 *            input - the numerators
	 * @param ds
	 *            input - the denominators
	 * @param infs
	 *            input - should hold 0/1 values. If the i'th value is 1 the
	 *            i'th fraction should be regarded as having an infinity value
	 *            in the comparisons
	 * @param nm
	 *            output - the numerator of the minimum fraction
	 * @param dm
	 *            output - the denominator of the minimum fraction
	 * @param infm
	 *            output - the infinity indicator of the minimum fraction. I.e.,
	 *            should only be 1 if all fractions are regarded as infinity and
	 *            0 otherwise.
	 * @param cs
	 *            output - the index vector for indicating the minimum fraction
	 * @return
	 */
	MinInfFracProtocol getMinInfFracProtocol(SInt[] ns, SInt[] ds, SInt[] infs, SInt nm, SInt dm, SInt infm, SInt[] cs);
}
