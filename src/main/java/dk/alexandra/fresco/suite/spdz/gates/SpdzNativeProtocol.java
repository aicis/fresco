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
package dk.alexandra.fresco.suite.spdz.gates;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.lib.helper.HalfCookedNativeProtocol;

public abstract class SpdzNativeProtocol extends HalfCookedNativeProtocol {
		
	protected byte[] sendBroadcastValidation(MessageDigest dig, SCENetwork network, byte[] b, int players) {
		dig.update(b);
		byte[] digest = dig.digest();
		dig.reset();
		network.sendToAll(digest);		
		return digest;
	}
	
	protected byte[] sendBroadcastValidation(MessageDigest dig, SCENetwork network, List<byte[]> bs, int players) {
		for (byte[] b: bs) {
			dig.update(b);
		}
		byte[] digest = dig.digest();
		dig.reset();
		network.sendToAll(digest);		
		return digest;
	}
	
	protected boolean receiveBroadcastValidation(SCENetwork network, byte[] digest) {
		//TODO: should we check that we get messages from all players?
		boolean validated = true;
		List<byte[]> digests = network.receiveFromAll();
		for (byte[] d : digests) {
			validated = validated && Arrays.equals(d, digest);
		}
		return validated;
	}	
	
	protected Map<Integer, byte[][]> receiveFromAllMap(SCENetwork network) {
		Map<Integer, byte[][]> res = new HashMap<Integer, byte[][]>();
		List<byte[]> values = network.receiveFromAll();
		List<byte[]> randomnesses = network.receiveFromAll();
		for (int i = 0; i < values.size(); i++) {
			byte[][] tmp = new byte[2][];
			tmp[0] = values.get(i);
			tmp[1] = randomnesses.get(i);
			res.put(i+1, tmp);
		}
		return res;
	}	
}
