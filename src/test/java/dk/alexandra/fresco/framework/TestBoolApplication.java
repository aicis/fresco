package dk.alexandra.fresco.framework;

import dk.alexandra.fresco.framework.value.OBool;

public abstract class TestBoolApplication implements Application {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8103586116011881841L;
	public OBool[] outputs;
	
	public OBool[] getOutputs() {
		return this.outputs;
	}

}
