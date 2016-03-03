package dk.alexandra.fresco.suite.ninja;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;

public class NinjaConfiguration implements ProtocolSuiteConfiguration{

	private ProtocolFactory ninjaFactory;
	private boolean dummy;
	
	public NinjaConfiguration() {
		//default is real factory
		ninjaFactory = new NinjaFactory();
		dummy = false;
	}
	
	public void setNinjaFactory(ProtocolFactory ninjaFactory) {
		this.ninjaFactory = ninjaFactory;
	}
	
	public void setDummy(boolean useDummy) {
		this.dummy = useDummy;
	}
	
	public ProtocolFactory getProtocolFactory() {
		return this.ninjaFactory;
	}	
	
	public boolean useDummy() {
		return dummy;
	}
}
