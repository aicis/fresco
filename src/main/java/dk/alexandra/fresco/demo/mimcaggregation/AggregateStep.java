package dk.alexandra.fresco.demo.mimcaggregation;

import java.util.List;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.framework.value.Value;
import dk.alexandra.fresco.lib.helper.builder.OmniBuilder;
import dk.alexandra.fresco.lib.helper.builder.SymmetricEncryptionBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericProtocolBuilder;
import dk.alexandra.fresco.lib.helper.builder.NumericIOBuilder;

public class AggregateStep implements Application {

	/**
	 * 
	 */
	private static final long serialVersionUID = -420207687882983242L;
	
	private List<BigInteger> cipherTexts;
	private List<SInt> keys;
	private List<SInt> values;
	private int cipherColumn;
    private int keyColumn;
    private int aggColumn;
    private SInt[][] result;
    
    public AggregateStep(Value[][] inputRows, int cipherColumn, int keyColumn, int aggColumn) {
		super();
		this.cipherColumn = cipherColumn;
		this.keyColumn = keyColumn;
		this.aggColumn = aggColumn;
		this.cipherTexts = new ArrayList<>(inputRows.length);
		this.keys = new ArrayList<>(inputRows.length);
		this.values = new ArrayList<>(inputRows.length);
		unzipInputRows(inputRows);
	}
    
    private void unzipInputRows(Value[][] inputRows) {
    	for (Value[] row : inputRows) {
    		OInt _cipherText = (OInt) row[this.cipherColumn];
    		this.cipherTexts.add(_cipherText.getValue());
    		this.keys.add((SInt) row[this.keyColumn]);
    		System.out.println(row[this.keyColumn]);
    		this.values.add((SInt) row[this.aggColumn]);
    		System.out.println(row[this.aggColumn]);
    	}
    }
    
	@Override
    public ProtocolProducer prepareApplication(ProtocolFactory factory) {
        OmniBuilder builder = new OmniBuilder(factory);
        NumericProtocolBuilder npb = builder.getNumericProtocolBuilder();
        
        Map<BigInteger, SInt> groupedByCipher = new HashMap<>();
        Map<BigInteger, SInt> cipherToShare = new HashMap<>();
        
        Iterator<BigInteger> c = cipherTexts.iterator();
        Iterator<SInt> v = values.iterator();
        Iterator<SInt> k = keys.iterator();
        
        // Note: this is not optimized as it constructs the entire circuit in place        
        builder.beginSeqScope();
        
	    	while (c.hasNext() && k.hasNext() && v.hasNext()) {
	    		BigInteger cipher = c.next();
	    		SInt key = k.next();
	    		SInt value = v.next();
	    		
	    		if (!groupedByCipher.containsKey(cipher)) {
	    			groupedByCipher.put(cipher, npb.known(0));
	    			cipherToShare.put(cipher, key);
	    		}
	    		
	    		SInt subTotal = npb.add(groupedByCipher.get(key), value);
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
}
