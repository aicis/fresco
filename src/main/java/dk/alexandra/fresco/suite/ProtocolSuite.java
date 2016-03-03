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
package dk.alexandra.fresco.suite;

import java.util.HashSet;
import java.util.Set;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.bgw.BgwProtocolSuite;
import dk.alexandra.fresco.suite.dummy.DummyProtocolSuite;
import dk.alexandra.fresco.suite.ninja.NinjaProtocolSuite;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;

public interface ProtocolSuite {

	/**
	 * Initializes the protocol suite by supplying any needed
	 * resources to the protocol suite. The protocol invocation implementation is then
	 * in charge of supplying the needed resources to it's internal protocols
	 * when needed.
	 * 
	 * @param resourcePool
	 * @param conf
	 *            The configuration specific to this protocol suite.
	 */
	public void init(ResourcePool resourcePool, ProtocolSuiteConfiguration conf);

	/**
	 * Let's the protocol suite know that now is a possible point of synchronization.
	 * The invariant is that all threads are done executing. This means that no
	 * network connections are busy any more as all gates up until now has been
	 * evaluated.
	 * 
	 * @param gatesEvaluated
	 *            Indicates how many gates was evaluated since last call to
	 *            synchronize. It is therefore _not_ indicative of a total
	 *            amount.
	 */
	public void synchronize(int gatesEvaluated) throws MPCException;

	/**
	 * Let the protocol suite know that the evaluation has reached it's end. Runtime
	 * can then do cleanup or resume background activities if needed.
	 */
	public void finishedEval();

	/**
	 * Sends a signal to the protocol suite to shut down any running threads and
	 * close open streams and similar.
	 */
	public void destroy();
	
	
	

	
	/**
	 * The protocol suites currently supported by the framework.
	 *
	 */
	public static Set<String> getSupportedProtocolSuites() {
		Set<String> res = new HashSet<String>();
		res.add("dummy");
		res.add("ninja");
		res.add("bgw");
		res.add("spdz");
		return res;
	}
	
	public static String protocolSuiteToString(ProtocolSuite suite) {
		if(suite instanceof BgwProtocolSuite) {
			return "bgw";
		} else if(suite instanceof SpdzProtocolSuite) {
			return "spdz";
		} else if(suite instanceof NinjaProtocolSuite) {
			return "ninja";
		} else if(suite instanceof DummyProtocolSuite) {
			return "dummy";
		} else {
			throw new IllegalArgumentException("FRESCO does not currently know about the given protocol suite: " +suite);
		}
	}
	
}
