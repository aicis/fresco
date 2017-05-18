package dk.alexandra.fresco.demo.mimcaggregation;

import java.util.List;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;

public class AggregateStep implements Application {

	private static final long serialVersionUID = -420207687882983242L;
	
	private List<Triple<SInt,SInt,BigInteger>> triples;
	private int cipherColumn;
    private int keyColumn;
    private int aggColumn;
    private SInt[][] result;
    
    public AggregateStep(Value[][] inputRows, int cipherColumn, int keyColumn, int aggColumn) {
		super();
		this.cipherColumn = cipherColumn;
		this.keyColumn = keyColumn;
		this.aggColumn = aggColumn;
		this.triples = new ArrayList<>(inputRows.length);
		convertToTriples(inputRows);
	}
    
    private void convertToTriples(Value[][] inputRows) {
    	for (Value[] row : inputRows) {
    		Triple<SInt,SInt,BigInteger> triple = new Triple<SInt,SInt,BigInteger>(
    				(SInt)row[this.keyColumn], 
    				(SInt)row[this.aggColumn], 
    				((OInt)row[this.cipherColumn]).getValue()
    		);
    		this.triples.add(triple);
    	}
    }
    
	@Override
    public ProtocolProducer prepareApplication(ProtocolFactory factory) {
        OmniBuilder builder = new OmniBuilder(factory);
        NumericProtocolBuilder npb = builder.getNumericProtocolBuilder();
        
        Map<BigInteger, SInt> groupedByCipher = new HashMap<>();
        Map<BigInteger, SInt> cipherToShare = new HashMap<>();
        
        // Note: this is not optimized as it constructs the entire circuit in place        
        builder.beginSeqScope();
        
	    	for (Triple<SInt,SInt,BigInteger> triple : this.triples) {
	    		BigInteger cipher = triple.cipher;
	    		SInt key = triple.key;
	    		SInt value = triple.value;
	    		
	    		if (!groupedByCipher.containsKey(cipher)) {
	    			groupedByCipher.put(cipher, npb.known(0));
	    			cipherToShare.put(cipher, key);
	    		}
	    		
	    		SInt subTotal = npb.add(groupedByCipher.get(cipher), value);
	    		groupedByCipher.put(cipher, subTotal);
	    	}
	    	
	    	result = new SInt[groupedByCipher.size()][2];
	    	int index = 0;
	    	for (Map.Entry<BigInteger, SInt> keyAndValues : groupedByCipher.entrySet()) {
	    		result[index][keyColumn] = cipherToShare.get(keyAndValues.getKey());
	    		result[index][aggColumn] = keyAndValues.getValue();
	    		index++;
	    	}
        
        builder.endCurScope();
        
        return builder.getProtocol();
    }
	
	public SInt[][] getResult() {
		return this.result;
	}
		
	private class Triple<K, V, C> {
		private K key;
		private V value;
		private C cipher;
		
		public Triple(K key, V value, C cipher) {
			super();
			this.key = key;
			this.value = value;
			this.cipher = cipher;
		}
	}
}
