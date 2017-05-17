package dk.alexandra.fresco.demo.mimcaggregation;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;

public class InputStep implements Application {

	private int[][] inputRows;
	private int pid;
	private SInt[][] secretSharedRows;
	
	public InputStep(int[][] inputRows, int pid) {
		super();
		this.inputRows = inputRows;
		this.pid = pid;
	}

	@Override
	public ProtocolProducer prepareApplication(ProtocolFactory factory) {
		// Create the necessary builders
		OmniBuilder builder = new OmniBuilder(factory);
		NumericIOBuilder niob = builder.getNumericIOBuilder();
		
		// Secret-share input		
		builder.beginSeqScope();
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
