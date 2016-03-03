package dk.alexandra.fresco.suite.ninja;

import java.util.HashMap;
import java.util.Map;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.suite.ProtocolSuite;
import dk.alexandra.fresco.suite.ninja.storage.NinjaDummyStorage;
import dk.alexandra.fresco.suite.ninja.storage.NinjaStorage;
import dk.alexandra.fresco.suite.ninja.storage.NinjaStorageImpl;

public class NinjaProtocolSuite implements ProtocolSuite{

	private NinjaStorage storage;
	private static Map<Integer, NinjaProtocolSuite> instances = new HashMap<>();
	
	public static NinjaProtocolSuite getInstance(int id) {
		if(instances.get(id) == null) {			
			instances.put(id, new NinjaProtocolSuite());
		}
		return instances.get(id);
	}
	
	private NinjaProtocolSuite() {
		this.storage = new NinjaDummyStorage();
	}
	
	@Override
	public void init(ResourcePool resourcePool, ProtocolSuiteConfiguration conf) {
		NinjaConfiguration ninjaConfig = (NinjaConfiguration)conf;
		if(!ninjaConfig.useDummy()) {
			this.storage = new NinjaStorageImpl(resourcePool.getStreamedStorage());	
		}		
	}
	
	public NinjaStorage getStorage() {
		return this.storage;
	}

	@Override
	public void synchronize(int gatesEvaluated) throws MPCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finishedEval() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
