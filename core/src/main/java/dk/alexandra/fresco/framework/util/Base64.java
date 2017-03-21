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

/**
 * Everyone using base64 encoding within FRESCO should use this class to ensure
 * compatibility with the rest of FRESCO. Currently the underlying
 * encoder/decoder used is java's own Base64 class found within
 * java.util.Base64.
 * 
 * @author Kasper Damgaard
 *
 */
public class Base64 {

	public static byte[] encode(byte[] bytesToEncode) {
		return java.util.Base64.getEncoder().encode(bytesToEncode);
	}

	public static byte[] decode(byte[] bytesToDecode) {
		return java.util.Base64.getDecoder().decode(bytesToDecode);
	}

	public static byte[] decodeFromString(String base64EncodedString) {
		return java.util.Base64.getDecoder().decode(base64EncodedString);
	}

	public static String encodeToString(byte[] bytesToEncode) {
		return java.util.Base64.getEncoder().encodeToString(bytesToEncode);
	}

}
