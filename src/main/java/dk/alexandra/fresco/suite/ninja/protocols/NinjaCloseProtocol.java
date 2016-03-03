package dk.alexandra.fresco.suite.ninja.protocols;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.field.bool.CloseBoolProtocol;
import dk.alexandra.fresco.suite.ninja.NinjaOBool;
import dk.alexandra.fresco.suite.ninja.NinjaProtocolSuite;
import dk.alexandra.fresco.suite.ninja.NinjaSBool;
import dk.alexandra.fresco.suite.ninja.storage.PrecomputedInputNinja;

public class NinjaCloseProtocol extends NinjaProtocol implements CloseBoolProtocol {

	private int inputter;
	private NinjaOBool in;
	private NinjaSBool out;
	private PrecomputedInputNinja inputNinja;
	
	public NinjaCloseProtocol(int id, int inputter, OBool in, SBool out) {
		this.id = id;
		this.inputter = inputter;
		this.in = (NinjaOBool)in;
		this.out = (NinjaSBool)out;
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
		NinjaProtocolSuite ps = NinjaProtocolSuite.getInstance(resourcePool.getMyId()); 
		switch(round) {
		case 0: 			
			if(resourcePool.getMyId() == this.inputter) {
				inputNinja = ps.getStorage().getPrecomputedInputNinja(this.getId());
				byte real = inputNinja.getRealValue();
				byte x = this.in.getValueAsByte();
				byte y = ByteArithmetic.xor(x, real);
				network.sendToAll(y);
			}
			network.expectInputFromPlayer(inputter);
			return EvaluationStatus.HAS_MORE_ROUNDS;
		case 1:
			byte y = network.receive(inputter);
			this.out.setValue(y);
			
			inputNinja = null;
			in = null;			
			return EvaluationStatus.IS_DONE;
		default:
			throw new MPCException("Cannot evaluate rounds larger than 1");
		}
	}


}
