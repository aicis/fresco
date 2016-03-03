package dk.alexandra.fresco.suite.ninja.storage;

public class PrecomputedInputNinja {

	private Byte realValue;
	
	public PrecomputedInputNinja(Byte realValue) {
		super();
		this.realValue = realValue;
	}
		
	public Byte getRealValue() {
		return realValue;
	}
	public void setRealValue(Byte realValue) {
		this.realValue = realValue;
	}

	@Override
	public String toString() {
		return "PrecomputedInputNinja [realValue=" + realValue + "]";
	}
	
}
