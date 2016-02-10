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
package dk.alexandra.fresco.framework.sce.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Party;
import dk.alexandra.fresco.framework.ProtocolEvaluator;
import dk.alexandra.fresco.framework.configuration.ConfigurationException;
import dk.alexandra.fresco.framework.sce.evaluator.EvaluationStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.Storage;
import dk.alexandra.fresco.framework.sce.resources.storage.StorageStrategy;
import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import dk.alexandra.fresco.framework.sce.util.Util;

public class FileBasedSCEConfiguration implements SCEConfiguration {

	private Properties prop;
	private String propertiesLocation = "properties/sce.properties";
	private boolean loaded = false;

	private String protocolSuite;
	private Map<Integer, Party> parties;
	private int myId;
	private Level level;
	private int noOfThreads;
	private int noOfVmThreads;
	private int maxBatchSize;
	private ProtocolEvaluator evaluator;
	private Storage storage;
	private StreamedStorage streamedStorage;

	private FileBasedSCEConfiguration(String propertiesLocation) {
		this.propertiesLocation = propertiesLocation;
	}

	private synchronized void loadProperties() {
		InputStream is;
		try {
			is = Util.getInputStream(propertiesLocation);
			prop = new Properties();
			prop.load(is);
			
			//load myId
			this.myId = Integer.parseInt(prop.getProperty("myId"));
			
			//load parties
			Map<Integer, Party> parties = new HashMap<Integer, Party>();
			int i = 1;
			while (true) {
				String party = prop.getProperty("party" + i);
				if (party == null) {
					// no more parties
					break;
				}
				String[] ipAndPort = party.split(",");
				if (ipAndPort.length != 2) {
					throw new IllegalArgumentException(
							"a party configuration should contain something similar to 'party1=192.168.0.1,8000'");
				}
				Party p = new Party(i, ipAndPort[0], Integer.parseInt(ipAndPort[1]));			
				parties.put(i, p);
				i++;
			}
			this.parties = parties;
			
			//load loglevel
			Level logLevel;
			String levelString = prop.getProperty("logLevel", "info");
			switch (levelString.toLowerCase()) {
			case "finest":
				logLevel = Level.FINEST;
				break;
			case "finer":
				logLevel = Level.FINER;
				break;
			case "fine":
				logLevel = Level.FINE;
				break;
			case "info":
				logLevel = Level.INFO;
				break;
			case "warning":
				logLevel = Level.WARNING;
				break;
			case "severe":
				logLevel = Level.SEVERE;
				break;
			default:
				System.err
				.println("Could not understand the logLevel property. Using default INFO level.");
				logLevel = Level.INFO;
			}
			this.level = logLevel;
			
			//load runtime
			String protocolSuite = prop.getProperty("protocolSuite");
			if(protocolSuite == null) {
				throw new ConfigurationException("The property 'protocolSuite' must be set");
			}
			this.protocolSuite = protocolSuite;
			
			//load no of threads
			String threads = prop.getProperty("noOfThreads");
			if (threads == null) {
				this.noOfThreads = -1;
			} else {
				this.noOfThreads = Integer.parseInt(threads); 			
			}
			
			//load no of vm threads
			String vmThreads = prop.getProperty("noOfVmThreads");
			if (vmThreads == null) {
				this.noOfVmThreads = -1;
			} else {
				this.noOfVmThreads = Integer.parseInt(vmThreads); 			
			}
			
			// load evaluator
			String evaluator = prop.getProperty("evaluator");
			if (evaluator == null) {
				throw new ConfigurationException(
						"The property 'evaluator' must be set to one of these values: " + Arrays.toString(EvaluationStrategy.values()));
			}

			this.evaluator = EvaluationStrategy.fromString(evaluator);
			
			String storage = prop.getProperty("storage");
			if(storage == null) {
				throw new ConfigurationException("The property 'storage' must be set to one of these values: "+ Arrays.toString(StorageStrategy.values()));
			}
			this.storage = StorageStrategy.fromString(storage);
			if(this.storage == null) {
				throw new ConfigurationException("The property 'storage' must be set to one of these values: "+ Arrays.toString(StorageStrategy.values()));
			}
			//If the storage is in fact also a streamed storage, we also set that field.
			if(this.storage instanceof StreamedStorage) {
				this.streamedStorage = (StreamedStorage) this.storage;
			}
			
			this.maxBatchSize = Integer.parseInt(prop.getProperty("maxBatchSize", "4096"));
			
			loaded = true;
		} catch (IOException e) {
			throw new MPCException(
					"Could not locate the SCE properties file. ", e);
		}
	}

	public static FileBasedSCEConfiguration getInstance(String propertiesDir) {		
		String propertiesPath = propertiesDir+"/"+"sce.properties";
		return new FileBasedSCEConfiguration(propertiesPath);
	}

	@Override
	public int getMyId() {
		if (!loaded) {
			loadProperties();
		}				
		return this.myId;
	}

	@Override
	public Map<Integer, Party> getParties() {
		if (!loaded) {
			loadProperties();
		}		
		return this.parties;
	}

	/**
	 * Defaults to info if logLevel is not found in the properties file.
	 * 
	 * @return
	 */
	@Override
	public Level getLogLevel() {
		if (!loaded) {
			loadProperties();
		}		
		return this.level;
	}

	@Override
	public String getProtocolSuiteName() {
		if (!loaded) {
			loadProperties();
		}		
		return this.protocolSuite;
	}

	/**
	 * Returns -1 if no such property is found.
	 * 
	 * @return
	 */
	@Override
	public int getNoOfThreads() {
		if (!loaded) {
			loadProperties();
		}		
		return this.noOfThreads;
	}
	
	@Override
	public int getNoOfVMThreads() {
		if (!loaded) {
			loadProperties();
		}		
		return this.noOfVmThreads;
	}

	@Override
	public ProtocolEvaluator getEvaluator() {
		if(!loaded) {
			loadProperties();
		}		
		return this.evaluator;
	}

	@Override
	public Storage getStorage() {
		if(!loaded) {
			loadProperties();
		}
		return this.storage;
	}

	@Override
	public int getMaxBatchSize() {
		if(!loaded) {
			loadProperties();
		}
		return this.maxBatchSize;
	}

	@Override
	public StreamedStorage getStreamedStorage() {
		if(!loaded) {
			loadProperties();
		}
		return this.streamedStorage;
	}

	@Override
	public String toString() {
		return "FileBasedSCEConfiguration [propertiesLocation=" + propertiesLocation + ", loaded=" + loaded
				+ ", protocolSuite=" + protocolSuite + ", parties=" + parties + ", myId=" + myId + ", level=" + level
				+ ", noOfThreads=" + noOfThreads + ", noOfVmThreads=" + noOfVmThreads + ", maxBatchSize=" + maxBatchSize
				+ ", evaluator=" + evaluator + ", storage=" + storage + ", streamedStorage=" + streamedStorage + "]";
	}	
}
