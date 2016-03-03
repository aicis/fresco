package dk.alexandra.fresco.suite.ninja.prepro;

import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.NotProtocol;
import dk.alexandra.fresco.suite.ninja.protocols.NinjaProtocol;

public class DummyPreproNOTProtocol extends NinjaProtocol implements NotProtocol {

	private PreproNinjaSBool in, out;
	
	public DummyPreproNOTProtocol(int id, PreproNinjaSBool in, PreproNinjaSBool out) {
		this.id = id;
		this.in = in;
		this.out = out;
		
		this.out.setOriginator(this);
	}
	
	@Override
	public Value[] getInputValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Value[] getOutputValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {				
		return EvaluationStatus.IS_DONE;
	}

}
