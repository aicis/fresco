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

public enum StorageStrategy {

	IN_MEMORY,
	STREAMED_STORAGE,
	MYSQL;
	
	public static Storage fromString(String storageString) {
		final String ss = storageString.toUpperCase();
		switch(ss) {
		case "IN_MEMORY":
		case "INMEMORY":
			return new InMemoryStorage();
		case "MYSQL":
		case "MY_SQL":
			return MySQLStorage.getInstance();
		case "STREAMED_STORAGE":
		case "FILE_BASED_STORAGE":
			return new FilebasedStreamedStorageImpl(new InMemoryStorage());
		default:
			return null;
		}
	}

	public static String storageToString(Storage storage) {
		if(storage instanceof InMemoryStorage) {
			return IN_MEMORY.name();
		} else if(storage instanceof MySQLStorage) {
			return MYSQL.name();
		} else if(storage instanceof FilebasedStreamedStorageImpl){
			return STREAMED_STORAGE.name();
		} else {
			return null;
		}
	}
}
