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
package dk.alexandra.fresco.suite.tinytables.storage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTInput;
import dk.alexandra.fresco.suite.tinytables.util.ot.datatypes.OTSigma;

public class TinyTablesDummyStorage implements TinyTablesStorage {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3455535991348570325L;
	private Map<Integer, TinyTable> tinyTables = new ConcurrentHashMap<>();
	private int storageId;
	private Map<Integer, Boolean> maskShares = new ConcurrentHashMap<>();
	
	/*
	 * sigmas, otInputs and tmps are only used during preprocessing, and should not be
	 * serialized.
	 */
	private transient LinkedHashMap<Integer, OTSigma[]> sigmas = new LinkedHashMap<>();	
	private transient LinkedHashMap<Integer, OTInput[]> otInputs = new LinkedHashMap<>();
	private transient Map<Integer, boolean[]> tmps = new ConcurrentHashMap<>();
	
	public TinyTablesDummyStorage(int id) {
		this.storageId = storageId;
	}

	@Override
	public boolean lookupTinyTable(int id, boolean... inputs) {
		TinyTable tinyTable = tinyTables.get(id);
		if (tinyTable == null) {
			throw new IllegalArgumentException("No TinyTable with ID " + id);
		}
		return tinyTable.getValue(inputs);
	}

	@Override
	public TinyTable getTinyTable(int id) {
		return tinyTables.get(id);
	}

	@Override
	public void storeTinyTable(int id, TinyTable table) {
		tinyTables.put(id, table);
	}

	@Override
	public void storeMaskShare(int id, boolean r) {
		maskShares.put(id, r);
	}

	@Override
	public boolean getMaskShare(int id) {
		return maskShares.get(id);
	}

	@Override
	public void storeOTSigma(int id, OTSigma[] sigmas) {
		this.sigmas.put(id, sigmas);
	}

	@Override
	public void storeOTInput(int id, OTInput[] inputs) {
		this.otInputs.put(id, inputs);
	}

	@Override
	public LinkedHashMap<Integer, OTSigma[]> getOTSigmas() {
		return sigmas;
	}

	@Override
	public LinkedHashMap<Integer, OTInput[]> getOTInputs() {
		return otInputs;
	}

	@Override
	public void storeTemporaryBooleans(int id, boolean[] booleans) {
		tmps.put(id, booleans);
	}

	@Override
	public boolean[] getTemporaryBooleans(int id) {
		return tmps.get(id);
	}

}
