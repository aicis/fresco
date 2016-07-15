/*******************************************************************************
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.suite.tinytables.prepro;

import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.sce.configuration.ProtocolSuiteConfiguration;

public class TinyTablesPreproConfiguration implements ProtocolSuiteConfiguration {

	private ProtocolFactory tinyTablesFactory;
	private String hostname;
	private int port;

	public TinyTablesPreproConfiguration() {
		tinyTablesFactory = new TinyTablesPreproFactory();
	}

	public void setTinyTableFactory(ProtocolFactory tinyTablesFactory) {
		this.tinyTablesFactory = tinyTablesFactory;
	}

	public ProtocolFactory getProtocolFactory() {
		return this.tinyTablesFactory;
	}

	/**
	 * Set the hostname of the other player.
	 * 
	 * @param hostname
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * Return the hostname of the other player. See also
	 * {@link #setHostname(String)}.
	 * 
	 * @return
	 */
	public String getHostname() {
		return this.hostname;
	}

	/**
	 * Set the port number to perform the oblivious transfers through. Should be
	 * the same for both players.
	 * 
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Return the port number to do oblivious transfers through. See also
	 * {@link #setPort}.
	 * 
	 * @return
	 */
	public int getPort() {
		return this.port;
	}
}
