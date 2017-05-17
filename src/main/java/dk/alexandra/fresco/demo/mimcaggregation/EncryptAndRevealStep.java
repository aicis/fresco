package dk.alexandra.fresco.demo.mimcaggregation;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;
import dk.alexandra.fresco.lib.helper.builder.SortingProtocolBuilder;
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
		this.rowsWithOpenedCiphers = new Value[this.inputRows.length][this.inputRows.length + 1];
	}

	public SInt getMimcKey() {
		return mimcKey;
	}

	@Override
    public ProtocolProducer prepareApplication(ProtocolFactory factory) {
        
        OmniBuilder builder = new OmniBuilder(factory);

        NumericProtocolBuilder npb = builder.getNumericProtocolBuilder();
        NumericIOBuilder niob = builder.getNumericIOBuilder();                
        SymmetricEncryptionBuilder seb = builder.getSymmetricEncryptionBuilder();
        
        builder.beginSeqScope();

        	// Generate random value to use as encryption key        	
        	mimcKey = npb.rand();

        	// Encrypt desired column and open resulting cipher text
        	for (int r = 0; r < inputRows.length; r++) {
        		SInt toEncrypt = inputRows[r][toEncryptIndex];
        		SInt _cipherText = seb.mimcEncrypt(toEncrypt, mimcKey);
        		OInt cipherText = niob.output(_cipherText);        		
        		rowsWithOpenedCiphers[r][inputRows[r].length] = cipherText;
        		for (int c = 0; c < inputRows[r].length; c++) {
        			rowsWithOpenedCiphers[r][c] = inputRows[r][c];
        		}
        	}
        	
        builder.endCurScope();
            
        return builder.getProtocol();
    }

	public Value[][] getRowsWithOpenedCiphers() {
		return rowsWithOpenedCiphers;
	}

}
