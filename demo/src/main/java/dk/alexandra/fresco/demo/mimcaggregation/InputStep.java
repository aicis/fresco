package dk.alexandra.fresco.demo.mimcaggregation;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;

public class InputStep implements Application {

	/**
	 * 
	 */
	private static final long serialVersionUID = -848122790189375513L;
	private int[][] inputRows;
	private int pid;
	private SInt[][] secretSharedRows;
	
	public InputStep(int[][] inputRows, int pid) {
		super();
		this.inputRows = inputRows;
		this.pid = pid;
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
