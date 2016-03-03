package dk.alexandra.fresco.suite.ninja.prepro;

import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.CloseBoolProtocol;
import dk.alexandra.fresco.suite.ninja.NinjaOBool;
import dk.alexandra.fresco.suite.ninja.NinjaProtocolSuite;
import dk.alexandra.fresco.suite.ninja.protocols.NinjaProtocol;
import dk.alexandra.fresco.suite.ninja.storage.NinjaStorage;
import dk.alexandra.fresco.suite.ninja.storage.PrecomputedInputNinja;

public class DummyPreproCloseProtocol extends NinjaProtocol implements CloseBoolProtocol{

	private int inputter;
	private NinjaOBool in;
	private PreproNinjaSBool out;
	
	public DummyPreproCloseProtocol(int id, int inputter, OBool in, SBool out) {
		this.id = id;
		this.inputter = inputter;
		this.in = (NinjaOBool)in;
		this.out = (PreproNinjaSBool)out;
		
		this.out.setOriginator(this);
	}
	
	@Override
	public Value[] getInputValues() {
		return new Value[] {in};
	}

	@Override
	public Value[] getOutputValues() {
		return new Value[] {out};
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {			
		PrecomputedInputNinja inputNinjaWithRealVal = new PrecomputedInputNinja((byte)0);		
		NinjaStorage storage1 = NinjaProtocolSuite.getInstance(1).getStorage();
		NinjaStorage storage2 = NinjaProtocolSuite.getInstance(2).getStorage();
		
		if(inputter == 1) {
			storage1.storeInputNinja(id, inputNinjaWithRealVal);
		} else {
			storage2.storeInputNinja(id, inputNinjaWithRealVal);
		}
		
		return EvaluationStatus.IS_DONE;
	}

}
