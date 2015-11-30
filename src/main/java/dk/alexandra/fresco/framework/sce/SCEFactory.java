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
package dk.alexandra.fresco.framework.sce;

import dk.alexandra.fresco.framework.sce.configuration.FileBasedSCEConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;

public class SCEFactory {

	private static final String defaultPropertiesDir = "properties";

	/**
	 * Generates a SCE based on the property files located in the folder
	 * determined by the environment variable FRESCO_HOME. Default is set to
	 * '/properties'. This also applies for the protocol suite specific
	 * properties which are assumed by default to be within the property files
	 * folder. Other environment variables might apply here. Check the protocol
	 * suite specific documentation if needed.
	 * 
	 * @return
	 */
	public static synchronized SCE getSCEFromProperties() {
		String propertiesDir = System.getenv("FRESCO_HOME");
		if (propertiesDir == null) {
			propertiesDir = defaultPropertiesDir;
		}
		SCEConfiguration sceConf = FileBasedSCEConfiguration
				.getInstance(propertiesDir);
		return getSCEFromConfiguration(sceConf);
	}

	/**
	 * Generates an SCE based on the given configuration. This will still assume
	 * that protocol suite configuration is loaded via property files. If this
	 * is not wanted, use the factory method
	 * {@code SCEFactory.getSCEFromConfiguration(SCEConfiguration conf,
	 * ProtocolSuiteConfiguration psConf} instead.
	 * 
	 * @param conf
	 * @return
	 */
	public static synchronized SCE getSCEFromConfiguration(SCEConfiguration conf) {
		SCE sce = new SCEImpl(conf);
		return sce;
	}

	/**
	 * Generates an SCE based on the given SCEConfiguration and the given
	 * ProtocolSuiteConfiguration. This is useful when you do not want to rely
	 * on a file-based approach to configuring your SCE, and strongly advised
	 * when testing.
	 * 
	 * @param conf
	 * @param psConf
	 * @return
	 */
	public static synchronized SCE getSCEFromConfiguration(
			SCEConfiguration conf, ProtocolSuiteConfiguration psConf) {
		SCE sce = new SCEImpl(conf, psConf);
		return sce;
	}
}
