/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.logic;

import java.util.List;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.collections.sort.KeyedCompareAndSwapCircuitGetNextGatesImpl;
import dk.alexandra.fresco.lib.collections.sort.OddEvenMergeCircuit;
import dk.alexandra.fresco.lib.collections.sort.OddEvenMergeCircuitRec;
import dk.alexandra.fresco.lib.collections.sort.OddEvenMergeSortFactory;
import dk.alexandra.fresco.lib.compare.CompareAndSwapCircuit;
import dk.alexandra.fresco.lib.compare.CompareAndSwapCircuitFactory;
import dk.alexandra.fresco.lib.compare.CompareAndSwapCircuitImpl;
import dk.alexandra.fresco.lib.compare.KeyedCompareAndSwapCircuit;
import dk.alexandra.fresco.lib.compare.bool.BinaryGreaterThanProtocol;
import dk.alexandra.fresco.lib.compare.bool.BinaryGreaterThanProtocolFactory;
import dk.alexandra.fresco.lib.compare.bool.BinaryGreaterThanProtocolImpl;
import dk.alexandra.fresco.lib.compare.bool.eq.AltBinaryEqualityProtocol;
import dk.alexandra.fresco.lib.compare.bool.eq.BinaryEqualityCircuitFactory;
import dk.alexandra.fresco.lib.compare.bool.eq.BinaryEqualityProtocol;
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.field.bool.NandProtocol;
import dk.alexandra.fresco.lib.field.bool.NotProtocol;
import dk.alexandra.fresco.lib.field.bool.OrProtocol;
import dk.alexandra.fresco.lib.field.bool.XnorProtocol;
import dk.alexandra.fresco.lib.field.bool.generic.AndFromCopyConstProtocol;
import dk.alexandra.fresco.lib.field.bool.generic.NandFromAndAndNotProtocolImpl;
import dk.alexandra.fresco.lib.field.bool.generic.NotFromXorProtocol;
import dk.alexandra.fresco.lib.field.bool.generic.OrFromCopyConstProtocol;
import dk.alexandra.fresco.lib.field.bool.generic.OrFromXorAndProtocol;
import dk.alexandra.fresco.lib.field.bool.generic.XnorFromXorAndNotProtocolImpl;
import dk.alexandra.fresco.lib.helper.CopyProtocolFactory;
import dk.alexandra.fresco.lib.math.add.AdderCircuitFactory;
import dk.alexandra.fresco.lib.math.add.BitIncrementerCircuit;
import dk.alexandra.fresco.lib.math.add.BitIncrementerCircuitFactory;
import dk.alexandra.fresco.lib.math.add.BitIncrementerCircuitImpl;
import dk.alexandra.fresco.lib.math.add.FullAdderCircuit;
import dk.alexandra.fresco.lib.math.add.FullAdderCircuitImpl;
import dk.alexandra.fresco.lib.math.add.OneBitFullAdderCircuit;
import dk.alexandra.fresco.lib.math.add.OneBitFullAdderCircuitImpl;
import dk.alexandra.fresco.lib.math.add.OneBitHalfAdderCircuit;
import dk.alexandra.fresco.lib.math.add.OneBitHalfAdderCircuitImpl;
import dk.alexandra.fresco.lib.math.log.LogCircuitFactory;
import dk.alexandra.fresco.lib.math.log.LogProtocol;
import dk.alexandra.fresco.lib.math.log.LogProtocolImpl;
import dk.alexandra.fresco.lib.math.mult.BinaryMultCircuit;
import dk.alexandra.fresco.lib.math.mult.BinaryMultCircuitFactory;
import dk.alexandra.fresco.lib.math.mult.BinaryMultCircuitImpl;

public abstract class AbstractBinaryFactory implements BasicLogicFactory,
		AdderCircuitFactory, BinaryMultCircuitFactory, LogCircuitFactory,
		CopyProtocolFactory<SBool>, BinaryGreaterThanProtocolFactory,
		BinaryEqualityCircuitFactory, CompareAndSwapCircuitFactory,
		OddEvenMergeSortFactory, BitIncrementerCircuitFactory {
	/**
	 * Advanced circuits - compare and swap functionality
	 */

	@Override
	public CompareAndSwapCircuit getCompareAndSwapCircuit(SBool[] left,
			SBool[] right) {
		return new CompareAndSwapCircuitImpl(left, right, this);
	}

	@Override
	public KeyedCompareAndSwapCircuit getKeyedCompareAndSwapCircuit(
			SBool[] leftKey, SBool[] leftValue, SBool[] rightKey,
			SBool[] rightValue) {
		// TODO Auto-generated method stub
		// return new KeyedCompareAndSwapCircuitImpl(leftKey, leftValue,
		// rightKey,
		return new KeyedCompareAndSwapCircuitGetNextGatesImpl(leftKey,
				leftValue, rightKey, rightValue, this);
	}

	/**
	 * simple circuits - basic functionality
	 */
	@Override
	public SBool[] getSBools(int amount) {
		SBool[] res = new SBool[amount];
		for (int i = 0; i < amount; i++) {
			res[i] = this.getSBool();
		}
		return res;
	}

	public SBool[] getKnownConstantSBools(boolean[] bools) {
		int amount = bools.length;
		SBool[] res = new SBool[amount];
		for (int i = 0; i < amount; i++) {
			res[i] = this.getKnownConstantSBool(bools[i]);
		}
		return res;
	}

	
	public XnorProtocol getXnorCircuit(SBool left, SBool right, SBool out) {
		return new XnorFromXorAndNotProtocolImpl(left, right, out, this);
	}

	
	public NandProtocol getNandCircuit(SBool left, SBool right, SBool out) {
		return new NandFromAndAndNotProtocolImpl(left, right, out, this);
	}

	
	public OrProtocol getOrCircuit(SBool inLeft, SBool inRight, SBool out) {
		return new OrFromXorAndProtocol(this, this, this, inLeft, inRight, out);
	}

	
	public OrProtocol getOrCircuit(SBool inLeft, OBool inRight, SBool out) {
		return new OrFromCopyConstProtocol(this, this, inLeft, inRight, out);
	}

	@Override
	public AndProtocol getAndCircuit(SBool inLeft, OBool inRight, SBool out) {
		return new AndFromCopyConstProtocol(this, this, inLeft, inRight, out);
	}

	@Override
	public NotProtocol getNotCircuit(SBool in, SBool out) {
		return new NotFromXorProtocol(this, this, in, out);
	}

	/**
	 * Advanced circuits - addition
	 */

	@Override
	public OneBitFullAdderCircuit getOneBitFullAdderCircuit(SBool left,
			SBool right, SBool carry, SBool outS, SBool outCarry) {
		return new OneBitFullAdderCircuitImpl(left, right, carry, outS,
				outCarry, this);
	}

	@Override
	public FullAdderCircuit getFullAdderCircuit(SBool[] lefts, SBool[] rights,
			SBool inCarry, SBool[] outs, SBool outCarry) {
		return new FullAdderCircuitImpl(lefts, rights, inCarry, outs, outCarry,
				this, this);
	}

	@Override
	public BitIncrementerCircuit getBitIncrementerCircuit(SBool[] base,
			SBool increment, SBool[] outs) {
		return new BitIncrementerCircuitImpl(base, increment, outs, this, this);
	}

	@Override
	public OneBitHalfAdderCircuit getOneBitHalfAdderCircuit(SBool left,
			SBool right, SBool outS, SBool outCarry) {
		return new OneBitHalfAdderCircuitImpl(left, right, outS, outCarry, this);
	}

	/**
	 * Advanced circuits - multiplication
	 */

	@Override
	public BinaryMultCircuit getBinaryMultCircuit(SBool[] lefts,
			SBool[] rights, SBool[] outs) {
		return new BinaryMultCircuitImpl(lefts, rights, outs, this, this);
	}

	@Override
	public LogProtocol getLogCircuit(SBool[] number, SBool[] result) {
		return new LogProtocolImpl(number, result, this);
	}

	/**
	 * Advanced circuits - comparisons
	 */

	@Override
	public BinaryGreaterThanProtocol getBinaryComparisonCircuit(SBool[] inLeft,
			SBool[] inRight, SBool out) {
		return new BinaryGreaterThanProtocolImpl(inLeft, inRight, out, this);
		// return new BinaryComparisonCircuitNextGatesImpl(inLeft, inRight, out,
		// this);
		// return new GenericBinaryComparisonCircuit2(this, inLeft, inRight,
		// out);
	}

	@Override
	public BinaryEqualityProtocol getBinaryEqualityCircuit(SBool[] inLeft,
			SBool[] inRight, SBool out) {
		// return new BinaryEqualityCircuitImpl(inLeft, inRight, out, this);
		return new AltBinaryEqualityProtocol(inLeft, inRight, out, this);
	}

	/**
	 * Advanced circuits - sorting
	 */

	@Override
	public OddEvenMergeCircuit getOddEvenMergeCircuit(
			List<Pair<SBool[], SBool[]>> left,
			List<Pair<SBool[], SBool[]>> right,
			List<Pair<SBool[], SBool[]>> sorted) {
		return new OddEvenMergeCircuitRec(left, right, sorted, this);
	}

}
