package dk.alexandra.fresco.configurations;

import java.math.BigInteger;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import dk.alexandra.fresco.framework.sce.configuration.SCEConfiguration;
import dk.alexandra.fresco.suite.bgw.configuration.BgwConfiguration;

public class BGWConfigurationFromCmdLine {

	public static BgwConfiguration fromCmdLine(SCEConfiguration sceConf, CommandLine cmd) throws ParseException {
		// Validate BGW specific arguments.
		Properties p = cmd.getOptionProperties("D");
		if (!p.containsKey("bgw.threshold")) {
			throw new ParseException("BGW requires setting -Dbgw.threshold=[int]");
		}

		try {
			final int threshold = Integer.parseInt(p.getProperty("bgw.threshold"));
			if (threshold < 1)
				throw new ParseException("bgw.threshold must be > 0");
			if (threshold > sceConf.getParties().size() / 2)
				throw new ParseException("bgw.threshold must be < n/2");

			final BigInteger modulus = new BigInteger(p.getProperty("bgw.modulus", "618970019642690137449562111"));
			if (!modulus.isProbablePrime(40)) {
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
	}
}
