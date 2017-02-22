/*******************************************************************************
 * Copyright (c) 2017 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.spdz.storage.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.DataSupplier;

/**
 * Uses the gas station to fetch the next piece of preprocessed data.
 * @author Kasper Damgaard
 *
 */
public class DataRestSupplierImpl implements DataSupplier{

	//TODO: For now without security - but we need some kind of "login" or 
	//token based security such that only the parties with access can obtain the different parties shares.
	//Maybe use certificates and SSL connections instead, but this is harder to test and make work.

	private final static int tripleAmount = 10000;
	private final static int expAmount = 3000;
	private final static int bitAmount = 50000;
	private final static int inputAmount = 2000;

	private String restEndPoint;
	private int myId;
	private int noOfParties;
	private int threadId; 
	
	private BigInteger modulus;
	private BigInteger alpha;

	private BlockingQueue<SpdzTriple> triples;
	private BlockingQueue<SpdzSInt> bits;
	private BlockingQueue<SpdzSInt[]> exps;
	private Map<Integer, BlockingQueue<SpdzInputMask>> inputs;
	
	private final List<RetrieverThread> threads = new ArrayList<>();

	public DataRestSupplierImpl(int myId, int noOfParties, String restEndPoint, int threadId) {		
		this.myId = myId;
		this.noOfParties = noOfParties;
		this.restEndPoint = restEndPoint;
		this.threadId = threadId;
		if(!this.restEndPoint.endsWith("/")) {
			this.restEndPoint += "/";
		}		
		this.restEndPoint += "api/fuel/";
		
		init();
	}	
	
	private void init() {
		this.triples = new ArrayBlockingQueue<>(tripleAmount);
		this.bits = new ArrayBlockingQueue<>(bitAmount);
		this.exps= new ArrayBlockingQueue<>(expAmount);
		this.inputs = new HashMap<>();
		for(int i = 1; i <= noOfParties; i++) {
			this.inputs.put(i, new ArrayBlockingQueue<>(inputAmount));
		}

		//Start retriver threads
		for(Type t : Type.values()) {
			RetrieverThread thread = null;
			switch(t) {
			case TRIPLE:
				thread = new RetrieverThread(this.restEndPoint, myId, this, t, 2000, threadId);
				thread.start();
				threads.add(thread);
				break;
			case BIT:
				thread = new RetrieverThread(this.restEndPoint, myId, this, t, 5000, threadId);
				thread.start();
				threads.add(thread);
				break;
			case EXP:
				thread = new RetrieverThread(this.restEndPoint, myId, this, t, 200, threadId);
				thread.start();
				threads.add(thread);
				break;
			case INPUT:
				for(int i = 1; i <= noOfParties; i++) {
					thread = new RetrieverThread(this.restEndPoint, myId, this, t, 1000, threadId, i);
					thread.start();
					threads.add(thread);
				}
				break;
			}
		}
	}

	public void addTriple(SpdzTriple trip) throws InterruptedException {
		this.triples.put(trip);
	}

	public void addExp(SpdzElement[] es) throws InterruptedException {
		SpdzSInt[] pipe = new SpdzSInt[es.length];
		int i = 0;
		for(SpdzElement elm : es) {
			pipe[i] = new SpdzSInt(elm);
			i++;
		}
		this.exps.put(pipe);
	}

	public void addBit(SpdzElement b) throws InterruptedException {
		this.bits.put(new SpdzSInt(b));
	}

	public void addInput(SpdzInputMask mask, int towardsId) throws InterruptedException {
		this.inputs.get(towardsId).put(mask);
	}

	@Override
	public SpdzTriple getNextTriple() {		
		try {
			boolean print = false;
			long then = 0;
			if(this.triples.peek() == null) {
				print = true;
				then = System.currentTimeMillis();
			}
			SpdzTriple t = this.triples.take();
			if(print) {
				long now = System.currentTimeMillis();
				Reporter.warn("Triple queue got back online within "+(now-then)+"ms.");
			}
			return t;
		} catch (InterruptedException e) {
			throw new MPCException("Supplier got interrupted before a new triple was made available", e);
		}
	}



	@Override
	public SpdzSInt[] getNextExpPipe() {
		try {
			boolean print = false;
			long then = 0;
			if(this.exps.peek() == null) {
				Reporter.warn("No more exp pipes in the queue");
				print = true;
				then = System.currentTimeMillis();
			}
			SpdzSInt[] exp = this.exps.take();
			if(print) {
				long now = System.currentTimeMillis();
				Reporter.warn("Exp pipe queue got back online within "+(now-then)+"ms.");
			}
			
			return exp;
		} catch (InterruptedException e) {
			throw new MPCException("Supplier got interrupted before a new exp pipe was made available", e);
		}
	}

	@Override
	public SpdzInputMask getNextInputMask(int towardPlayerID) {
		try {
			return this.inputs.get(towardPlayerID).take();
		} catch (InterruptedException e) {
			throw new MPCException("Supplier got interrupted before a new triple was made available", e);
		}
	}

	@Override
	public SpdzSInt getNextBit() {
		try {			
			return this.bits.take();
		} catch (InterruptedException e) {
			throw new MPCException("Supplier got interrupted before a new triple was made available", e);
		}
	}

	private BigInteger getBigInteger(String endpoint) {
		BigInteger result = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {			
			HttpGet httpget = new HttpGet(this.restEndPoint + endpoint);

			Reporter.fine("Executing request " + httpget.getRequestLine());            

			// Create a custom response handler
			ResponseHandler<BigInteger> responseHandler = new ResponseHandler<BigInteger>() {

				@Override
				public BigInteger handleResponse(
						final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {                        
						BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						StringBuffer result = new StringBuffer();
						String line = "";
						while ((line = rd.readLine()) != null) {
							result.append(line);
						}
						return new BigInteger(result.toString());
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}

			};
			result = httpClient.execute(httpget, responseHandler);       
		} catch (ClientProtocolException e) {
			throw new MPCException("Could not complete the http request", e);
		} catch (IOException e) {
			throw new MPCException("IO error", e);
		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {
				//silent crashing - nothing to do at this point. 
			}        	
		}
		return result;
	}

	@Override
	public BigInteger getModulus() {
		if(this.modulus == null) {
			this.modulus = this.getBigInteger("modulus");
		}
		return this.modulus;
	}

	@Override
	public BigInteger getSSK() {
		if(this.alpha == null) {
			this.alpha = this.getBigInteger("alpha/"+this.myId);
		}
		return alpha;
	}

	@Override
	public SpdzSInt getNextRandomFieldElement() {
		return new SpdzSInt(this.getNextTriple().getA());
	}

	@Override
	public void shutdown() {
		for(RetrieverThread t : threads) {
			t.stopThread();
			t.interrupt();			
		}
	}

	/**
	 * Clears the cache and sends a reset signal to the fuel station. 
	 */
	public void reset() {
		for(RetrieverThread t : threads) {
			t.stopThread();
			t.interrupt();			
		}
		
		//Only send signal if we're thread 0.
		if(threadId == 0) {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			try {			
				HttpGet httpget = new HttpGet(this.restEndPoint + "/reset/"+myId);
	
				Reporter.fine("Executing request " + httpget.getRequestLine());            
	
				// Create a custom response handler
				ResponseHandler<Boolean> responseHandler = new ResponseHandler<Boolean>() {
	
					@Override
					public Boolean handleResponse(
							final HttpResponse response) throws ClientProtocolException, IOException {
						int status = response.getStatusLine().getStatusCode();
						if (status >= 200 && status < 300) {                        
							BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
							StringBuffer result = new StringBuffer();
							String line = "";
							while ((line = rd.readLine()) != null) {
								result.append(line);
							}
							return Boolean.parseBoolean(result.toString());
						} else {
							throw new ClientProtocolException("Unexpected response status: " + status);
						}
					}
	
				};
				boolean success = httpClient.execute(httpget, responseHandler);
				if(!success) {
					throw new MPCException("Fuelstation refused reset signal.");
				}
			} catch (ClientProtocolException e) {
				throw new MPCException("Could not complete the http request", e);
			} catch (IOException e) {
				throw new MPCException("IO error", e);
			} finally {
				try {
					httpClient.close();
				} catch (IOException e) {
					//silent crashing - nothing to do at this point. 
				}        	
			}
		}
		
		init();
	}



}
