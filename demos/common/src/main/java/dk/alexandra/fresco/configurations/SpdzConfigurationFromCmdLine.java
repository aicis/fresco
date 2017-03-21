package dk.alexandra.fresco.configurations;

import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import dk.alexandra.fresco.framework.configuration.PreprocessingStrategy;
import dk.alexandra.fresco.suite.spdz.configuration.SpdzConfiguration;

public class SpdzConfigurationFromCmdLine {

	public static SpdzConfiguration fromCmdLine(CommandLine cmd) throws ParseException {
		Properties p = cmd.getOptionProperties("D"); 
		final int maxBitLength = Integer.parseInt(p.getProperty("spdz.maxBitLength", "64"));
		if (maxBitLength < 2) {
			throw new ParseException("spdz.maxBitLength must be > 1");
		}
		
		final String fuelStationBaseUrl = p.getProperty("spdz.fuelStationBaseUrl", null);
		String strat = p.getProperty("spdz.preprocessingStrategy");
		final PreprocessingStrategy strategy;
		if(strat != null) {
			 strategy = PreprocessingStrategy.fromString(strat);
		} else {
			throw new IllegalArgumentException("The property -Dspdz.preprocessingStrategy must be set to one of the values: " + Arrays.toString(PreprocessingStrategy.values()));
		}

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
