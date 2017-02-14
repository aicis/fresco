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

public class SimpleLPPrefix implements LPPrefix {
	
	private final Matrix<SInt> updateMatrix;
	private final LPTableau tableau;
	private final SInt pivot;
	private ProtocolProducer prefix;
	
	public SimpleLPPrefix(Matrix<SInt> updateMatrix, LPTableau tableau, SInt pivot, ProtocolProducer prefix) {
		this.updateMatrix = updateMatrix;
		this.tableau = tableau;
		this.pivot = pivot;
		this.prefix = prefix;
	}
	
	@Override
	public ProtocolProducer getPrefix() {
		return prefix;
	}

	@Override
	public LPTableau getTableau() {
		return tableau;
	}

	@Override
	public Matrix<SInt> getUpdateMatrix() {
		return updateMatrix;
	}

	@Override
	public SInt getPivot() {
		return pivot;
	}
}
