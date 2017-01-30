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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.BitVector;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.ot.OTFactory;
import dk.alexandra.fresco.framework.util.ot.base.BaseOTFactory;
import dk.alexandra.fresco.framework.util.ot.extension.SemiHonestOTExtensionFactory;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTable;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElementVector;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesTriple;
import dk.alexandra.fresco.suite.tinytables.online.TinyTablesProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproANDProtocol;
import dk.alexandra.fresco.suite.tinytables.storage.BatchTinyTablesTripleProvider;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorage;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorageImpl;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesTripleProvider;
import dk.alexandra.fresco.suite.tinytables.util.TinyTablesTripleGenerator;
import dk.alexandra.fresco.suite.tinytables.util.Util;

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
	private TinyTablesPreproConfiguration configuration;
	private File tinyTablesFile;
	private TinyTablesTripleProvider tinyTablesTripleProvider;
	private List<TinyTablesPreproANDProtocol> unprocessedAndGates;
	private ResourcePool resourcePool;
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
		this.configuration = (TinyTablesPreproConfiguration) configuration;
		this.tinyTablesFile = this.configuration.getTinyTablesFile();

		OTFactory otFactory = new SemiHonestOTExtensionFactory(resourcePool.getNetwork(),
				resourcePool.getMyId(), 128, new BaseOTFactory(resourcePool.getNetwork(),
						resourcePool.getMyId(), resourcePool.getSecureRandom()),
				resourcePool.getSecureRandom());
		
		this.tinyTablesTripleProvider = new BatchTinyTablesTripleProvider(
				new TinyTablesTripleGenerator(resourcePool.getMyId(),
						resourcePool.getSecureRandom(), otFactory), 10000);
		
		this.unprocessedAndGates = Collections
				.synchronizedList(new ArrayList<TinyTablesPreproANDProtocol>());

		this.resourcePool = resourcePool;
	}

	public TinyTablesStorage getStorage() {
		return this.storage;
	}

	public void addANDGate(TinyTablesPreproANDProtocol gate) {
		this.unprocessedAndGates.add(gate);
	}

	public TinyTablesTripleProvider getTinyTablesTripleProvider() {
		return this.tinyTablesTripleProvider;
	}

	@Override
	public void synchronize(int gatesEvaluated) throws MPCException {
		/*
		 * When 1000 AND gates needs to be processed, we do it.
		 */
		if (this.unprocessedAndGates.size() > 1000) {
			calculateTinyTablesForUnprocessedANDGates();
		}
	}

	private void calculateTinyTablesForUnprocessedANDGates() {
		try {
			int unprocessedGates = this.unprocessedAndGates.size();

			/*
			 * Sort the unprocessed gates to make sure that the players
			 * process them in the same order.
			 */
			this.unprocessedAndGates.sort(new Comparator<TinyTablesPreproANDProtocol>() {
				@Override
				public int compare(TinyTablesPreproANDProtocol o1,
						TinyTablesPreproANDProtocol o2) {
					return Integer.compare(o1.getId(), o2.getId());
				}
			});

			// Two bits per gate
			TinyTablesElementVector shares = new TinyTablesElementVector(unprocessedGates * 2);
			List<TinyTablesTriple> usedTriples = new ArrayList<TinyTablesTriple>();
			for (int i = 0; i < unprocessedGates; i++) {
				TinyTablesPreproANDProtocol gate = this.unprocessedAndGates.get(i);
				TinyTablesTriple triple = this.tinyTablesTripleProvider.getNextTriple();
				usedTriples.add(triple);

				/*
				 * Calculate temp values e, d for multiplication. These should
				 * be opened before calling finalize.
				 */
				Pair<TinyTablesElement, TinyTablesElement> msg = gate.getInRight().getValue().multiply(gate.getInLeft().getValue(), triple);
				
				shares.setShare(2 * i, msg.getFirst().getShare());
				shares.setShare(2 * i + 1, msg.getSecond().getShare());
			}

			this.resourcePool.getNetwork().send("0",
					Util.otherPlayerId(resourcePool.getMyId()), shares);
			TinyTablesElementVector otherShares = this.resourcePool.getNetwork().receive("0",
					Util.otherPlayerId(resourcePool.getMyId()));
			
			BitVector open = TinyTablesElementVector.open(shares, otherShares);
			
			for (int i = 0; i < unprocessedGates; i++) {
				TinyTablesPreproANDProtocol gate = this.unprocessedAndGates.get(i);
				boolean e = open.get(2*i);
				boolean d = open.get(2*i + 1);

				TinyTablesElement product = TinyTablesElement.finalizeMultiplication(e, d,
						usedTriples.get(i), this.resourcePool.getMyId());
				
				TinyTable tinyTable = gate.calculateTinyTable(this.resourcePool.getMyId(), product);
				
				this.storage.storeTinyTable(gate.getId(), tinyTable);
			}
			
			this.unprocessedAndGates.clear();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void finishedEval() {
		calculateTinyTablesForUnprocessedANDGates();
		tinyTablesTripleProvider.close();
		/*
		 * Store the TinyTables to a file.
		 */
		try {
			storeTinyTables(storage, this.tinyTablesFile);
			Reporter.info("TinyTables stored to " + this.tinyTablesFile);
		} catch (IOException e) {
			Reporter.severe("Failed to save TinyTables: " + e.getMessage());
		}

	}

	private void storeTinyTables(TinyTablesStorage tinyTablesStorage, File file) throws IOException {
		file.createNewFile();
		FileOutputStream fout = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(tinyTablesStorage);
		oos.close();
	}

	@Override
	public void destroy() {
	}

}
