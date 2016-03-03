package dk.alexandra.fresco.suite.ninja.prepro;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.ninja.protocols.NinjaProtocol;

public class PreproNinjaSBool implements SBool{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7049163907480239087L;

	//Id of wire
	private int id;
	
	//value of the random value assigned to the wire
	private byte r;
	
	//Gate where this wire starts
	private NinjaProtocol originator;
		
	public PreproNinjaSBool(int id, byte r) {
		this.id = id;
		this.r = r;
	}

	@Override
	public byte[] getSerializableContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSerializableContent(byte[] val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isReady() {
		return this.id != -1;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public byte getR() {
		return r;
	}

	public void setR(byte r) {
		this.r = r;
	}

	public NinjaProtocol getOriginator() {
		return originator;
	}

	public void setOriginator(NinjaProtocol originator) {
		if(this.originator != null) {
			throw new MPCException("Trying to modify the originator of this wire twice - there must be an error in the circuit. This wire: " + this);
		}
		this.originator = originator;
	}	

	@Override
	public String toString() {
		return "PreproNinjaSBool [id=" + id + ", r=" + r + ", originator=" + originator + "]";
	}
}
