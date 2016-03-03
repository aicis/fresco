package dk.alexandra.fresco.suite.ninja;

import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.framework.value.SBool;

public class NinjaSBool implements SBool{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8582913017231020209L;
	private byte value;
	
	public NinjaSBool() {
		 value = -1;
	}
	
	@Override
	public byte[] getSerializableContent() {
		return new byte[] {value};
	}

	@Override
	public void setSerializableContent(byte[] val) {
		this.value = val[0];
	}

	@Override
	public boolean isReady() {
		return value != -1;
	}

	public byte getValue() {
		return value;
	}

	public void setValue(byte value) {
		this.value = value;
	}

	public byte xor(NinjaSBool other) {
		return ByteArithmetic.xor(this.value, other.getValue());		
	}

	public byte and(NinjaSBool other) {
		return ByteArithmetic.mult(this.value, other.getValue());
	}

	@Override
	public String toString() {
		return "NinjaSBool [value=" + value + "]";
	}

	public byte not() {
		return ByteArithmetic.not(value);
	}
	
	
	
}
