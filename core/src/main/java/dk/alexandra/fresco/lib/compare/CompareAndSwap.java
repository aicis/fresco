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
package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.ComputationBuilder;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.List;


public class CompareAndSwap implements ComputationBuilder<List<SBool>, ProtocolBuilderNumeric> {

	private Computation<List<SBool>> left;
	private Computation<List<SBool>> right;

	public CompareAndSwap(Computation<List<SBool>> left, Computation<List<SBool>> right) {
		this.left = left;
		this.right = right;
	}
/*
	@Override
	protected ProtocolProducer initializeProtocolProducer() {
		BasicLogicBuilder blb = new BasicLogicBuilder(bp);
		blb.beginSeqScope();
		SBool comparisonResult = blb.greaterThan(left, right);

		blb.beginParScope();
		SBool[] tmpLeft = blb.condSelect(comparisonResult, left, right);
		SBool[] tmpRight = blb.condSelect(comparisonResult, right, left);
		blb.endCurScope();

		blb.beginParScope();
		blb.copy(tmpLeft, left);
		blb.copy(tmpRight, right);
		blb.endCurScope();

		blb.endCurScope();
		return blb.getProtocol();
	}*/

  @Override
	public Computation<List<SBool>> build(ProtocolBuilderNumeric builder) {
		// TODO Auto-generated method stub
    return null;
  }
}
