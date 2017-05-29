package dk.alexandra.fresco.demo.mimcaggregation;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;

public class OutputStep implements Application {

	private SInt[][] secretSharedRows;
	private OInt[][] openedRows;
	
	public OutputStep(SInt[][] secretSharedRows) {
		super();
		this.secretSharedRows = secretSharedRows;
	}

	@Override
	public ProtocolProducer prepareApplication(ProtocolFactory factory) {
		// Create the necessary builders
		OmniBuilder builder = new OmniBuilder(factory);
		NumericIOBuilder niob = builder.getNumericIOBuilder();
		
		// Open secret-shared rows		
		builder.beginSeqScope();
			setOpenedRows(niob.outputMatrix(secretSharedRows));
		builder.endCurScope();
    	
		return builder.getProtocol();
	}

	public Value[][] getSecretSharedRows() {
		return secretSharedRows;
	}

	public void setSecretSharedRows(SInt[][] secretSharedRows) {
		this.secretSharedRows = secretSharedRows;
	}

	public OInt[][] getOpenedRows() {
		return openedRows;
	}

	public void setOpenedRows(OInt[][] openedRows) {
		this.openedRows = openedRows;
	}
	
}
