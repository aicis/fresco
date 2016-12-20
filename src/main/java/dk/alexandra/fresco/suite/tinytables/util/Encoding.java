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

import java.util.ArrayList;
import java.util.List;

public class Encoding {

	/**
	 * Encode a boolean as a <code>byte</code>. We encode <code>true</code> as 1
	 * and <code>false</code> as 0.
	 * 
	 * @param b
	 * @return
	 */
	public static byte encodeBoolean(boolean b) {
		return b ? (byte) 0x01 : (byte) 0x00;
	}
	
	/**
	 * Encode array of booleans as bytes. See also {@link #encodeBoolean(boolean)}.
	 * 
	 * @param b
	 * @return
	 */
	public static byte[] encodeBooleans(List<Boolean> booleans) {
		byte[] bytes = new byte[booleans.size()];
		for (int i = 0; i < booleans.size(); i++) {
			bytes[i] = encodeBoolean(booleans.get(i));
		}
		return bytes;
	}
	
	/**
	 * Decode a byte-encoded boolean. See {@link #encodeBoolean(boolean)}.
	 * 
	 * @param b
	 * @return
	 */
	public static boolean decodeBoolean(byte b) {
		return b != 0x00 ? true : false;
	}
	
	/**
	 * Decode an array of byte-encoded booleans. See also
	 * {@link #decodeBoolean(byte)}.
	 * 
	 * @param b
	 * @return
	 */
	public static List<Boolean> decodeBooleans(byte[] bytes) {
		List<Boolean> booleans = new ArrayList<Boolean>();
		for (byte b : bytes) {
			booleans.add(decodeBoolean(b));
		}
		return booleans;
	}
	
}
