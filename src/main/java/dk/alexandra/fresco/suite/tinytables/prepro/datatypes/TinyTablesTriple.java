package dk.alexandra.fresco.suite.tinytables.prepro.datatypes;

import java.io.Serializable;

public class TinyTablesTriple implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2666542565038907636L;
	private boolean a, b, c;
	
	public TinyTablesTriple(boolean a, boolean b, boolean c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
	public boolean getA() {
		return this.a;
	}
	
	public boolean getB() {
		return this.b;
	}

	public boolean getC() {
		return this.c;
	}
	
	public void setA(boolean a) {
		this.a = a;
	}
	
	public void setB(boolean b) {
		this.b = b;
	}
	
	public void setC(boolean c) {
		this.c = c;
	}
	
	@Override
	public String toString() {
		return "TinyTablesTriple: " + a + ", " + b + ", " + c;
	}
}
