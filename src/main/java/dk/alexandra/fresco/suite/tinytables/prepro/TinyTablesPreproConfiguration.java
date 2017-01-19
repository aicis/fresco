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
	private File triplesFile;
	private int triplesBatchSize;

	public static ProtocolSuiteConfiguration fromCmdLine(SCEConfiguration sceConf, CommandLine cmd)
			throws ParseException, IllegalArgumentException {

		Options options = new Options();

		TinyTablesPreproConfiguration configuration = new TinyTablesPreproConfiguration();

		/*
		 * Parse TinyTables specific options
		 */

		String tinytablesFileOption = "tinytables.file";
		options.addOption(Option.builder("D")
				.desc("The file where the generated TinyTables should be stored.")
				.longOpt(tinytablesFileOption).required(false).hasArgs().build());

		String triplesFileOption = "triples.file";
		options.addOption(Option.builder("D")
				.desc("A file for storing generated multiplication triples")
				.longOpt(triplesFileOption).required(false).hasArgs().build());

		String batchSizeOption = "triples.batchSize";
		options.addOption(Option.builder("D")
				.desc("The amount of triples to keep in memory at a time").longOpt(batchSizeOption)
				.required(false).hasArgs().build());

		Properties p = cmd.getOptionProperties("D");

		String tinyTablesFilePath = p.getProperty(tinytablesFileOption, "tinytables");
		File tinyTablesFile = new File(tinyTablesFilePath);
		configuration.setTinyTablesFile(tinyTablesFile);

		String triplesFilePath = p.getProperty(triplesFileOption, "triples");
		File triplesFile = new File(triplesFilePath);
		configuration.setTriplesFile(triplesFile);

		int batchSize = Integer.parseInt(p.getProperty(batchSizeOption, "1024"));
		configuration.setTriplesBatchSize(batchSize);

		return configuration;
	}

	public TinyTablesPreproConfiguration() {
		tinyTablesFactory = new TinyTablesPreproFactory();
	}

	/**
	 * Set file where generated TinyTables should be stored. Each AND gate need
	 * one TinyTable, and a TinyTable should only be used once in the online
	 * phase
	 * 
	 * @param file
	 */
	public void setTinyTablesFile(File file) {
		this.tinytablesfile = file;
	}

	/**
	 * Get the file where TinyTables are stored to.
	 * 
	 * @return
	 */
	public File getTinyTablesFile() {
		return this.tinytablesfile;
	}

	public ProtocolFactory getProtocolFactory() {
		return this.tinyTablesFactory;
	}

	/**
	 * Set file where multipliaction (Beaver) triples can be stored and
	 * loaded from. If a file with triples alread exists, we use these
	 * triples. Otherwise we generate new ones and store them to this file.
	 * 
	 * @param triplesFile
	 */
	public void setTriplesFile(File triplesFile) {
		this.triplesFile = triplesFile;
	}

	/**
	 * Get the file wher multiplication triples are stored and loaded from.
	 * 
	 * @return
	 */
	public File getTriplesFile() {
		return this.triplesFile;
	}

	/**
	 * Set the number of triples that we want to load at a time. Decreasing this
	 * will use less memory but increasing it will decrease the number of times
	 * we need to generate and load triples.
	 * 
	 * @param batchSize
	 */
	public void setTriplesBatchSize(int batchSize) {
		this.triplesBatchSize = batchSize;
	}

	/**
	 * Get the number of triples loaded at a time.
	 * 
	 * @return
	 */
	public int getTriplesBatchSize() {
		return this.triplesBatchSize;
	}

}
