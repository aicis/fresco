/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
 * 
 */
package dk.alexandra.fresco.lib.compare;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.helper.AbstractSimpleProtocol;

/**
 * @author mortenvchristiansen
 *
 */
public class SortingCircuitImpl extends AbstractSimpleProtocol implements SortingCircuit {

	SInt[] toBeSorted;
	public SortingCircuitImpl() {

	}

	@Override
	public Value[] getInputValues() {
		return toBeSorted;
	}

	@Override
	public Value[] getOutputValues() {
		return toBeSorted;
	}

	/**
	 * Sets the input values of this circuit.
	 * 
	 * @param inputs
	 *            the input values of the circuit.
	 */
	@Override
	protected void setInputValues(Value[] inputs) {
		if (inputs instanceof SInt[] )
			this.toBeSorted = (SInt[]) inputs;
		else
			throw new ClassCastException();
	}

	/**
	 * Sets the output values of this circuit.
	 * 
	 * @param outputs
	 *            the output values of the circuit.
	 */
	@Override
	protected void setOutputValues(Value[] outputs) {
		if (outputs instanceof SInt[] )
			this.toBeSorted = (SInt[]) outputs;
		else
			throw new ClassCastException();
	}

	
	
	@Override
	protected ProtocolProducer initializeGateProducer() {
		// TODO Auto-generated method stub
		return null;
	}

}
