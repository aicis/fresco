package dk.alexandra.fresco.suite.ninja.prepro;

import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.suite.ninja.NinjaProtocolSuite;
import dk.alexandra.fresco.suite.ninja.protocols.NinjaProtocol;
import dk.alexandra.fresco.suite.ninja.storage.NinjaStorage;
import dk.alexandra.fresco.suite.ninja.storage.PrecomputedNinja;

public class DummyPreproANDProtocol extends NinjaProtocol implements AndProtocol{

	private int id;
	private PreproNinjaSBool inLeft, inRight, out;
	
	public DummyPreproANDProtocol(int id, PreproNinjaSBool inLeft, PreproNinjaSBool inRight, PreproNinjaSBool out) {
		super();
		this.id = id;
		this.inLeft = inLeft;
		this.inRight = inRight;
		this.out = out;
		
		this.out.setOriginator(this);
	}

	@Override
	public Value[] getInputValues() {
		return new Value[] {inLeft, inRight};
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[] {out};
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {
		NinjaStorage storage1 = NinjaProtocolSuite.getInstance(1).getStorage();
		NinjaStorage storage2 = NinjaProtocolSuite.getInstance(2).getStorage();
		PrecomputedNinja ninja1 = new PrecomputedNinja(new byte[] {0, 0, 0, 1});
		PrecomputedNinja ninja2 = new PrecomputedNinja(new byte[] {0, 0, 0, 0});
		storage1.storeNinja(id, ninja1);		
		storage2.storeNinja(id, ninja2);		

		return EvaluationStatus.IS_DONE;
	}

}
