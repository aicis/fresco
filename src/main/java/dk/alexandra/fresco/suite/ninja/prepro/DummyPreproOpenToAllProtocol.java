package dk.alexandra.fresco.suite.ninja.prepro;

import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.OpenBoolProtocol;
import dk.alexandra.fresco.suite.ninja.NinjaOBool;
import dk.alexandra.fresco.suite.ninja.NinjaProtocolSuite;
import dk.alexandra.fresco.suite.ninja.protocols.NinjaProtocol;
import dk.alexandra.fresco.suite.ninja.storage.NinjaStorage;
import dk.alexandra.fresco.suite.ninja.storage.PrecomputedOutputNinja;

public class DummyPreproOpenToAllProtocol extends NinjaProtocol implements OpenBoolProtocol{

	private PreproNinjaSBool toOpen;
	private NinjaOBool opened;
	
	public DummyPreproOpenToAllProtocol(int id, PreproNinjaSBool toOpen, NinjaOBool opened) {
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
		NinjaStorage storage1 = NinjaProtocolSuite.getInstance(1).getStorage();
		PrecomputedOutputNinja ninja = new PrecomputedOutputNinja(new byte[] {0, 1});
		storage1.storeOutputNinja(id, ninja);
		
		NinjaStorage storage2 = NinjaProtocolSuite.getInstance(2).getStorage();
		storage2.storeOutputNinja(id, ninja);
		
		return EvaluationStatus.IS_DONE;
	}

}
