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
package dk.alexandra.fresco.lib.helper;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import dk.alexandra.fresco.framework.NativeProtocol;

public abstract class HalfCookedNativeProtocol implements NativeProtocol {

	private boolean evaluated = false;

	@Override
	public int getNextProtocols(NativeProtocol[] nativeProtocols, int pos) {
		if (pos + 1 >= nativeProtocols.length) {
			return pos;
		}
		if (evaluated) {
			return pos;
		} else {
			evaluated = true;
			nativeProtocols[pos++] = this;
			return pos;
		}
	}

	@Override
	public boolean hasNextProtocols() {
		return !evaluated;
	}

	/* Helper methods for network communication */

	protected void sendToAll(Map<Integer, Serializable> output,
			Serializable[] toSend) {
		for (int i = 0; i < toSend.length; i++) {
			output.put(i + 1, toSend[i]);
		}
	}

	protected void sendToAll(Map<Integer, Serializable> output,
			Serializable toSend, int noOfPlayers) {
		for (int i = 0; i < noOfPlayers; i++) {
			output.put(i + 1, toSend);
		}
	}

	protected void expectFromAll(int noOfPlayers,
			Set<Integer> expectedInputForNextRound) {
		for (int i = 1; i <= noOfPlayers; i++) {
			expectedInputForNextRound.add(i);
		}
	}
}
