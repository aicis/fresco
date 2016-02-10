package dk.alexandra.fresco.lib.collections;

import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.lp.LPFactory;

public class LookupProtocolFactoryImpl implements LookUpCircuitFactory<SInt>{

	private LPFactory lpFactory;
	private BasicNumericFactory bnf;
	private int securityParameter;
	
	public LookupProtocolFactoryImpl(int securityParameter, LPFactory lpFactory, BasicNumericFactory bnf) {
		this.securityParameter = securityParameter;
		this.lpFactory = lpFactory;
		this.bnf = bnf;
	}
	
	@Override
	public LookUpProtocol<SInt> getLookUpCircuit(SInt lookUpKey, SInt[] keys, SInt[] values, SInt outputValue) {
		return (LookUpProtocol<SInt>) new LinearLookUpCircuit(securityParameter, lookUpKey, keys, values, outputValue, lpFactory, bnf);
	}

	@Override
	public LookUpProtocol<SInt> getLookUpCircuit(SInt lookUpKey, SInt[] keys, SInt[][] values, SInt[] outputValue) {
		return new LinearLookUpCircuit(securityParameter, lookUpKey, keys, values, outputValue, lpFactory, bnf);
	}

}
