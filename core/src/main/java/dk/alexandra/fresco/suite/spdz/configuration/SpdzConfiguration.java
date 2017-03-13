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
package dk.alexandra.fresco.suite.spdz.configuration;

import dk.alexandra.fresco.framework.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;

public interface SpdzConfiguration extends ProtocolSuiteConfiguration {

	/**
	 * Gets an approximation of the maximum bit length of any number appearing 
	 * in an application. This is used by certain protocols, e.g., to avoid overflow 
	 * when working in a Z_p field. 
	 * TODO: Consider factoring out of the configuration as this is really specific
	 * only to certain protocols.
	 * @return the expected maximum bit length of any number appearing in the application.
	 */
	int getMaxBitLength();

	/**
	 * True: system will use dummy data for preprocessed data.
	 * False: System will read data from the FRESCO native storage.
	 * @return The preprocessing strategy to use.
	 */
	public PreprocessingStrategy getPreprocessingStrategy();
	
	/**
	 * Should return the base url (e.g. 154.92.132.12:80) where the fuel station is deployed.
	 * @return The base URL for the fuel station.
	 */
	public String fuelStationBaseUrl();

}
