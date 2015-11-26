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

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;

public interface SpdzConfiguration extends ProtocolSuiteConfiguration {

	/**
	 * 
	 * @return The maximum bit length of any number in the field. It should be possible to multiply two numbers without overflow
	 */
	int getMaxBitLength();

	/**
	 * The path to where preprocessed data is located, including the triples
	 * used for e.g. multiplication.
	 * 
	 * @return
	 */
	public String getTriplePath();

	static SpdzConfiguration fromCmdArgs(SCEConfiguration sceConf,
			String[] remainingArgs) throws ParseException {
		Options options = new Options();

		options.addOption(Option
				.builder("D")
				.desc("The path to where the preprocessed data is located - e.g. the triples. Defaults to '/triples'")
				.longOpt("spdz.triplePath").required(false).hasArgs().build());

		options.addOption(Option
				.builder("D")
				.desc("The maximum bit length. A suggestion is half the size of the modulus, but might be required to be lower for some applications.")
				.longOpt("spdz.maxBitLength").required(true).hasArgs().build());

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, remainingArgs);

		// Validate BGW specific arguments.
		Properties p = cmd.getOptionProperties("D");
		if (!p.contains("spdz.triplePath")) {
			throw new ParseException(
					"SPDZ requires you to specify -Dspdz.triplePath=[path]");
		}

		if (!p.contains("spdz.securityParameter")) {
			throw new ParseException(
					"SPDZ requires you to specify -Dspdz.securityParameter=[path]");
		}

		final int maxBitLength = Integer.parseInt(p
				.getProperty("spdz.maxBitLength"));
		if (maxBitLength < 2) {
			throw new ParseException("spdz.maxBitLength must be > 1");
		}

		final String triplePath = p.getProperty("spdz.triplePath", "/triples");

		return new SpdzConfiguration() {

			@Override
			public String getTriplePath() {
				return triplePath;
			}

			@Override
			public int getMaxBitLength() {
				return maxBitLength;
			}
		};
	}

}
