package dk.alexandra.fresco.configurations;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dk.alexandra.fresco.suite.tinytables.online.TinyTablesConfiguration;
import dk.alexandra.fresco.suite.tinytables.prepro.TinyTablesPreproConfiguration;

public class TinyTablesConfigurationFromCmdLine {

	public static TinyTablesConfiguration fromCmdLine(CommandLine cmd)
			throws ParseException, IllegalArgumentException {

		Options options = new Options();

		TinyTablesConfiguration configuration = new TinyTablesConfiguration();

		String tinyTablesFileOption = "tinytables.file";

		options.addOption(Option.builder("D").desc("The file where the generated TinyTables is leaded from.")
				.longOpt(tinyTablesFileOption).required(false).hasArgs().build());

		Properties p = cmd.getOptionProperties("D");

		String tinyTablesFilePath = p.getProperty(tinyTablesFileOption, "tinytables");
		File tinyTablesFile = new File(tinyTablesFilePath);
		configuration.setTinyTablesFile(tinyTablesFile);

		return configuration;
	}

	public static TinyTablesPreproConfiguration preProFromCmdLine(CommandLine cmd)
			throws ParseException, IllegalArgumentException {

		Options options = new Options();

		TinyTablesPreproConfiguration configuration = new TinyTablesPreproConfiguration();

		/*
		 * Parse TinyTables specific options
		 */

		String tinytablesFileOption = "tinytables.file";
		options.addOption(Option.builder("D").desc("The file where the generated TinyTables should be stored.")
				.longOpt(tinytablesFileOption).required(false).hasArgs().build());

		String triplesFileOption = "triples.file";
		options.addOption(Option.builder("D").desc("A file for storing generated multiplication triples")
				.longOpt(triplesFileOption).required(false).hasArgs().build());

		String batchSizeOption = "triples.batchSize";
		options.addOption(Option.builder("D").desc("The amount of triples to keep in memory at a time")
				.longOpt(batchSizeOption).required(false).hasArgs().build());

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
}
