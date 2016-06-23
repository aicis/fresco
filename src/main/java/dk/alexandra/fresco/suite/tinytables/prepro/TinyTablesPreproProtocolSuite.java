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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTable;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesDummyStorage;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorage;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorageImpl;
import dk.alexandra.fresco.suite.tinytables.util.Encoding;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTReceiver;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTSender;

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
		 * Finished preprocessing - finishing off with player 2 creates his
		 * TinyTable. This requires doing OT's for each AND-protocol.
		 */

		if (resourcePool.getMyId() == 1) {
			/*
			 * Player 1
			 */

			/*
			 * Each AND-gate has stored inputs for two OT's in the storage.
			 */
			LinkedHashMap<Integer, boolean[][]> inputsFromPrepro = storage.getOTInputs();

			/*
			 * Create a byte array of inputs to the OT's stored during
			 * preprocessing. Note that the order of the sigmas and the order of
			 * the inputs from the sender should be the same.
			 */
			ArrayList<Byte> x0 = new ArrayList<Byte>();
			ArrayList<Byte> x1 = new ArrayList<Byte>();

			for (int id : inputsFromPrepro.keySet()) {
				boolean[][] inputsForProtocol = inputsFromPrepro.get(id);
				for (boolean[] inputs : inputsForProtocol) {
					x0.add(Encoding.encodeBoolean(inputs[0]));
					x1.add(Encoding.encodeBoolean(inputs[1]));
				}
			}

			/*
			 * Convert ArrayList<Byte>'s to byte[]'s and keep the ordering.
			 */
			int numberOfOts = x0.size(); // == x1.size();
			byte[][] inputs = new byte[2][];
			inputs[0] = new byte[numberOfOts];
			inputs[1] = new byte[numberOfOts];
			for (int i = 0; i < numberOfOts; i++) {
				inputs[0][i] = x0.get(i);
				inputs[1][i] = x1.get(i);
			}

			/*
			 * Perform OT's with player 2
			 */
			OTSender.transfer("localhost", 9005, inputs[0], inputs[1]);

		} else {
			/*
			 * Player 2
			 */

			/*
			 * Create a byte array from the sigmas stored during preprocessing.
			 * Note that the order of the sigmas and the order of the inputs
			 * from the sender should be the same.
			 */
			LinkedHashMap<Integer, boolean[]> sigmasFromPrepro = storage.getOTSigmas();
			ArrayList<Byte> sigmasForOt = new ArrayList<Byte>();
			for (int id : sigmasFromPrepro.keySet()) {
				boolean[] sigmasForProtocol = sigmasFromPrepro.get(id);
				for (boolean s : sigmasForProtocol) {
					sigmasForOt.add(Encoding.encodeBoolean(s));
				}
			}

			int numberOfOts = sigmasForOt.size();
			byte[] sigmas = new byte[numberOfOts];
			for (int i = 0; i < numberOfOts; i++) {
				sigmas[i] = sigmasForOt.get(i);
			}

			/*
			 * Do OT's with player 1.
			 */
			byte[] outputs = OTReceiver.transfer("localhost", 9005, sigmas);

			int progress = 0;
			for (int id : sigmasFromPrepro.keySet()) {
				
				// Two OT's per AND-gate
				boolean rV = Encoding.decodeBoolean(sigmas[progress]);
				boolean input0 = Encoding.decodeBoolean(outputs[progress]);
				
				boolean rU = Encoding.decodeBoolean(sigmas[progress + 1]);
				boolean input1 = Encoding.decodeBoolean(outputs[progress + 1]);				
				
				boolean[] tmps = storage.getTemporaryBooleans(id);
				boolean rO = tmps[0];
								
				boolean[] y = new boolean[] { tmps[1], tmps[2], tmps[3] };
				
				boolean[] t = new boolean[4];
				t[0] = input0 ^ input1 ^ (rU && rV) ^ rO;
				t[1] = t[0] ^ y[0] ^ rU;
				t[2] = t[0] ^ y[1] ^ rV;
				t[3] = t[0] ^ y[2] ^ rU ^ rV ^ true;
				
				TinyTable tinyTable = new TinyTable(t);
				storage.storeTinyTable(id, tinyTable);

				progress += 2;
			}
		}

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
