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
package dk.alexandra.fresco.suite.bgw.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Properties;

import dk.alexandra.fresco.framework.configuration.ConfigurationException;
import dk.alexandra.fresco.framework.sce.util.Util;

public class BgwConfigurationFromProperties implements BgwConfiguration {

	private final String defaultPropertiesLocation = "properties/bgw/bgw.properties";
	private int threshold;
	private BigInteger modulus;

	public BgwConfigurationFromProperties() {
		InputStream is;
		try {
			is = Util.getInputStream(defaultPropertiesLocation);
			Properties prop = new Properties();
			prop.load(is);
			parseProperties(prop);
		} catch (IOException e) {
			throw new ConfigurationException("Could not locate the BGW properties file. ", e);
		}
	}
	
	private void parseProperties(Properties prop) {
		StringBuilder sb = new StringBuilder();
		boolean failed = false;
		String propName;
		String propString;
		// Required properties
		propName = "bgw.threshold";
		propString = prop.getProperty(propName);
		if (propString != null) {
			try { 
				this.threshold = Integer.parseInt(propString); 
			} catch (NumberFormatException e) {
				failed = true;
				sb.append("Value of property " + propName + " did not parse as a number:" + e.getLocalizedMessage() + "\n");
			}
			if (threshold < 1) {
				failed = true;
				sb.append("Value of property "  + propName + " was " + threshold + " but must be larger then 0.\n");
			}
		}
		// Properties with defaults
		propName = "bgw.modulus";
		propString = prop.getProperty(propName, "618970019642690137449562111");
		try {
			this.modulus = new BigInteger(propString);
		} catch (NumberFormatException e) {
			failed = true;
			sb.append("Value of property " + propName + " did not parse as a number.\n");
		}
		if (!modulus.isProbablePrime(40)) {
			failed = true;
			sb.append("Value of property " + propName + " was " + modulus + " but must be a prime.\n");
		}
		if (failed) {
			throw new ConfigurationException("Building Bgw configuration failded. " + sb.toString());
		}
	}

	public int getThreshold() {
		return threshold;
	}

	@Override
	public BigInteger getModulus() {
		return modulus;
	}
}
