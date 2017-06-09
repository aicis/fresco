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
package dk.alexandra.fresco.framework.util;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.NativeProtocol;
import java.util.HashMap;
import java.util.Stack;

public class Counter {

	private Counter(){
		//Never instantiated. Thought as a static class
	}
	
	private static HashMap<Class, Integer> instantiations = new HashMap<Class, Integer>();
	private static HashMap<String, String> evaluationSnapshots = new HashMap<String, String>();
	private static Stack<NativeProtocol> stack = new Stack<NativeProtocol>();
	private static boolean record = false;

	public static void register(NativeProtocol c) {
		if (!record) {return;}
		synchronized (instantiations) {
			if(instantiations.containsKey(c.getClass())){
				instantiations.put(c.getClass(), instantiations.get(c.getClass())+1);
			}
			else{
				instantiations.put(c.getClass(), 1);
			}
		}
	}

	public static void entering(NativeProtocol c) {
		if (!record) {return;}
		stack.push(c);
	}

	public static void leaving(NativeProtocol c) {
		if (!record) {return;}
		if(stack.pop() != c){
			throw new MPCException("Object are not alike. Someone forgot to enter or hiding in the closet");
		}
	}

	public static void recordEval(NativeProtocol c, String hashCodes) {
		if (!record) {return;}
		String stackRecord = stack.toString();
		//evaluationSnapshots.put(c.hashCode(), stackRecord + " - " + hashCodes);
		evaluationSnapshots.put(c.toString(), stackRecord + " - " + hashCodes);
	}
	
	public static HashMap<Class, Integer> getInstantiations(){
		return instantiations;
	}
	
	public static HashMap<String, String> getEvaluationSnapshots(){
		return evaluationSnapshots;
	}
	
	public static void reset(){
		if (!record) {return;}
		instantiations = new HashMap<Class, Integer>();
		evaluationSnapshots = new HashMap<String, String>();
		stack = new Stack<NativeProtocol>();
	}
}
