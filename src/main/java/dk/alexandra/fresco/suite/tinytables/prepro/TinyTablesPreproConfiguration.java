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
package dk.alexandra.fresco.suite.tinytables.prepro;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;

public class TinyTablesPreproConfiguration implements ProtocolSuiteConfiguration {

	private ProtocolFactory tinyTablesFactory;
	private File tinytablesfile;
	private int securityParameter;

	public static ProtocolSuiteConfiguration fromCmdLine(SCEConfiguration sceConf,
			CommandLine cmd) throws ParseException, IllegalArgumentException {
		
		Options options = new Options();
		
		TinyTablesPreproConfiguration configuration = new TinyTablesPreproConfiguration();

		/*
		 * Parse TinyTables specific options
		 */
		
		String tinytablesFileOption = "tinytables.file";

		String securityParameterOption = "128";

		options.addOption(Option
				.builder("D")
				.desc("Security parameter for the OT extension.")
				.longOpt(securityParameterOption).required(false).hasArgs().build());
		
		options.addOption(Option
				.builder("D")
				.desc("The file where the generated TinyTables should be stored.")
				.longOpt(tinytablesFileOption).required(false).hasArgs().build());
		
		Properties p = cmd.getOptionProperties("D");
		
		int otExtensionSecurityParameter = Integer.parseInt(p.getProperty(securityParameterOption, "128"));
		configuration.setSecurityParameter(otExtensionSecurityParameter);
		
		String tinyTablesFilePath = p.getProperty(tinytablesFileOption, "tinytables");
		File tinyTablesFile = new File(tinyTablesFilePath);
		configuration.setTinyTablesFile(tinyTablesFile);
		
		return configuration;
	}
	
	public void setSecurityParameter(int securityParameter) {
		this.securityParameter = securityParameter;
	}
	
	public int getSecurityParameter() {
		return this.securityParameter;
	}

	public TinyTablesPreproConfiguration() {
		tinyTablesFactory = new TinyTablesPreproFactory();
	}

	/**
	 * Set what file the generated TinyTables should be stored to when
	 * preprocessing is finished.
	 * 
	 * @param file
	 */
	public void setTinyTablesFile(File file) {
		this.tinytablesfile = file;
	}
	
	public File getTinyTablesFile() {
		return this.tinytablesfile;
	}
	
	public ProtocolFactory getProtocolFactory() {
		return this.tinyTablesFactory;
	}

}
