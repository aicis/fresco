package dk.alexandra.fresco.suite.spdz.configuration;

/**
 * Used by protocol suites which deals with preprocessed material.
 * @author Kasper Damgaard
 *
 */
public enum PreprocessingStrategy {

	DUMMY, // Use a dummy approach (e.g. always the same data)
	STATIC; // Use data already present on the machine it's running on. 
}
