/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.tinytables.prepro;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproANDProtocol;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTable;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesDummyStorage;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorage;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorageImpl;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTReceiver;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTSender;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTSigma;

/**
 * This protocol suite is meant for the preprocessing phase of the TinyTables
 * protocol. Here each player picks his additive share of a mask for each wite
 * in a protocol, and for each AND protocol, each of the two players must also
 * calculate a so-called TinyTable which is used in the online phase. The
 * masking values and TinyTables are stored in a {@link TinyTablesStorage} which
 * can be stored for later use in the online phase.
 * 
 * Since all the values found during the preprocessing phase is saved with a
 * protocols ID as key, it is important that the protocol is created exactly the
 * same way in the preprocessing and online phases.
 * 
 * @author jonas
 *
 */
public class TinyTablesPreproProtocolSuite implements ProtocolSuite {

	private TinyTablesStorage storage;
	private ResourcePool resourcePool;
	private static volatile Map<Integer, TinyTablesPreproProtocolSuite> instances = new HashMap<>();

	public static TinyTablesPreproProtocolSuite getInstance(int id) {
		if (instances.get(id) == null) {
			instances.put(id, new TinyTablesPreproProtocolSuite(id));
		}
		return instances.get(id);
	}

	private TinyTablesPreproProtocolSuite(int id) {
		this.storage = new TinyTablesDummyStorage(id);
	}

	@Override
	public void init(ResourcePool resourcePool, ProtocolSuiteConfiguration conf) {
		TinyTablesPreproConfiguration tinyTablePreproConfig = (TinyTablesPreproConfiguration) conf;
		this.resourcePool = resourcePool;

		if (!tinyTablePreproConfig.useDummy()) {
			this.storage = new TinyTablesStorageImpl(resourcePool.getStreamedStorage());
		}
	}

	public TinyTablesStorage getStorage() {
		return this.storage;
	}

	public void setStorage(TinyTablesStorage storage) {
		this.storage = storage;
	}

	@Override
	public void synchronize(int gatesEvaluated) throws MPCException {
		// TODO Auto-generated method stub
	}

	@Override
	public void finishedEval() {
		/*
		 * Finished preprocessing - now player 2 has to calculate his TinyTable.
		 * This requires doing two OT's for each AND-protocol. The inputs for
		 * the OT's was calculated and stored during the preprocessing.
		 */

		if (resourcePool.getMyId() == 1) {
			/*
			 * Player 1
			 */

			/*
			 * Each AND-gate has stored inputs for two OT's in the storage.
			 */
			LinkedHashMap<Integer, OTInput[]> inputsFromPrepro = storage.getOTInputs();

			/*
			 * Create an array of inputs to the OT's stored during
			 * preprocessing. Note that the order of the sigmas and the order of
			 * the inputs from the sender should be the same.
			 */
			int numberOfOts = 2 * inputsFromPrepro.keySet().size(); // Two OT's per AND-protocol
			OTInput[] inputs = new OTInput[numberOfOts];

			int i = 0;
			for (int id : inputsFromPrepro.keySet()) {
				OTInput[] inputsForProtocol = inputsFromPrepro.get(id);
				for (OTInput x : inputsForProtocol) {
					inputs[i++] = x;
				}
			}

			/*
			 * Perform OT's with player 2
			 */
			// TODO: Get host and port from configuration
			OTSender.transfer("localhost", 9005, inputs);

		} else {
			/*
			 * Player 2
			 */

			/*
			 * Create an array from the sigmas stored during preprocessing.
			 * Note that the order of the sigmas and the order of the inputs
			 * from the sender should be the same.
			 */
			LinkedHashMap<Integer, OTSigma[]> sigmasFromPrepro = storage.getOTSigmas();
			int numberOfOts = sigmasFromPrepro.size() * 2; // Two OT's per AND-protocol
			OTSigma[] sigmas = new OTSigma[numberOfOts];
			int i = 0;
			for (int id : sigmasFromPrepro.keySet()) {
				OTSigma[] sigmasForProtocol = sigmasFromPrepro.get(id);
				for (OTSigma s : sigmasForProtocol) {
					sigmas[i++] = s;
				}
			}

			/*
			 * Do OT's with player 1.
			 */
			// TODO: Get host and port from configuration
			boolean[] outputs = OTReceiver.transfer("localhost", 9005, sigmas);

			int progress = 0;
			for (int id : sigmasFromPrepro.keySet()) {

				/*
				 * Two OT's per AND gate
				 */
				boolean rV = sigmas[progress].getSigma();
				boolean output0 = outputs[progress];
				boolean rU = sigmas[progress + 1].getSigma();
				boolean output1 = outputs[progress + 1];

				/*
				 * We stored our share of r_O and player 1's s_00 + s_01 + rU^1,
				 * s_00 + s_10 + rV^1 and s_00 + s_11 + rU^1 + rV^1 as tmp's
				 * during preprocessing. Here rU^1 is player 1's share of rU
				 * (left input wire) (likewise for rV) and s_ij is the ij'th
				 * entry of player 1's TinyTable.
				 */
				boolean[] tmps = storage.getTemporaryBooleans(id);
				boolean rO = tmps[0];
				boolean[] y = new boolean[] { tmps[1], tmps[2], tmps[3] };

				TinyTable tinyTable = TinyTablesPreproANDProtocol.calculateTinyTable(output0, output1, rU, rV, rO, y);
				storage.storeTinyTable(id, tinyTable);

				/*
				 * For each protocol, we do two OTs so the index need to be
				 * increased by two.
				 */
				progress += 2;
			}
		}

	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

}
