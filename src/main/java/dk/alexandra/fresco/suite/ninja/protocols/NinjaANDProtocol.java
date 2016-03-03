package dk.alexandra.fresco.suite.ninja.protocols;

import java.util.List;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.suite.ninja.NinjaProtocolSuite;
import dk.alexandra.fresco.suite.ninja.NinjaSBool;

public class NinjaANDProtocol extends NinjaProtocol implements AndProtocol{

	private int id;
	private NinjaSBool inLeft, inRight, out;
	
	public NinjaANDProtocol(int id, NinjaSBool inLeft, NinjaSBool inRight, NinjaSBool out) {
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
		NinjaProtocolSuite ps = NinjaProtocolSuite.getInstance(resourcePool.getMyId());		
		switch(round) {
		case 0: 
			byte res = ps.getStorage().lookupNinjaTable(id, inLeft.getValue(), inRight.getValue());
			network.sendToAll(res);
			network.expectInputFromAll();
			return EvaluationStatus.HAS_MORE_ROUNDS;
		case 1:
			List<Byte> shares = network.receiveFromAll();			
			res = shares.get(0);
			for(int i = 1; i < shares.size(); i++) {
				res = ByteArithmetic.xor(res, shares.get(i));
			}
			this.out.setValue(res);
			return EvaluationStatus.IS_DONE;
		default:
			throw new MPCException("Cannot evaluate rounds larger than 1");
		}
	}

}
