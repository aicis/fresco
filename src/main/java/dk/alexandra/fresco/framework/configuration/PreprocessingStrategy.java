/*******************************************************************************
 * Copyright (c) 2017 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.framework.configuration;

/**
 * Used by protocol suites which deals with preprocessed material.
 * @author Kasper Damgaard
 *
 */
public enum PreprocessingStrategy {

	DUMMY, // Use a dummy approach (e.g. always the same data)
	STATIC, // Use data already present on the machine it's running on. 
	FUELSTATION; // Use the fuel station tool to obtain data.
	
	public static PreprocessingStrategy fromString(String s) {
		switch(s.toUpperCase()) {
		case "DUMMY":
			return PreprocessingStrategy.DUMMY;
		case "STATIC":
			return PreprocessingStrategy.STATIC;
		case "FUEL":
		case "FUELSTATION":
		case "FUEL_STATION":
			return FUELSTATION;
		}
		throw new IllegalArgumentException("Unkown strategy "+s+". Should be one of the following: DUMMY, STATIC, FUELSTATION");
	}
}
