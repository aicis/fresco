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
package dk.alexandra.fresco.suite.tinytables.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.SortedMap;

public class Util {
	
	/**
	 * Given a sorted map with integer keys and entries of type <code>T[]</code>
	 * , this method returns a list of all entries of type <code>T</code> in the
	 * induced ordering.
	 * 
	 * @param map
	 * @return
	 */
	public static <T> List<T> getAll(SortedMap<Integer, T[]> map) {
		List<T> array = new ArrayList<T>();
		for (int i : map.keySet()) {
			T[] entry = map.get(i);
			for (T t : entry) {
				array.add(t);
			}
		}
		return array;
	}
	
	/**
	 * Outputs a hash of j and the given bits of size l. We assume that l < 256
	 * since the underlying hash function is SHA-256.
	 * 
	 * @param j
	 * @param bits
	 * @param l
	 * @return
	 */
	public static BitSet hash(int j, BitSet bits, int l) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		digest.update((byte) j);
		byte[] binary = digest.digest(bits.toByteArray());
		return BitSet.valueOf(binary).get(0, l);
	}
	
	public static int otherPlayerId(int myId) {
		return myId == 1 ? 2 : 1;
	}
}
