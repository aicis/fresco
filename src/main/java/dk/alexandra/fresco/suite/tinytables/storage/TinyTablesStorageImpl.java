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

import java.util.concurrent.ConcurrentHashMap;

import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;

public class TinyTablesStorageImpl implements TinyTablesStorage {

	StreamedStorage storage;	
	ConcurrentHashMap<Integer, TinyTable> map;
	
	public TinyTablesStorageImpl(StreamedStorage storage) {
		this.storage = storage;
		map = new ConcurrentHashMap<>();
	}

	@Override
	public boolean lookupTinyTable(int id, boolean... inputs) {
		throw new UnsupportedOperationException("Not implemented, yet.");
	}

	@Override
	public TinyTable getTinyTable(int id) {
		throw new UnsupportedOperationException("Not implemented, yet.");
	}

	@Override
	public void storeTinyTable(int id, TinyTable table) {
		throw new UnsupportedOperationException("Not implemented, yet.");		
	}

	@Override
	public void storeMaskShare(int id, boolean r) {
		throw new UnsupportedOperationException("Not implemented, yet.");		
	}

	@Override
	public boolean getMaskShare(int id) {
		throw new UnsupportedOperationException("Not implemented, yet.");		
	}
	
}
