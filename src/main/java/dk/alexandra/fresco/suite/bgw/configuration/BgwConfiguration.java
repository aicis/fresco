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

import java.math.BigInteger;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;

public interface BgwConfiguration extends ProtocolSuiteConfiguration {

	/**
	 * @return The number of parties that can be corrupted without the protocol
	 *         security breaking down.
	 * 
	 */
	int getThreshold();
	
	/**
	 * 
	 * @return The modulus used in BGW
	 */
	BigInteger getModulus();
	
	/// Here comes methods for BGW specific parameters and their validation.
	
	
	public static BgwConfiguration fromCmdArgs(SCEConfiguration sceConf, String[] args) throws ParseException {
		Options options = new Options();

		options.addOption(Option.builder("D")
				.desc("The threshold security level. Must be a positive integer, must be at most n/2.")
				.longOpt("bgw.threshold")
				.required(true)
				.hasArgs()
				.build());		
		
		options.addOption(Option.builder("D")
				.desc("The modulus of the field you want to work in. i.e. the field size")
				.longOpt("bgw.modulus")
				.required(false)
				.hasArgs()
				.build());
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);
		
		// Validate BGW specific arguments.
		Properties p = cmd.getOptionProperties("D");
		if (!p.contains("bgw.threshold")) {
			throw new ParseException("BGW requires you to specify -Dbgw.threshold=[int]");
		}		
		
		try {
			final int threshold = Integer.parseInt(p.getProperty("bgw.threshold"));			
			if (threshold < 1) throw new ParseException("bgw.threshold must be > 0");
			if (threshold > sceConf.getParties().size() / 2) throw new ParseException("bgw.threshold must be < n/2");		
			
			final BigInteger modulus = new BigInteger(p.getProperty("bgw.modulus", "618970019642690137449562111"));
			if(!modulus.isProbablePrime(40)) {
				throw new ParseException("BGW Modulus must be a prime number");
			}
			
			return new BgwConfiguration() {

				@Override
				public int getThreshold() {
					return threshold;
				}

				@Override
				public BigInteger getModulus() {
					return modulus;
				}
				
			};
		} catch (NumberFormatException e) {
			throw new ParseException("Invalid bgw.threshold value: '" + p.getProperty("bgw.threshold") + "'");
		}
		
		
		//List<String> rest = cmd.getArgList(); // TODO: Pass back for use in application.
		
	
		
	}
	
}
