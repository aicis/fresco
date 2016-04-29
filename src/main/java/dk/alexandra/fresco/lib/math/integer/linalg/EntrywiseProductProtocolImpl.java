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
package dk.alexandra.fresco.lib.math.integer.linalg;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.MultByConstantFactory;
import dk.alexandra.fresco.lib.field.integer.MultCircuitFactory;
import dk.alexandra.fresco.lib.helper.AbstractRepeatProtocol;

public class EntrywiseProductProtocolImpl extends AbstractRepeatProtocol implements
		EntrywiseProductProtocol {

	private final MultCircuitFactory provider;
	private final MultByConstantFactory openMultProvider;
	private final SInt[] as, bs, results;
	private final OInt[] publicBs;
	private int limit, i = 0;

	public EntrywiseProductProtocolImpl(SInt[] as, SInt[] bs, SInt[] results,
			MultCircuitFactory provider) {
		if (as.length != bs.length && as.length != results.length) {
			throw new MPCException(
					"Can only compute dot-product with equal length input arrays");
		}
		this.as = as;
		this.bs = bs;
		this.publicBs = null;
		this.results = results;
		this.provider = provider;
		this.openMultProvider = null;
		this.limit = as.length;
	}

	public EntrywiseProductProtocolImpl(SInt[] as, OInt[] bs, SInt[] results,
			MultByConstantFactory openMultProvider) {
		if (as.length != bs.length && as.length != results.length) {
			throw new MPCException(
					"Can only compute dot-product with equal length input arrays");
		}
		this.as = as;
		this.bs = null;
		this.publicBs = bs;
		this.results = results;
		this.provider = null;
		this.openMultProvider = openMultProvider;
		this.limit = as.length;
	}

	protected ProtocolProducer getNextGateProducer() {
		if (i < limit) {
			ProtocolProducer mult;
			if (publicBs != null) {
				mult = openMultProvider.getMultProtocol(publicBs[i], as[i],
						results[i]);
			} else {
				mult = provider.getMultProtocol(as[i], bs[i], results[i]);
			}
			i++;
			return mult;
		} else {
			return null;
		}
	}
}
