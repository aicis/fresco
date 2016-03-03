package dk.alexandra.fresco.suite.ninja;

import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.field.bool.CloseBoolProtocol;
import dk.alexandra.fresco.lib.field.bool.NotProtocol;
import dk.alexandra.fresco.lib.field.bool.OpenBoolProtocol;
import dk.alexandra.fresco.lib.field.bool.XorProtocol;
import dk.alexandra.fresco.suite.ninja.protocols.NinjaANDProtocol;
import dk.alexandra.fresco.suite.ninja.protocols.NinjaCloseProtocol;
import dk.alexandra.fresco.suite.ninja.protocols.NinjaNOTProtocol;
import dk.alexandra.fresco.suite.ninja.protocols.NinjaOpenToAllProtocol;
import dk.alexandra.fresco.suite.ninja.protocols.NinjaXORProtocol;

public class NinjaFactory implements BasicLogicFactory{

	private int counter;
	
	public NinjaFactory() {
		this.counter = 0;
	}
	
	@Override
	public CloseBoolProtocol getCloseProtocol(int source, OBool open, SBool closed) {
		return new NinjaCloseProtocol(counter++, source, open, closed);
	}

	@Override
	public OpenBoolProtocol getOpenProtocol(SBool closed, OBool open) {
		return new NinjaOpenToAllProtocol(counter++, (NinjaSBool)closed, (NinjaOBool)open);
	}

	@Override
	public OpenBoolProtocol getOpenCircuit(int target, SBool closed, OBool open) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBool getSBool() {
		return new NinjaSBool();
	}

	@Override
	public SBool[] getSBools(int amount) {
		SBool[] res = new SBool[amount];
		for(int i = 0; i < amount; i++) {
			res [i] = this.getSBool();
		}
		return res;
	}

	@Override
	public SBool getKnownConstantSBool(boolean b) {
		throw new RuntimeException("Not implemented - deprectated method.");
	}

	@Override
	public SBool[] getKnownConstantSBools(boolean[] bools) {
		throw new RuntimeException("Not implemented - deprectated method.");
	}

	@Override
	public OBool getOBool() {
		return new NinjaOBool();
	}

	@Override
	public OBool getKnownConstantOBool(boolean b) {
		return new NinjaOBool(b);
	}

	@Override
	public AndProtocol getAndCircuit(SBool inLeft, SBool inRight, SBool out) {
		return new NinjaANDProtocol(counter++, (NinjaSBool)inLeft, (NinjaSBool)inRight, (NinjaSBool)out);
	}

	@Override
	public AndProtocol getAndCircuit(SBool inLeft, OBool inRight, SBool out) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NotProtocol getNotCircuit(SBool in, SBool out) {
		return new NinjaNOTProtocol(counter++, (NinjaSBool)in, (NinjaSBool)out);
	}

	@Override
	public XorProtocol getXorCircuit(SBool inLeft, SBool inRight, SBool out) {
		return new NinjaXORProtocol(counter++, (NinjaSBool)inLeft, (NinjaSBool)inRight, (NinjaSBool)out);
	}

	@Override
	public XorProtocol getXorCircuit(SBool inLeft, OBool inRight, SBool out) {
		// TODO Auto-generated method stub
		return null;
	}

}
