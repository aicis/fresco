package dk.alexandra.fresco.demo.mimcaggregation;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import java.math.BigInteger;
import java.util.List;

public class OutputStep implements Application {

	private SInt[][] secretSharedRows;
  private List<List<Computation<BigInteger>>> openedRows;

  public OutputStep(SInt[][] secretSharedRows) {
    super();
		this.secretSharedRows = secretSharedRows;
	}

	@Override
	public ProtocolProducer prepareApplication(BuilderFactory producer) {
		// Create the necessary builders
		BuilderFactoryNumeric factoryNumeric = (BuilderFactoryNumeric) producer;
		OmniBuilder builder = new OmniBuilder(factoryNumeric);
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

  public List<List<Computation<BigInteger>>> getOpenedRows() {
    return openedRows;
  }

  public void setOpenedRows(List<List<Computation<BigInteger>>> openedRows) {
    this.openedRows = openedRows;
  }
	
}
