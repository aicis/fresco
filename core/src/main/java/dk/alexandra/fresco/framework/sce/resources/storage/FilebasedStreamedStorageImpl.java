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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.sce.resources.storage.exceptions.NoMoreElementsException;

/**
 * Streamed Storage based on files.
 * @author Kasper Damgaard
 *
 */
public class FilebasedStreamedStorageImpl implements StreamedStorage {
	
	private ConcurrentHashMap<String, ObjectInputStream> oiss;
	private ConcurrentHashMap<String, ObjectOutputStream> ooss;
	private Storage storage;
	
	public FilebasedStreamedStorageImpl(Storage internalStorage) {
		this.storage = internalStorage;
		oiss = new ConcurrentHashMap<String, ObjectInputStream>();
		ooss = new ConcurrentHashMap<String, ObjectOutputStream>();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T getNext(String name) throws NoMoreElementsException {
		if(!oiss.containsKey(name)) {			
			FileInputStream fis;
			try {
				fis = new FileInputStream(name);
			} catch (FileNotFoundException e) {
				throw new MPCException("File with filename '"+name+"' not found.");
			}
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(fis);
			} catch (IOException e) {
				e.printStackTrace();
				if(fis != null) {
					try {
						fis.close();
					} catch (IOException e1) {
					}
				}
				throw new MPCException("IOException: "+e.getMessage());
			}
			oiss.put(name, ois);			
		}
		try {
			return (T) oiss.get(name).readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new MPCException("Class not found: " + e.getMessage());
		} catch (IOException e) {
			Reporter.severe("IO-Exception. Could not read object from: "+name+". This is most likely because there are no more elements available.");
			throw new NoMoreElementsException("IOException - most likely because there are no more elements available." , e);
		}
		
	}

	@Override
	public boolean putNext(String name, Serializable o) {
		if(!ooss.containsKey(name)) {
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(name);
			} catch (FileNotFoundException e) {
				throw new MPCException("File with filename '"+name+"' not found.");
			}
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(fos);
			} catch (IOException e) {
				e.printStackTrace();
				if(fos != null) {
					try {
						fos.close();
					} catch (IOException e1) {
					}
				}
				throw new MPCException("IOException: "+e.getMessage());
			}
			ooss.put(name, oos);			
		}		
		try {
			ooss.get(name).writeObject(o);
		} catch (IOException e) {
			e.printStackTrace();
			throw new MPCException("IOException: " + e.getMessage());
		}
		return true;
	}

	@Override
	public void shutdown() {
		for(ObjectInputStream ois : oiss.values()) {
			try {
				ois.close();
			} catch (IOException e) {
				//Do nothing - nothing can be done
			}
		}
		
		for(ObjectOutputStream oos : ooss.values()) {
			try {
				oos.close();
			} catch (IOException e) {
				//Do nothing - nothing can be done
			}
		}
	}

	@Override
	public boolean putObject(String name, String key, Serializable o) {
		return this.storage.putObject(name, key, o);
	}

	@Override
	public <T extends Serializable> T getObject(String name, String key) {
		return this.storage.getObject(name, key);
	}

	@Override
	public boolean removeFromStorage(String name, String key) {
		return this.storage.removeFromStorage(name, key);
	}

	@Override
	public boolean removeNameFromStorage(String name) {
		return this.storage.removeNameFromStorage(name);
	}

	
}
