/**
 * 
 */
package dk.alexandra.fresco.lib.math.logic;

import java.math.BigInteger;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.builder.AbstractProtocolBuilder;

/**
 * @author mortenvchristiansen
 *
 */
public class LogicProtocolBuilder extends AbstractProtocolBuilder{

	
	private final BasicNumericFactory bnf;
	
	private final SInt trueSInt;
	private final SInt falseSInt;
		
	@SuppressWarnings("deprecation")
	public LogicProtocolBuilder(BasicNumericFactory bnf) {
		this.bnf=bnf;
		trueSInt = bnf.getSInt(BigInteger.ONE);
		falseSInt = bnf.getSInt(BigInteger.ZERO);
	}

	/* (non-Javadoc)
	 * @see dk.alexandra.fresco.lib.helper.builder.CircuitBuilder#addGateProducer(dk.alexandra.fresco.framework.ProtocolProducer)
	 */
	@Override
	public void addProtocolProducer(ProtocolProducer gp) {
		append(gp);
	}
	
    
    public SInt and(SInt a, SInt b) {
    	SInt result=bnf.getSInt();
    	append(bnf.getMultProtocol(a, b, result));
    	return result;
    }
    
    public SInt or(SInt a, SInt b) {
    	SInt result=bnf.getSInt();
    	append(bnf.getMultProtocol(a, b, result));
    	SInt result2=bnf.getSInt();
    	append(bnf.getAddProtocol(a, b, result2));
    	SInt result3=bnf.getSInt();
    	append(bnf.getSubtractProtocol(result2, result, result3));
    	return result3;
    }
    
    public SInt xor(SInt a, SInt b) {
    	SInt result=or(a,b);
    	SInt result2=bnf.getSInt();
    	append(bnf.getMultProtocol(a, b, result2));
    	SInt result3=bnf.getSInt();
    	append(bnf.getSubtractProtocol(result, result2, result3));
    	return result3;
    } 
    
    public SInt not(SInt a) {
    	SInt result=bnf.getSInt();
    	append(bnf.getSubtractProtocol(trueSInt, a, result));
    	return result;
    } 
    
   

	/**
	 * @return the trueSint
	 */
	public SInt getTrueSInt() {
		return trueSInt;
	}

	/**
	 * @return the falseSInt
	 */
	public SInt getFalseSInt() {
		return falseSInt;
	} 
}
