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

import java.math.BigInteger;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.bgw.configuration.BgwConfiguration;

public class BgwProtocolSuite implements ProtocolSuite {

	private static BgwProtocolSuite instance;
	
	private int threshold;
	private BigInteger modulus;
	
	public BgwProtocolSuite() {
		
	}
	
	public static BgwProtocolSuite getInstance() {
		if(instance == null) {
			instance = new BgwProtocolSuite();
		}
		return instance;
	}
	
	@Override
	public void init(ResourcePool resourcePool, ProtocolSuiteConfiguration conf) {
		BgwConfiguration sconf = (BgwConfiguration)conf;
		this.threshold = sconf.getThreshold();
		this.modulus = sconf.getModulus();
		ShamirShare.setPrimeNumber(modulus);
	}

	@Override
	public void synchronize(int gatesEvaluated) throws MPCException {
		//Do nothing
	}

	@Override
	public void finishedEval() {
		//Do nothing
	}

	@Override
	public void destroy() {
		
	}

	public int getThreshold() {
		return this.threshold;
	}

	public int getMaxBitLength() {
		return modulus.bitLength();
	}

	public BigInteger getModulus() {
		return this.modulus;
	}

	@Override
	public int getMessageSize() {
		return ShamirShare.getSize();
	}

}
