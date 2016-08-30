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
package dk.alexandra.fresco.suite.tinytables.online;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;

public class TinyTablesConfiguration implements ProtocolSuiteConfiguration{

	private ProtocolFactory tinyTablesFactory;
	private File tinytablesfile;
	
	public TinyTablesConfiguration() {
		tinyTablesFactory = new TinyTablesFactory();
	}
	
	public static ProtocolSuiteConfiguration fromCmdArgs(SCEConfiguration sceConf,
			String[] remainingArgs) throws ParseException, IllegalArgumentException {
		
		Options options = new Options();
		
		TinyTablesConfiguration configuration = new TinyTablesConfiguration();
		
		String tinyTablesFileOption = "tinytables.file";
		
		options.addOption(Option
				.builder("D")
				.desc("The file where the generated TinyTables is leaded from.")
				.longOpt(tinyTablesFileOption).required(false).hasArgs().build());
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, remainingArgs);
		
		Properties p = cmd.getOptionProperties("D");
		
		String tinyTablesFilePath = p.getProperty(tinyTablesFileOption, "tinytables");
		File tinyTablesFile = new File(tinyTablesFilePath);
		configuration.setTinyTablesFile(tinyTablesFile);
		
		System.out.println("FromCmdArgs: " + configuration.getTinyTablesFile());
		
		return configuration;
	}
	
	public void setTinyTablesFile(File file) {
		this.tinytablesfile = file;
	}
	
	public File getTinyTablesFile() {
		return this.tinytablesfile;
	}
	
	public void setTinyTablesFactory(ProtocolFactory ninjaFactory) {
		this.tinyTablesFactory = ninjaFactory;
	}
	
	public ProtocolFactory getProtocolFactory() {
		return this.tinyTablesFactory;
	}	
}
