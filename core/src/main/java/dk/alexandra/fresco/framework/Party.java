/*******************************************************************************
 * Copyright (c) 2015 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.framework;

/**
 * FRESCO's view of a MPC party. 
 */
public class Party {

	private final int id;
	private final int port;
	private final String host;
	//Secret shared key used to communicate with this party. Can be null
	private String secretSharedKey;

	public Party(int id, String host, int port) {
		this.id = id;
		this.host = host;
		this.port = port;
		this.secretSharedKey = null;
	}
	
	/**
	 * 
	 * @param id
	 * @param host
	 * @param port
	 * @param secretSharedKey Base64 encoded aes key
	 */
	public Party(int id, String host, int port, String secretSharedKey) {
		this.id = id;
		this.host = host;
		this.port = port;
		this.secretSharedKey = secretSharedKey;
	}

	public String getHostname() {
		return this.host;
	}

	public int getPort() {
		return this.port;
	}

	public int getPartyId() {
		return this.id;
	}
	
	public String getSecretSharedKey() {
		return this.secretSharedKey;
	}
	
	public void setSecretSharedKey(String secretSharedKey) {
		this.secretSharedKey = secretSharedKey;		
	}

	@Override
	public String toString() {
		if(secretSharedKey == null) {
			return "Party(" + this.id + ", " + host + ":" + port + ")";
		} else {
			return "Party(" + this.id + ", " + host + ":" + port +", ssKey: "+secretSharedKey+")";
		}
	}	

}
