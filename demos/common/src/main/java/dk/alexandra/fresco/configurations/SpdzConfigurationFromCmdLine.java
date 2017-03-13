package dk.alexandra.fresco.configurations;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import dk.alexandra.fresco.framework.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;

public class SpdzConfigurationFromCmdLine {

	public static SpdzConfiguration fromCmdLine(CommandLine cmd) throws ParseException {
		Properties p = cmd.getOptionProperties("D");
		//TODO: Figure out a meaningful default for the below 
		final int maxBitLength = Integer.parseInt(p.getProperty("spdz.maxBitLength", "64"));
		if (maxBitLength < 2) {
			throw new ParseException("spdz.maxBitLength must be > 1");
		}
		
		final String fuelStationBaseUrl = p.getProperty("spdz.fuelStationBaseUrl", null);
		String strat = p.getProperty("spdz.preprocessingStrategy");
		final PreprocessingStrategy strategy = PreprocessingStrategy.fromString(strat);

		return new SpdzConfiguration() {
			
			@Override
			public int getMaxBitLength() {
				return maxBitLength;
			}

			@Override
			public PreprocessingStrategy getPreprocessingStrategy() {
				return strategy;
			}

			@Override
			public String fuelStationBaseUrl() {
				return fuelStationBaseUrl;
			}

		};
	}
}
