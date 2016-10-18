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
import java.net.InetSocketAddress;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;

public class TinyTablesPreproConfiguration implements ProtocolSuiteConfiguration {

	private ProtocolFactory tinyTablesFactory;
	private InetSocketAddress address;
	private boolean useOtExtension;
	private File tinytablesfile;
	private boolean testing;

	public static ProtocolSuiteConfiguration fromCmdLine(SCEConfiguration sceConf,
			CommandLine cmd) throws ParseException, IllegalArgumentException {
		
		Options options = new Options();
		
		TinyTablesPreproConfiguration configuration = new TinyTablesPreproConfiguration();

		/*
		 * Parse TinyTables specific options
		 */
		
		String otExtensionOption = "tinytables.otExtension";
		String otExtensionPortOption = "tinytables.otExtensionPort";
		String tinytablesFileOption = "tinytables.file";
		
		options.addOption(Option
				.builder("D")
				.desc("Specify whether we should try to use SCAPIs OT-Extension lib. This requires the SCAPI library to be installed.")
				.longOpt(otExtensionOption).required(false).hasArgs().build());
						
		options.addOption(Option
				.builder("D")
				.desc("The port number for the OT-Extension lib. Both players should specify the same here.")
				.longOpt(otExtensionPortOption).required(false).hasArgs().build());

		options.addOption(Option
				.builder("D")
				.desc("The file where the generated TinyTables should be stored.")
				.longOpt(tinytablesFileOption).required(false).hasArgs().build());
		
		Properties p = cmd.getOptionProperties("D");
		
		boolean useOtExtension = Boolean.parseBoolean(p.getProperty(otExtensionOption, "false"));
		configuration.setUseOtExtension(useOtExtension);
		
		int otExtensionPort = Integer.parseInt(p.getProperty(otExtensionPortOption, "9005"));
		Party sender = sceConf.getParties().get(1);
		configuration.setSenderAddress(InetSocketAddress.createUnresolved(sender.getHostname(), otExtensionPort));;
		
		String tinyTablesFilePath = p.getProperty(tinytablesFileOption, "tinytables");
		File tinyTablesFile = new File(tinyTablesFilePath);
		configuration.setTinyTablesFile(tinyTablesFile);
		
		// We are not testing when running from command line
		configuration.setTesting(false);
		
		return configuration;
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

	public void setTesting(boolean testing) {
		this.testing = testing;
	}

	/**
	 * Should return true iff we are running with both players in the same VM.
	 * 
	 * @return
	 */
	public boolean isTesting() {
		return testing;
	}
	
	/**
	 * Set the inet address of the sender used for the OT extension. The port
	 * number should not the same as the one used for the other communication.
	 * 
	 * @param host
	 */
	public void setSenderAddress(InetSocketAddress host) {
		this.address = host;
	}

	/**
	 * Return the address of the sender. See also
	 * {@link #setSenderAddress(InetSocketAddress)}.
	 * 
	 * @return
	 */
	public InetSocketAddress getSenderAddress() {
		return this.address;
	}
	
	/**
	 * Set whether we should try to use SCAPI's OT Extension lib during
	 * preprocessing. Both players should have the SCAPI lib installed for this
	 * to work.
	 * 
	 * @param value
	 */
	public void setUseOtExtension(boolean value) {
		this.useOtExtension = value;
	}
	
	public boolean getUseOtExtension() {
		return this.useOtExtension;
	}

}
