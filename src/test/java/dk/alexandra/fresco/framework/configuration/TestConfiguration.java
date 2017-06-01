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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.TestFrameworkException;

public class TestConfiguration implements NetworkConfiguration {

	private int myId;

	private Map<Integer, Party> parties = new HashMap<Integer, Party>();

	private Level logLevel;

	public TestConfiguration() {
	}

	public TestConfiguration(int myId, Map<Integer, Party> parties,
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
	
	public void add(int id, String host, int port, String secretKey) {
		Party p = new Party(id, host, port, secretKey);
		parties.put(id, p);
	}

	@Override
	public Party getParty(int id) {
		if (!parties.containsKey(id)) {
			throw new TestFrameworkException("No party with id " + id);
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

	
	public static Map<Integer, NetworkConfiguration> getNetworkConfigurations(int n, List<Integer> ports, Level logLevel) {
		Map<Integer, NetworkConfiguration> confs = new HashMap<Integer, NetworkConfiguration>(n);
		for (int i=0; i<n; i++) {
			TestConfiguration conf = new TestConfiguration();
			int id = 1;
			for (int port : ports) {
				conf.add(id++, "localhost", port);
			}
			conf.setMe(i + 1);
			conf.setLogLevel(logLevel);
			confs.put(i + 1, conf);
		}
		return confs;
	}

	/**
	 * As getConfigurations(n, ports, loglevel) but tries to find free ephemeral
	 * ports (but note that there is no guarantee that ports will remain
	 * unused).
	 * 
	 */
	public static Map<Integer, NetworkConfiguration> getNetworkConfigurations(int n, Level logLevel) {
		List<Integer> ports = getFreePorts(n);
		return getNetworkConfigurations(n, ports, logLevel);
	}
	
	private static List<Integer> getFreePorts(int n) {
		try{
			List<Integer> ports = new ArrayList<Integer>(n);
			for (int i=0; i<n; i++) {
				ServerSocket s = new ServerSocket(0);
				ports.add(s.getLocalPort());
				s.close();
			}
			return ports;
		} catch (IOException e) {
			throw new MPCException("Could not allocate free ports", e);

		}
	}

}
