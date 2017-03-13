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
import java.util.HashMap;
import java.util.Map;

public class InMemoryStorage implements Storage{

	private Map<String, Map<String, Serializable>> objects;
	
	public InMemoryStorage() {
		this.objects = new HashMap<String, Map<String, Serializable>>();
	}
	
	@Override
	public boolean putObject(String name, String key, Serializable o) {
		Map<String, Serializable> table = this.objects.get(name);
		if(table == null) {
			this.objects.put(name, new HashMap<String, Serializable>());
			table = this.objects.get(name);
		}				
		table.put(key, o);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T getObject(String name, String key) {
		Map<String, Serializable> table = this.objects.get(name);
		if(table == null) {
			return null;
		}
		return (T) table.get(key);
	}

	@Override
	public boolean removeFromStorage(String name, String key) {
		Map<String, Serializable> table = this.objects.get(name);
		if(table == null) {
			return false;
		}
		Serializable o = table.remove(key);
		if(o == null) {
			return false;
		}
		return true;
	}

	@Override
	public boolean removeNameFromStorage(String name) {
		Map<String, Serializable> table = this.objects.get(name);
		if(table == null) {
			return false;
		}
		this.objects.remove(name);
		return true;
	}

}
