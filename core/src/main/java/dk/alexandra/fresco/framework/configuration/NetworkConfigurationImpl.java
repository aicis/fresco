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
package dk.alexandra.fresco.framework.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Party;

public class NetworkConfigurationImpl implements NetworkConfiguration {

	private int myId;

	private Map<Integer, Party> parties = new HashMap<Integer, Party>();

	private Level logLevel;

	public NetworkConfigurationImpl() {
	}

	public NetworkConfigurationImpl(int myId, Map<Integer, Party> parties,
			Level logLevel) {
		super();
		this.myId = myId;
		this.parties = parties;
		this.logLevel = logLevel;
	}

	public void add(int id, String host, int port) {
		Party p = new Party(id, host, port);
		parties.put(id, p);
	}

	@Override
	public Party getParty(int id) {
		if (!parties.containsKey(id)) {
			throw new MPCException("No party with id " + id);
		}
		return parties.get(id);
	}

	@Override
	public int getMyId() {
		return myId;
	}

	@Override
	public Party getMe() {
		return getParty(getMyId());
	}

	@Override
	public int noOfParties() {
		return parties.size();
	}

	public void setMe(int id) {
		this.myId = id;
	}

	public void setLogLevel(Level logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public Level getLogLevel() {
		return this.logLevel;
	}

	@Override
	public String toString() {
		return "NetworkConfigurationImpl [myId=" + myId + ", parties="
				+ parties + ", logLevel=" + logLevel + "]";
	}

	
}
