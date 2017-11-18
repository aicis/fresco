package dk.alexandra.fresco.demo.cli;

/**
 * Exception which notifies of FRESCO configuration mistake.
 *
 */
public class ConfigurationException extends RuntimeException{

	private static final long serialVersionUID = -4997305081079255729L;

	public ConfigurationException(String msg) {
		super(msg);
	}
}
