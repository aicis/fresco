package dk.alexandra.fresco.suite.spdz.storage.rest;

/**
 * Used exclusively for the {@link SpdzRestDataSupplier}.
 * @author Kasper Damgaard
 *
 */
public enum Type {

	TRIPLE("triples"),
	EXP("exp"),
	INPUT("inputs"),
	BIT("bits");
	
	private final String restName;

  Type(String restName) {
        this.restName = restName;
    }

    public String getRestName() {
        return restName;
    }
}
