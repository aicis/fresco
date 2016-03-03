package dk.alexandra.fresco.suite.ninja;

import dk.alexandra.fresco.framework.value.OBool;

public class NinjaOBool implements OBool{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7615308960489978540L;
	private byte value;
	
	public NinjaOBool() {
		this.value = -1;
	}
	
	public NinjaOBool(boolean value) {
		this.value = (byte) (value ? 1 : 0);
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
		return this.value != -1;
	}

	public byte getValueAsByte() {
		return value;
	}
	
	@Override
	public boolean getValue() {
		return value == 1 ? true : false;
	}

	@Override
	public void setValue(boolean b) {
		this.value = (byte) (b ? 1 : 0);
	}

	public void setByteValue(byte value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "NinjaOBool [value=" + value + "]";
	}

}
