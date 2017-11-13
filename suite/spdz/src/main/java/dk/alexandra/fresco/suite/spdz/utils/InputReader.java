package dk.alexandra.fresco.suite.spdz.utils;

import java.io.IOException;

public interface InputReader {
	
	/**
	 * Reads reads input from the given source
	 */
	public void readInput() throws IOException;
	
	
	/**
	 * Tells whether or not the input has been read
	 * @return
	 */
	public boolean isRead();

}
