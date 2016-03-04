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
package dk.alexandra.fresco.suite.bgw;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dk.alexandra.fresco.lib.helper.HalfCookedNativeProtocol;

public abstract class BgwProtocol extends HalfCookedNativeProtocol {

	protected ShamirShare[] receiveFromAll(Map<Integer, Serializable> input) {
		ShamirShare[] res = new ShamirShare[input.size()];
		for (int i : input.keySet()) {
			res[i-1] = (ShamirShare) input.get(i);
		}
		return res;
	}	
	
	protected byte[][] sharesToBytes(ShamirShare[] shares) {
		byte[][] res = new byte[shares.length][];
		for(int i =0; i < shares.length; i++) {
			res[i] = shares[i].getPayload();
		}
		return res;
	}
	
	protected List<ShamirShare> bytesToShares(List<byte[]> bytes) {
		List<ShamirShare> shares = new ArrayList<>();
		for(byte[] b : bytes) {
			shares.add(new ShamirShare(b));
		}
		return shares;
	}
}
