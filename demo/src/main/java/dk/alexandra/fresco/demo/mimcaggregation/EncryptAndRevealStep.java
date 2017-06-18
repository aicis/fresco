package dk.alexandra.fresco.demo.mimcaggregation;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.FactoryProducer;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import dk.alexandra.fresco.lib.helper.builder.SymmetricEncryptionBuilder;

public class EncryptAndRevealStep implements Application {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1960555561200524318L;
	
	private SInt[][] inputRows;
	private SInt mimcKey;
	private int toEncryptIndex;
    private Value[][] rowsWithOpenedCiphers;
	
    public EncryptAndRevealStep(SInt[][] inputRows, int toEncryptIndex) {
		super();
		this.inputRows = inputRows;
		this.toEncryptIndex = toEncryptIndex;
		int numElementsPerRow = inputRows[0].length;
		this.rowsWithOpenedCiphers = new Value[inputRows.length][numElementsPerRow + 1];
	}

	public SInt getMimcKey() {
		return mimcKey;
	}

	@Override
	public ProtocolProducer prepareApplication(FactoryProducer producer) {

		OmniBuilder builder = new OmniBuilder(producer.getProtocolFactory());

        NumericProtocolBuilder npb = builder.getNumericProtocolBuilder();
        NumericIOBuilder niob = builder.getNumericIOBuilder();                
        SymmetricEncryptionBuilder seb = builder.getSymmetricEncryptionBuilder();
        
        builder.beginSeqScope();

        	// Generate random value to use as encryption key        	
        	mimcKey = npb.rand();

        	// Encrypt desired column and open resulting cipher text
        	for (int r = 0; r < inputRows.length; r++) {
        		SInt[] row = inputRows[r];
        		
        		SInt toEncrypt = row[toEncryptIndex];
        		SInt _cipherText = seb.mimcEncrypt(toEncrypt, mimcKey);
        		OInt cipherText = niob.output(_cipherText);
        		
        		// Since our result array has rows that are one element
        		// longer than our input, this is correct        		
        		int lastElementIndex = row.length;
        		rowsWithOpenedCiphers[r][lastElementIndex] = cipherText;
        		for (int c = 0; c < row.length; c++) {
        			rowsWithOpenedCiphers[r][c] = row[c];
        		}
        	}
        	
        builder.endCurScope();
            
        return builder.getProtocol();
    }

	public Value[][] getRowsWithOpenedCiphers() {
		return rowsWithOpenedCiphers;
	}

}
