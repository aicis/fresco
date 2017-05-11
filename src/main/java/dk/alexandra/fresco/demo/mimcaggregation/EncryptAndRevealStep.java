package dk.alexandra.fresco.demo.mimcaggregation;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.SortingProtocolBuilder;
import dk.alexandra.fresco.lib.helper.builder.SymmetricEncryptionBuilder;

public class EncryptAndRevealStep implements Application {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int[][] inputTuples;
    private int keyColumn;
    private int aggColumn;
    private SInt mimcKey;
    private OInt[] keyCiphers;
    private SInt[] values;
    
    public EncryptAndRevealStep(int[][] inputTuples, int keyColumn, int aggColumn) {
		super();
		this.inputTuples = inputTuples;
		this.keyColumn = keyColumn;
		this.aggColumn = aggColumn;
		this.keyCiphers = new OInt[inputTuples.length];
		this.values = new SInt[inputTuples.length];
	}

    public SInt getMimcKey() {
		return mimcKey;
	}

    public SInt[] getValues() {
    	return this.values;
    }
	
	public OInt[] getKeyCiphers() {
		return keyCiphers;
	}

	@Override
    public ProtocolProducer prepareApplication(ProtocolFactory factory) {
        
        OmniBuilder builder = new OmniBuilder(factory);

        NumericProtocolBuilder npb = builder.getNumericProtocolBuilder();
        NumericIOBuilder niob = builder.getNumericIOBuilder();                
        SymmetricEncryptionBuilder seb = builder.getSymmetricEncryptionBuilder();
        
        builder.beginSeqScope();

        	// Player 1 provides all input            
        	SInt[][] tuples = niob.inputMatrix(this.inputTuples, 1);
        	
            // TODO: we need to shuffle        	
        	
        	// Generate random value to use as encryption key        	
        	this.mimcKey = npb.rand();
        
        	for (int tupleIdx = 0; tupleIdx < tuples.length; tupleIdx++) {
        		SInt tupleKey = tuples[tupleIdx][this.keyColumn];
        		SInt tupleValue = tuples[tupleIdx][this.aggColumn];
        		
        		SInt encryptedTupleKey = seb.mimcEncrypt(tupleKey, mimcKey);
        		this.keyCiphers[tupleIdx] = niob.output(encryptedTupleKey);
        		this.values[tupleIdx] = tupleValue;
        	}
        	
        builder.endCurScope();
            
        return builder.getProtocol();
    }
}
