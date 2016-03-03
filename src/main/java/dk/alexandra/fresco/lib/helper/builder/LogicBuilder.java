package dk.alexandra.fresco.lib.helper.builder;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;

public class LogicBuilder extends AbstractProtocolBuilder{

	private BasicLogicFactory blf;
	
	public LogicBuilder(BasicLogicFactory blf) {
		super();
		this.blf = blf;
	}
	
	public SBool input(int inputter, OBool value) {
		SBool res = blf.getSBool();
		append(this.blf.getCloseProtocol(inputter, value, res));
		return res;
	}
	
	public SBool input(int inputter, boolean value) {
		SBool res = blf.getSBool();
		OBool known = blf.getKnownConstantOBool(value);
		append(this.blf.getCloseProtocol(inputter, known, res));
		return res;
	}
	
	public SBool[] input(int inputter, boolean[] values) {
		SBool[] res = new SBool[values.length];
		beginParScope();		
		for(int i = 0; i < values.length; i++) {
			res[i] = input(inputter, values[i]);
		}
		endCurScope();
		return res;
	}
	
	public SBool xor(SBool left, SBool right) {
		SBool res = blf.getSBool();
		append(this.blf.getXorCircuit(left, right, res));
		return res;
	}
	
	public SBool and(SBool left, SBool right) {
		SBool res = blf.getSBool();
		append(this.blf.getAndCircuit(left, right, res));
		return res;
	}
	
	public SBool not(SBool inp1) {
		SBool res = blf.getSBool();
		append(blf.getNotCircuit(inp1, res));
		return res;
	}

	@Override
	public void addGateProducer(ProtocolProducer gp) {
		append(gp);
	}
	
	public OBool getOBool(boolean b) {
		return blf.getKnownConstantOBool(b);
	}

	public OBool output(SBool inp) {
		OBool res = blf.getOBool();
		append(blf.getOpenProtocol(inp, res));
		return res;
	}
	
	public OBool[] output(SBool... inp) {
		OBool[] res = blf.getOBools(inp.length);
		beginParScope();
		for(int i = 0; i < inp.length; i++) {
			append(blf.getOpenProtocol(inp[i], res[i]));
		}		
		endCurScope();
		return res;
	}

}
