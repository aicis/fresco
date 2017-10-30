package dk.alexandra.fresco.suite.spdz.configuration;

/**
 * Used by protocol suites which deals with preprocessed material.
 * @author Kasper Damgaard
 *
 */
public enum PreprocessingStrategy {

	DUMMY, // Use a dummy approach (e.g. always the same data)
	STATIC, // Use data already present on the machine it's running on. 
	FUELSTATION; // Use the fuel station tool to obtain data.
	
	public static PreprocessingStrategy fromString(String s) {
		switch(s.toUpperCase()) {
		case "DUMMY":
			return PreprocessingStrategy.DUMMY;
		case "STATIC":
			return PreprocessingStrategy.STATIC;
		case "FUEL":
		case "FUELSTATION":
		case "FUEL_STATION":
			return FUELSTATION;
		}
		throw new IllegalArgumentException("Unkown strategy "+s+". Should be one of the following: DUMMY, STATIC, FUELSTATION");
	}
}
