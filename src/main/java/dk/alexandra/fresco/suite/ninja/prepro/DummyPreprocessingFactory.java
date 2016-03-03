package dk.alexandra.fresco.suite.ninja.prepro;

import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.field.bool.CloseBoolProtocol;
import dk.alexandra.fresco.lib.field.bool.NotProtocol;
import dk.alexandra.fresco.lib.field.bool.OpenBoolProtocol;
import dk.alexandra.fresco.lib.field.bool.XorProtocol;
import dk.alexandra.fresco.suite.ninja.NinjaOBool;

public class DummyPreprocessingFactory implements BasicLogicFactory{

	private int counter = 0;
	private int wireCounter = 0;
	
	@Override
	public CloseBoolProtocol getCloseProtocol(int source, OBool open, SBool closed) {
		return new DummyPreproCloseProtocol(counter++, source, open, closed);
	}

	@Override
	public OpenBoolProtocol getOpenProtocol(SBool closed, OBool open) {
		return new DummyPreproOpenToAllProtocol(counter++, (PreproNinjaSBool)closed, (NinjaOBool)open);
	}

	@Override
	public OpenBoolProtocol getOpenCircuit(int target, SBool closed, OBool open) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBool getSBool() {
		return new PreproNinjaSBool(wireCounter++, (byte)0);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SBool[] getKnownConstantSBools(boolean[] bools) {
		// TODO Auto-generated method stub
		return null;
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
		return new DummyPreproANDProtocol(counter++, (PreproNinjaSBool)inLeft, (PreproNinjaSBool)inRight, (PreproNinjaSBool)out);
	}

	@Override
	public AndProtocol getAndCircuit(SBool inLeft, OBool inRight, SBool out) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NotProtocol getNotCircuit(SBool in, SBool out) {
		return new DummyPreproNOTProtocol(counter++, (PreproNinjaSBool)in, (PreproNinjaSBool)out);
	}

	@Override
	public XorProtocol getXorCircuit(SBool inLeft, SBool inRight, SBool out) {
		return new DummyPreproXORProtocol(counter++, (PreproNinjaSBool)inLeft, (PreproNinjaSBool)inRight, (PreproNinjaSBool)out);
	}

	@Override
	public XorProtocol getXorCircuit(SBool inLeft, OBool inRight, SBool out) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
