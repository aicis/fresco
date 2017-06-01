package dk.alexandra.fresco.framework.sce.resources.storage.exceptions;

public class NoMoreElementsException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3816913230150343707L;

	public NoMoreElementsException() {
		super();
	}
	
	public NoMoreElementsException(String s) {
		super(s);
	}
	
	public NoMoreElementsException(String s, Exception e) {
		super(s, e);
	}
}
