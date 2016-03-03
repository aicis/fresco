package dk.alexandra.fresco.suite.ninja.protocols;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.XorProtocol;
import dk.alexandra.fresco.suite.ninja.NinjaSBool;

public class NinjaXORProtocol extends NinjaProtocol implements XorProtocol{

	private NinjaSBool inLeft, inRight, out;
	
	public NinjaXORProtocol(int id, NinjaSBool inLeft, NinjaSBool inRight, NinjaSBool out) {
		super();
		this.id = id;
		this.inLeft = inLeft;
		this.inRight = inRight;
		this.out = out;
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
		if(round == 0) {
			this.out.setValue(inLeft.xor(inRight));
			return EvaluationStatus.IS_DONE;
		} else {
			throw new MPCException("Cannot evaluate XOR in round > 0");
		}
	}

}
