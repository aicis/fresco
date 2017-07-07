/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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

/**
 * Generic storage interface for the different run-times.
 * 
 * @author Kasper Damgaard
 *
 */
public interface Storage {

	/**
	 * Stores a serializable object under the given key. If the key already
	 * exists, an exception should be thrown. If the name does not exist, an
	 * entry will be created under that name for future use.
	 * 
	 * @param name
	 *            The name of the databaseId/filename that you want to use
	 * @param key
	 *            the id to store by.
	 * @param o
	 *            the (serializable) object to store.
	 * @return true if all went well, false if we could not insert the object in
	 *         the storage.
	 */
	public boolean putObject(String name, String key, Serializable o);

	/**
	 * Returns all objects stored under the given id. If none are stored, null
	 * is returned instead.
	 * 
	 * @param name
	 *            The name of the databaseId/filename that you want to use
	 * @param key
	 *            The id to search for.
	 * @return the object stored under the given id. If the storage contains
	 *         nothing, null is returned.
	 * 
	 */
	public <T extends Serializable> T getObject(String name, String key);

}
