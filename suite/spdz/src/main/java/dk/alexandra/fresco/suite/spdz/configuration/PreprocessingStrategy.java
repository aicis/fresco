package dk.alexandra.fresco.suite.spdz.configuration;

/**
 * Used by protocol suites which deals with preprocessed material.
 */
public enum PreprocessingStrategy {

  DUMMY, // Use a dummy approach (e.g. always the same data)
  MASCOT, // Use the Mascot preprocessing
  STATIC; // Use data already present on the machine it's running on.
}
