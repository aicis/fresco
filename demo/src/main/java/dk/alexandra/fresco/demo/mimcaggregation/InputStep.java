package dk.alexandra.fresco.demo.mimcaggregation;

import dk.alexandra.fresco.demo.inputsum.DemoApplication;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;

public class InputStep extends DemoApplication<SInt[][]> {

	/**
	 * 
	 */
	private int[][] inputRows;
	private SInt[][] secretSharedRows;
	
	public InputStep(int[][] inputRows, int pid) {
		super();
		this.inputRows = inputRows;
	}

	@Override
	public ProtocolProducer prepareApplication(BuilderFactory producer) {
		// Create the necessary builders
		BuilderFactoryNumeric factoryNumeric = (BuilderFactoryNumeric) producer;
		OmniBuilder builder = new OmniBuilder(factoryNumeric);
		NumericIOBuilder niob = builder.getNumericIOBuilder();
		
		// Secret-share input		
		builder.beginSeqScope();
			// Hard-coded input from player 1 for now			
			setSecretSharedRows(niob.inputMatrix(inputRows, 1));
		builder.endCurScope();
    	
		return builder.getProtocol();
	}

	public SInt[][] getSecretSharedRows() {
		return secretSharedRows;
	}

	public void setSecretSharedRows(SInt[][] secretSharedRows) {
		this.secretSharedRows = secretSharedRows;
	}
	
}
