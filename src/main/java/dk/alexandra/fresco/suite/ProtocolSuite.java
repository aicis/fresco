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
package dk.alexandra.fresco.suite;

import java.util.HashSet;
import java.util.Set;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.bgw.BgwProtocolSuite;
import dk.alexandra.fresco.suite.dummy.DummyProtocolSuite;
import dk.alexandra.fresco.suite.spdz.evaluation.strategy.SpdzProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;

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
	 * Get a RoundSynchronization used by evaluators to signal progress and
	 * allow protocols to do additional work during evaluation.
	 * Only RoundSynchronization.finishedBatch is guaranteed to be called by the evaluator.
	 *
	 * @return a RoundSynchronization that can be used by current evaluation.
	 */
	public RoundSynchronization createRoundSynchronization();

	/**
	 * Let the protocol suite know that the evaluation has reached it's end. Runtime
	 * can then do cleanup or resume background activities if needed.
	 */
	public void finishedEval(ResourcePool resourcePool, SCENetwork sceNetwork);

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
		res.add("tinytablesprepro");
		res.add("tinytables");
		res.add("bgw");
		res.add("spdz");
		return res;
	}

	public static String protocolSuiteToString(ProtocolSuite suite) {
		if(suite instanceof BgwProtocolSuite) {
			return "bgw";
		} else if(suite instanceof SpdzProtocolSuite) {
			return "spdz";
		} else if (suite instanceof TinyTablesPreproProtocolSuite) {
			return "tinytablesprepro";
		} else if (suite instanceof TinyTablesProtocolSuite) {
			return "tinytables";
		} else if (suite instanceof DummyProtocolSuite) {
			return "dummy";
		} else {
			throw new IllegalArgumentException("FRESCO does not currently know about the given protocol suite: " +suite);
		}
	}

	public interface RoundSynchronization {
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
		 * @param sceNetwork
		 */
		public void finishedBatch(int gatesEvaluated, ResourcePool resourcePool, SCENetwork sceNetwork) throws MPCException;

        /**
         * Method called after a round has finished.
         * Return true if round synchronization is finished. The method might be called in future rounds as well.
         * If false is returned the method is guaranteed to be called with next round as well,
         * even if no more protocols are evaluated during the actual computation.
         *
         * @return true if round synchronization has finished.
         * @throws MPCException on error
         */
        public boolean roundFinished(
                int round, ResourcePool resourcePool, SCENetwork network) throws MPCException;
    }

    /**
     * Dummy round synchronization that does nothing.
     */
    public class DummyRoundSynchronization implements RoundSynchronization {

        @Override
        public void finishedBatch(int gatesEvaluated, ResourcePool resourcePool, SCENetwork sceNetwork) throws MPCException {

        }

        @Override
        public boolean roundFinished(int round, ResourcePool resourcePool, SCENetwork network) throws MPCException {
            return true;
        }
    }
}
