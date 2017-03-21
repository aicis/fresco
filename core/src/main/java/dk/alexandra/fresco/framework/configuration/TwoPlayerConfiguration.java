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
package dk.alexandra.fresco.framework.configuration;

import java.util.logging.Level;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Party;

public class TwoPlayerConfiguration implements NetworkConfiguration {
	
	private Party me;
	private Party opposite;
	private Level logLevel;
	
	public TwoPlayerConfiguration(Party me, Party opposite, Level logLevel) {
		if (me == null || opposite == null || logLevel == null) {
			throw new NullPointerException();
		}
		this.me = me;
		this.opposite = opposite;
		this.logLevel = logLevel;
	}	
	
	@Override
	public Party getParty(int i) {
		if (i == me.getPartyId()) {
			return me;
		} else if (i == opposite.getPartyId()) {
			return opposite;
		} else {
			throw new MPCException("There is no party: " + i);
		}
	}

	@Override
	public Party getMe() {
		return me;
	}
	
	
	public Party getOpposite() {
		return opposite;
	}

	@Override
	public int getMyId() {
		return me.getPartyId();
	}

	@Override
	public int noOfParties() {
		return 2;
	}	

	@Override
	public Level getLogLevel() {
		return logLevel;
	}
}
