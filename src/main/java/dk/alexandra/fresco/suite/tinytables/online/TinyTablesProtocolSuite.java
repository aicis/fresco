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
package dk.alexandra.fresco.suite.tinytables.online;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesANDProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesCloseProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesNOTProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesOpenToAllProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesXORProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproProtocolSuite;
import dk.alexandra.fresco.suite.tinytables.storage.TinyTablesStorage;

/**
 * <p>
 * This protocol suite is intended for the online phase of the TinyTables
 * protocol as created by Ivan Damgård, Jesper Buus Nielsen and Michael Nielsen
 * from the Department of Computer Science at Aarhus University.
 * </p>
 * 
 * <p>
 * When evaluating a protocol in the online phase, it is assumed that the same
 * protocol has been evaluated in the preprocessing phase (see
 * {@link TinyTablesPreproProtocolSuite}), and that all protocols/gates are
 * evaluated in the exact same order. In the preprocessing phase, the two
 * players picked their additive shares of the masks for all wires. In the
 * online phase, the players add actual input values to their share of the mask,
 * and evaluate the protocol. The details on how this is done can be seen in the
 * specific protocols: {@link TinyTablesANDProtocol},
 * {@link TinyTablesCloseProtocol}, {@link TinyTablesNOTProtocol},
 * {@link TinyTablesOpenToAllProtocol} and {@link TinyTablesXORProtocol}.
 * </p>
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 */
public class TinyTablesProtocolSuite implements ProtocolSuite{

	private TinyTablesStorage storage;
	private static volatile Map<Integer, TinyTablesProtocolSuite> instances = new HashMap<>();	
	
	public static TinyTablesProtocolSuite getInstance(int id) {
		if(instances.get(id) == null) {			
			instances.put(id, new TinyTablesProtocolSuite());
		}
		return instances.get(id);
	}
	
	@Override
	public void init(ResourcePool resourcePool, ProtocolSuiteConfiguration conf) {
		try {
			File tinyTablesFile = ((TinyTablesConfiguration) conf).getTinyTablesFile();
			this.storage = loadTinyTables(tinyTablesFile);
		} catch (ClassNotFoundException e) {
		} catch (IOException e) {
			Reporter.severe("Failed to load TinyTables: " + e.getMessage());
		}
	}

	private TinyTablesStorage loadTinyTables(File file) throws IOException,
			ClassNotFoundException {
		FileInputStream fin = new FileInputStream(file);
		ObjectInputStream is = new ObjectInputStream(fin);
		Reporter.info("Loading TinyTabels from " + file);
		TinyTablesStorage storage = (TinyTablesStorage) is.readObject();
		is.close();
		return storage;
	}
	
	public TinyTablesStorage getStorage() {
		return this.storage;
	}

	@Override
	public RoundSynchronization createRoundSynchronization() {
		return new DummyRoundSynchronization();
	}

	@Override
	public void finishedEval() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
