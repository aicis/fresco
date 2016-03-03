package dk.alexandra.fresco.suite.ninja.protocols;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.OpenBoolProtocol;
import dk.alexandra.fresco.suite.ninja.NinjaOBool;
import dk.alexandra.fresco.suite.ninja.NinjaProtocolSuite;
import dk.alexandra.fresco.suite.ninja.NinjaSBool;
import dk.alexandra.fresco.suite.ninja.storage.NinjaStorage;

public class NinjaOpenToAllProtocol extends NinjaProtocol implements OpenBoolProtocol{

	private NinjaSBool toOpen;
	private NinjaOBool opened;
	
	public NinjaOpenToAllProtocol(int id, NinjaSBool toOpen, NinjaOBool opened) {
		super();
		this.id = id;
		this.toOpen = toOpen;
		this.opened = opened;
	}

	@Override
	public Value[] getInputValues() {
		return new Value[] {toOpen};
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[] {opened};
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {
		switch(round) {
		case 0: 
			NinjaStorage storage = NinjaProtocolSuite.getInstance(resourcePool.getMyId()).getStorage();
			opened.setByteValue(storage.lookupNinjaTable(id, toOpen.getValue()));
			return EvaluationStatus.IS_DONE;
		default:
			throw new MPCException("Cannot evaluate rounds larger than 1");
		}
	}

}
