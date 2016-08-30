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
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
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

	public static ProtocolSuiteConfiguration fromCmdArgs(SCEConfiguration sceConf,
			String[] remainingArgs) throws ParseException, IllegalArgumentException {
		
		Options options = new Options();
		
		TinyTablesPreproConfiguration configuration = new TinyTablesPreproConfiguration();
		
		options.addOption(Option
				.builder("D")
				.desc("Specify whether we should try to use SCAPIs OT-Extension lib. This requires the SCAPI library to be installed.")
				.longOpt("tinytables.useOtExtension").required(false).hasArgs().build());
						
		options.addOption(Option
				.builder("D")
				.desc("The port number for the OT-Extension lib. Both players should specify the same here.")
				.longOpt("tinytables.otExtensionPort").required(false).hasArgs().build());

		options.addOption(Option
				.builder("D")
				.desc("The file where the generated TinyTables should be stored.")
				.longOpt("tinytables.tinyTablesFile").required(false).hasArgs().build());
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, remainingArgs);
		
		Properties p = cmd.getOptionProperties("D");
		
		boolean useOtExtension = Boolean.parseBoolean(p.getProperty("tinytables.useOtExtension", "false"));
		configuration.setUseOtExtension(useOtExtension);
		
		int otExtensionPort = Integer.parseInt(p.getProperty("tinytables.otExtensionPort", "9005"));
		Party other = sceConf.getParties().get(getOtherId(sceConf.getMyId()));
		configuration.setAddress(InetSocketAddress.createUnresolved(other.getHostname(), otExtensionPort));;
		
		String tinyTablesFilePath = p.getProperty("tinytables.tinyTablesFile", "tinytables");
		File tinyTablesFile = new File(tinyTablesFilePath);
		configuration.setTinyTablesFile(tinyTablesFile);
		
		return configuration;
	}
	
	private static int getOtherId(int myId) throws IllegalArgumentException {
		if (myId < 1 || myId > 2) {
			throw new IllegalArgumentException("MyID should be either 1 or 2");
		}
		return myId == 1 ? 2 : 1;
	}
	
	public TinyTablesPreproConfiguration() {
		tinyTablesFactory = new TinyTablesPreproFactory();
	}

	public void setTinyTablesFile(File file) {
		this.tinytablesfile = file;
	}
	
	public File getTinyTablesFile() {
		return this.tinytablesfile;
	}
	
	public ProtocolFactory getProtocolFactory() {
		return this.tinyTablesFactory;
	}

	/**
	 * Set the inet address of the other player. The port number should be the
	 * same for both players.
	 * 
	 * @param host
	 */
	public void setAddress(InetSocketAddress host) {
		this.address = host;
	}

	/**
	 * Return the host of the other player. See also
	 * {@link #setAddress(InetSocketAddress)}.
	 * 
	 * @return
	 */
	public InetSocketAddress getAddress() {
		return this.address;
	}
	
	public void setUseOtExtension(boolean value) {
		this.useOtExtension = value;
	}
	
	public boolean getUseOtExtension() {
		return this.useOtExtension;
	}

}
