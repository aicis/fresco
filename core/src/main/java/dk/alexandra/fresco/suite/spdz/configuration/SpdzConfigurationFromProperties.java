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
package dk.alexandra.fresco.suite.spdz.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.framework.sce.util.Util;

public class SpdzConfigurationFromProperties implements SpdzConfiguration {
	
	private Properties prop;
	private final String defaultPropertiesLocation = "properties/spdz/spdz.properties";
	
	public SpdzConfigurationFromProperties() {
		InputStream is;
		try {
			is = Util.getInputStream(defaultPropertiesLocation);		
			prop = new Properties();		
			prop.load(is);			
		} catch (IOException e) {
			throw new MPCException("Could not locate the SPDZ properties file. ", e);
		}
	}

	@Override
	public int getMaxBitLength() {
		return Integer.parseInt(prop.getProperty("maxBitLength"));		
	}

	@Override
	public PreprocessingStrategy getPreprocessingStrategy() {
		return PreprocessingStrategy.fromString(prop.getProperty("preprocessingStrategy"));
	}

	@Override
	public String fuelStationBaseUrl() {
		return prop.getProperty("fuelStationBaseUrl", null);
	}
	
}
