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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproANDProtocol;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTable;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorage;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorageImpl;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTFactory;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTReceiver;
import dk.alexandra.fresco.suite.tinytables.util.ot.OTSender;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTSigma;
import dk.alexandra.fresco.suite.tinytables.util.ot.extension.OTExtensionFactory;
import dk.alexandra.fresco.suite.tinytables.util.ot.java.JavaOTFactory;

/**
 * <p>
 * This protocol suite is intended for the preprocessing phase of the TinyTables
 * protocol as created by Ivan Damgård, Jesper Buus Nielsen and Michael Nielsen
 * from the Department of Computer Science at Aarhus University.
 * </p>
 * 
 * <p>
 * The TinyTables protocol has to phases - a <i>preprocessing</i> and an
 * <i>online</i> phase. In the preprocessing phase, each of the two players
 * picks his additive share of a mask for each input wire of a protocol.
 * Furthermore, for each AND protocol each of the two players must also
 * calculate a so-called <i>TinyTable</i> which is used in the online phase (see
 * {@link TinyTablesProtocolSuite}). This is done using oblivious transfer. To
 * enhance performance, all oblivious transfers are done at the end of the
 * preprocessing (see {@link TinyTablesPreproProtocolSuite#finishedEval()}).
 * </p>
 * 
 * <p>
 * The masking values and TinyTables are stored in a {@link TinyTablesStorage}
 * which can be stored for later use in the online phase. In order to avoid
 * leaks, you should not reuse the values from a preprocessing in multiple
 * evaluations of a protocol, but should instead preprocess once per evaluation.
 * Note that all the values calculated during the preprocessing phase is saved
 * with a protocols ID as key, which is simply incremented on each created
 * protocol, it is important that the protocols are created in exactly the same
 * order in the preprocessing and online phases.
 * </p>
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class TinyTablesPreproProtocolSuite implements ProtocolSuite {

	private TinyTablesStorage storage;
	private ResourcePool resourcePool;
	private TinyTablesPreproConfiguration configuration;
	private OTFactory otFactory;
	private File tinyTablesFile;
	private static volatile Map<Integer, TinyTablesPreproProtocolSuite> instances = new HashMap<>();

	public static TinyTablesPreproProtocolSuite getInstance(int id) {
		if (instances.get(id) == null) {
			instances.put(id, new TinyTablesPreproProtocolSuite(id));
		}
		return instances.get(id);
	}

	private TinyTablesPreproProtocolSuite(int id) {
		this.storage = TinyTablesStorageImpl.getInstance(id);
	}

	@Override
	public void init(ResourcePool resourcePool, ProtocolSuiteConfiguration configuration) {
		this.resourcePool = resourcePool;
		this.configuration = (TinyTablesPreproConfiguration) configuration;
		negotiateOTExtension();
		this.tinyTablesFile = this.configuration.getTinyTablesFile();
	}

	/**
	 * SCAPI has a fast OT Extension library, but since it is part of SCAPI in
	 * C++ (see https://scapi.readthedocs.io/en/latest/) and is called using
	 * JNI, it requires SCAPI to be installed
	 * (https://scapi.readthedocs.io/en/latest/install.html). If this is not
	 * available for both players, we fall back to the java OT which is also
	 * part of SCAPI. This is much slower, so we recommend that both players
	 * install SCAPI and use the C++ lib.
	 */
	private void negotiateOTExtension() {
		try {
			boolean iHaveOTExtensionLibrary = this.configuration.getUseOtExtension();
			resourcePool.getNetwork().send("0", otherId(), iHaveOTExtensionLibrary);
			boolean otherHasOTExtensionLibrary = resourcePool.getNetwork().receive("0", otherId());
			boolean useOTExtension = iHaveOTExtensionLibrary && otherHasOTExtensionLibrary;

			/*
			 * If we are testing in the same VM, we need to run the OTExtension
			 * lib in seperate processes.
			 */
			this.otFactory = useOTExtension ? new OTExtensionFactory(configuration.getAddress(), configuration.isTesting())
					: new JavaOTFactory(resourcePool.getNetwork(), resourcePool.getMyId());
			
			Reporter.fine("I have OT Extension library: " + iHaveOTExtensionLibrary);
			Reporter.fine("Using OT Extension: " + useOTExtension);
		} catch (IOException e) {
			
		}
	}
	
	private int otherId() {
		return resourcePool.getMyId() == 1 ? 2 : 1;
	}
	
	public TinyTablesStorage getStorage() {
		return this.storage;
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
			SortedMap<Integer, OTInput[]> inputsFromPrepro = storage.getOTInputs();
			
			/*
			 * Create an array of inputs to the OT's stored during
			 * preprocessing. Note that the order of the sigmas and the order of
			 * the inputs from the sender should be the same.
			 */
			int numberOfOts = 2 * inputsFromPrepro.keySet().size(); // Two OT's
																	// per
																	// AND-protocol
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
			OTSender otSender = otFactory.createOTSender();
			otSender.send(inputs);

		} else {
			/*
			 * Player 2
			 */

			/*
			 * Create an array from the sigmas stored during preprocessing. Note
			 * that the order of the sigmas and the order of the inputs from the
			 * sender should be the same.
			 */
			SortedMap<Integer, OTSigma[]> sigmasFromPrepro = storage.getOTSigmas();
			int numberOfOts = sigmasFromPrepro.size() * 2; // Two OT's per
															// AND-protocol
			OTSigma[] sigmas = new OTSigma[numberOfOts];
			int i = 0;
			for (int id : sigmasFromPrepro.keySet()) {
				OTSigma[] sigmasForProtocol = sigmasFromPrepro.get(id);
				for (OTSigma s : sigmasForProtocol) {
					sigmas[i++] = s;
				}
			}

			/*
			 * Do OT's with player 1. During initialization we negotiate with
			 * other player whether we can use OT Extension (which requires the
			 * SCAPI lib to be installed. Otherwise we fall back to the much
			 * slower java version.
			 */
			OTReceiver otReceiver = otFactory.createOTReceiver();
			boolean[] outputs = otReceiver.receive(sigmas);

			if (outputs.length < 2 * sigmasFromPrepro.size()) {
				throw new MPCException("To few outputs from OT's: Expected "
						+ sigmasFromPrepro.size() * 2 + " but got only " + outputs.length);
			}

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

				TinyTable tinyTable = TinyTablesPreproANDProtocol.calculateTinyTable(output0,
						output1, rU, rV, rO, y);
				storage.storeTinyTable(id, tinyTable);

				/*
				 * For each protocol, we do two OTs so the index needs to be
				 * increased by two.
				 */
				progress += 2;
			}
			
		}
		
		try {
			storeTinyTables(storage, this.tinyTablesFile);
			Reporter.info("TinyTables stored to " + this.tinyTablesFile);
		} catch (IOException e) {
			Reporter.severe("Failed to save TinyTables: " + e.getMessage());
		}

	}

	private void storeTinyTables(TinyTablesStorage tinyTablesStorage, File file)
			throws IOException {
		file.createNewFile();
		FileOutputStream fout = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(tinyTablesStorage);
		oos.close();
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

}
