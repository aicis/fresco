package dk.alexandra.fresco.suite.ninja.protocols;

import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.NotProtocol;
import dk.alexandra.fresco.suite.ninja.NinjaSBool;

public class NinjaNOTProtocol extends NinjaProtocol implements NotProtocol{

	private NinjaSBool in, out;
	
	public NinjaNOTProtocol(int id, NinjaSBool in, NinjaSBool out) {
		this.id = id;
		this.in = in;
		this.out = out;
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
		this.out.setValue(in.not());
		return EvaluationStatus.IS_DONE;
	}

}
