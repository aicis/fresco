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

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ot.OTFactory;
import dk.alexandra.fresco.framework.util.ot.extension.semihonest.SemiHonestOTExtensionFactory;
import dk.alexandra.fresco.framework.util.ot.java.JavaOTFactory;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproANDProtocol;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorage;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorageImpl;

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
		this.tinyTablesFile = this.configuration.getTinyTablesFile();
		this.otFactory = new SemiHonestOTExtensionFactory(resourcePool.getNetwork(), resourcePool.getMyId(), 128, 
				new JavaOTFactory(resourcePool.getNetwork(), resourcePool.getMyId(), resourcePool.getSecureRandom()), resourcePool.getSecureRandom());

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
		 * The proprocessing of all AND gates has to be finished.
		 */
		TinyTablesPreproANDProtocol.finishPreprocessing(resourcePool.getMyId(), otFactory, storage,
				resourcePool.getNetwork());
		
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
