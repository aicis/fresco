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
package dk.alexandra.fresco.framework.sce.resources.storage;

import java.io.Serializable;

import dk.alexandra.fresco.framework.sce.resources.storage.exceptions.NoMoreElementsException;

public interface StreamedStorage extends Storage {

	/**
	 * Returns the next object from the storage with the given name. This could
	 * be e.g. the given filename.
	 * 
	 * @param name
	 *            The name of the storage to get from. This could e.g. be a
	 *            filename.
	 * @return the next object in line
	 */
	public <T extends Serializable> T getNext(String name) throws NoMoreElementsException;

	/**
	 * Inserts an object into the storage with the given name. This could be
	 * e.g. append to a file with the filename as 'name'.
	 * 
	 * @param name
	 *            The storage to put objects into.
	 * @param o
	 *            The object to store.
	 * @return true if the object was stored, false otherwise:
	 */
	public boolean putNext(String name, Serializable o);

	/**
	 * Closes any open connections to the storage.
	 */
	public void shutdown();
}
