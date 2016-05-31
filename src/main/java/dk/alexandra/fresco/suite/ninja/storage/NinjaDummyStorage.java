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
package dk.alexandra.fresco.suite.ninja.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NinjaDummyStorage implements NinjaStorage{

	private Map<Integer, PrecomputedInputNinja> inputNinjas = new ConcurrentHashMap<>();
	private Map<Integer, PrecomputedNinja> ninjas = new ConcurrentHashMap<>();
	private Map<Integer, PrecomputedOutputNinja> outputNinjas = new ConcurrentHashMap<>();
	
	public NinjaDummyStorage() {
		
	}
	
	@Override
	public boolean lookupNinjaTable(int id, boolean left, boolean right) {
		//System.out.println("Getting ninja for id "+id +" "+ninjas.get(id));
		return ninjas.get(id).lookup(left, right);
	}
	
	@Override
	public boolean lookupNinjaTable(int id, boolean value) {
		//System.out.println("Getting output ninja for id "+id +" "+outputNinjas.get(id));
		return outputNinjas.get(id).lookup(value);
	}

	@Override
	public PrecomputedInputNinja getPrecomputedInputNinja(int id) {
		//System.out.println("Fetching input ninja from "+id+" with thread "+ Thread.currentThread()+", and I am: " + this);
		//this is actually enough - but to check generation of input ninja's, it might be a good idea to pick line 2.
		//return new PrecomputedInputNinja((byte)0, (byte)0);
		return inputNinjas.get(id);
	}

	@Override
	public void storeInputNinja(int id, PrecomputedInputNinja inputNinja) {
		//System.out.println("Storing input ninja: " + id+", "+inputNinja+" with thread "+ Thread.currentThread()+", and I am: " + this);
		inputNinjas.put(id, inputNinja);
	}

	@Override
	public void storeNinja(int id, PrecomputedNinja ninja) {
		//System.out.println("Storing ninja for id "+ id + ": " + ninja);
		ninjas.put(id, ninja);
	}

	@Override
	public void storeOutputNinja(int id, PrecomputedOutputNinja ninja) {
		//System.out.println("Storing Output ninja for id "+ id + ": " + ninja);
		outputNinjas.put(id, ninja);
	}

}
