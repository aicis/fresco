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

import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocol;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactory;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactoryImpl;
import dk.alexandra.fresco.lib.compare.ConditionalSelectProtocol;
import dk.alexandra.fresco.lib.compare.ConditionalSelectProtocolImpl;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactoryImpl;
import dk.alexandra.fresco.lib.compare.eq.EqualityProtocol;
import dk.alexandra.fresco.lib.compare.eq.EqualityProtocolImpl;
import dk.alexandra.fresco.lib.compare.gt.GreaterThanReducerProtocolImpl;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestProtocolFactory;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestProtocolFactoryImpl;
import dk.alexandra.fresco.lib.debug.MarkerProtocol;
import dk.alexandra.fresco.lib.debug.MarkerProtocolImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.RandomFieldElementFactory;
import dk.alexandra.fresco.lib.helper.CopyProtocol;
import dk.alexandra.fresco.lib.helper.CopyProtocolImpl;
import dk.alexandra.fresco.lib.math.integer.NumericNegateBitFactory;
import dk.alexandra.fresco.lib.math.integer.NumericNegateBitFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.InversionProtocolImpl;
import dk.alexandra.fresco.lib.math.integer.inv.InversionProtocol;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.EntrywiseProductProtocol;
import dk.alexandra.fresco.lib.math.integer.linalg.EntrywiseProductProtocolImpl;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductProtocol;
import dk.alexandra.fresco.lib.math.integer.min.MinimumProtocol;
import dk.alexandra.fresco.lib.math.integer.min.MinimumProtocolImpl;
import dk.alexandra.fresco.lib.math.integer.min.MinInfFracProtocol;
import dk.alexandra.fresco.lib.math.integer.min.MinimumFractionProtocol;
import dk.alexandra.fresco.lib.math.integer.min.MinimumFractionProtocolImpl;

public class LPFactoryImpl implements LPFactory {

	private final int securityParameter;
	private final BasicNumericFactory bnf;
	private final LocalInversionFactory localInvFactory;
	private final NumericNegateBitFactory numericNegateBitFactory;
	private final RandomAdditiveMaskFactory randomAdditiveMaskFactory;
	private final RandomFieldElementFactory randFactory;
	private final InnerProductFactory innerProductFactory;
	private final ZeroTestProtocolFactory zeroTestProtocolFactory;
	private final MiscOIntGenerators misc;
	private ComparisonProtocolFactory compFactory;

	public LPFactoryImpl(int securityParameter, BasicNumericFactory bnf,
			LocalInversionFactory localInvFactory,
			NumericBitFactory numericBitFactory,
			ExpFromOIntFactory expFromOIntFactory,
			PreprocessedExpPipeFactory expFactory,
			RandomFieldElementFactory randFactory) {
		this.securityParameter = securityParameter;
		this.bnf = bnf;
		this.localInvFactory = localInvFactory;
		this.randFactory = randFactory;
		this.numericNegateBitFactory = new NumericNegateBitFactoryImpl(bnf);
		this.innerProductFactory = new InnerProductFactoryImpl(bnf);		
		randomAdditiveMaskFactory = new RandomAdditiveMaskFactoryImpl(bnf,
				numericBitFactory);
		misc = new MiscOIntGenerators(bnf);
		this.zeroTestProtocolFactory = new ZeroTestProtocolFactoryImpl(bnf,
				expFromOIntFactory, numericBitFactory, numericNegateBitFactory, expFactory);
		this.compFactory = new ComparisonProtocolFactoryImpl(securityParameter, bnf, localInvFactory, numericBitFactory, expFromOIntFactory, expFactory);
	}

	@Override
	public InversionProtocol getInversionProtocol(SInt x, SInt result) {
		return new InversionProtocolImpl(x, result, bnf, localInvFactory, randFactory);
	}

	@Override
	public MarkerProtocol getMarkerProtocol(String message) {
		return new MarkerProtocolImpl(message);
	}

	@Override
	public CopyProtocol<SInt> getCopyProtocol(SInt in, SInt out) {
		return new CopyProtocolImpl<SInt>(in, out);
	}

	@Override
	public EntrywiseProductProtocol getEntrywiseProductProtocol(SInt[] as, SInt[] bs,
			SInt[] results) {
		return new EntrywiseProductProtocolImpl(as, bs, results, bnf);
	}

	@Override
	public EntrywiseProductProtocol getEntrywiseProductProtocol(SInt[] as, OInt[] bs,
			SInt[] results) {
		return new EntrywiseProductProtocolImpl(as, bs, results, bnf);
	}

	@Override
	public ConditionalSelectProtocol getConditionalSelectProtocol(SInt selector,
			SInt a, SInt b, SInt result) {
		return new ConditionalSelectProtocolImpl(selector, a, b, result, bnf);
	}

	@Override
	public MinimumProtocol getMinimumProtocol(SInt[] as, SInt m, SInt[] cs) {
		return new MinimumProtocolImpl(as, m, cs, this, bnf);
	}

	@Override
	public MinimumFractionProtocol getMinimumFractionProtocol(SInt[] ns,
			SInt[] ds, SInt nm, SInt dm, SInt[] cs) {
		return new MinimumFractionProtocolImpl(ns, ds, nm, dm, cs, bnf, this);
	}
	
	@Override
	public MinInfFracProtocol getMinInfFracProtocol(SInt[] ns,
			SInt[] ds, SInt[] infs, SInt nm, SInt dm, SInt infm, SInt[] cs) {
		return new MinInfFracProtocol(ns, ds, infs, nm, dm, infm, cs, bnf, compFactory);
	}

	@Override
	public ComparisonProtocol getComparisonProtocol(SInt x1, SInt x2,
			SInt result, boolean longCompare) {
		int bitLength = bnf.getMaxBitLength();
		if (longCompare) {
			bitLength *= 2;
		}
		return new GreaterThanReducerProtocolImpl(bitLength,
				this.securityParameter, x1, x2, result, bnf, numericNegateBitFactory,
				randomAdditiveMaskFactory, zeroTestProtocolFactory, misc,
				innerProductFactory, localInvFactory);
	}

	@Override
	public EqualityProtocol getEqualityProtocol(int bitLength,
			int securityParam, SInt x, SInt y, SInt result) {
		return new EqualityProtocolImpl(bitLength, securityParam, x, y, result,
				bnf, zeroTestProtocolFactory);
	}

	@Override
	public EnteringVariableProtocol getEnteringVariableProtocol(
			LPTableau tableau, Matrix<SInt> updateMatrix, SInt[] enteringIndex,
			SInt minimum) {
		return new EnteringVariableProtocol(tableau, updateMatrix,
				enteringIndex, minimum, this, bnf);
	}

	@Override
	public ExitingVariableProtocol getExitingVariableProtocol(LPTableau tableau,
			Matrix<SInt> updateMatrix, SInt[] enteringIndex,
			SInt[] exitingIndex, SInt[] updateColumn, SInt pivot) {
		return new ExitingVariableProtocol(tableau, updateMatrix, enteringIndex,
				exitingIndex, updateColumn, pivot, this, bnf);
	}

	@Override
	public UpdateMatrixProtocol getUpdateMatrixProtocol(
			Matrix<SInt> oldUpdateMatrix, SInt[] L, SInt[] C, SInt p,
			SInt p_prime, Matrix<SInt> newUpdateMatrix) {
		return new UpdateMatrixProtocol(oldUpdateMatrix, L, C, p, p_prime,
				newUpdateMatrix, this, bnf);
	}

	@Override
	public InnerProductProtocol getInnerProductProtocol(SInt[] aVector,
			SInt[] bVector, SInt result) {
		return this.innerProductFactory.getInnerProductProtocol(aVector,
				bVector, result);
	}

	@Override
	public OptimalValueProtocol getOptimalValueProtocol(
			Matrix<SInt> updateMatrix, SInt[] B, SInt pivot, SInt optimalValue) {
		return new OptimalValueProtocol(updateMatrix, B, pivot, optimalValue,
				this, bnf);
	}

	@Override
	public OptimalNumeratorProtocol getOptimalNumeratorProtocol(
			Matrix<SInt> updateMatrix, SInt[] B, SInt optimalNumerator) {
		return new OptimalNumeratorProtocol(updateMatrix, B, optimalNumerator,
				this);
	}

	@Override
	public RankProtocol getRankProtocol(SInt[] numerators, SInt[] denominators,
			SInt numerator, SInt denominator, SInt rank) {
		return new RankProtocol(numerators, denominators, numerator,
				denominator, rank, bnf, this);
	}

	@Override
	public RankProtocol getRankProtocol(SInt[] values, SInt rankValue, SInt rank) {
		return new RankProtocol(values, rankValue, rank, bnf, this);
	}

	@Override
	public LPSolverProtocol getLPSolverProtocol(LPTableau tableau,
			Matrix<SInt> updateMatrix, SInt pivot) {
		return new LPSolverProtocol(tableau, updateMatrix, pivot, this, bnf);
	}
}
