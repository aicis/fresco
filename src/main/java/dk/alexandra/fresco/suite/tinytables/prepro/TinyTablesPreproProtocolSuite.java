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
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproANDProtocol;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorage;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorageImpl;
import dk.alexandra.fresco.suite.tinytables.util.Util;
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
			this.otFactory = useOTExtension ? new OTExtensionFactory(configuration.getSenderAddress(), configuration.isTesting())
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
			 * Send tmps to player 2
			 */
			try {
				resourcePool.getNetwork().send("0", 2, (Serializable) storage.getTemporaryBooleans());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/*
			 * Perform OT's with player 2
			 */
			List<OTInput> otInputs = Util.getAll(storage.getOTInputs());
			OTSender otSender = otFactory.createOTSender();
			otSender.send(otInputs);
			
		} else {
			/*
			 * Player 2
			 */

			/*
			 * Get tmps from player 1
			 */
			try {
				SortedMap<Integer, boolean[]> in = resourcePool.getNetwork().receive("0", 1);
				storage.getTemporaryBooleans().putAll(in);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			/*
			 * Do OT's with player 1. During initialization we negotiate with
			 * other player whether we can use OT Extension (which requires the
			 * SCAPI lib to be installed. Otherwise we fall back to the much
			 * slower java version.
			 */
			List<OTSigma> sigmas = Util.getAll(storage.getOTSigmas());
			OTReceiver otReceiver = otFactory.createOTReceiver();
			List<Boolean> outputs = otReceiver.receive(sigmas);
			if (outputs.size() < 2 * storage.getOTSigmas().size()) {
				throw new MPCException("To few outputs from OT's: Expected "
						+ storage.getOTSigmas().size() * 2 + " but got only " + outputs.size());
			}
			
			TinyTablesPreproANDProtocol.player2CalculateTinyTables(outputs, storage);
			
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
