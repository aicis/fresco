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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.utils.Util;

public class RetrieverThread extends Thread {
	
	private String restEndPoint;
	private final int myId;
	private DataRestSupplierImpl supplier;
	private final Type type;
	private final int amount;		
	private final int towardsId;
	private final int threadId;
	private final Semaphore semaphore;
	
	private static final int waitTimeInMs = 1000;
	private boolean running = true;

	public RetrieverThread(String restEndPoint, int myId, DataRestSupplierImpl supplier, Type type, int amount, int threadId) {
		this(restEndPoint, myId, supplier, type, amount, threadId, -1);
	}

	public RetrieverThread(String restEndPoint, int myId, DataRestSupplierImpl supplier, Type type, int amount, int threadId, int towardsId) {
		super();
		this.restEndPoint = restEndPoint;		
		this.myId = myId;
		this.supplier = supplier;
		this.type = type;
		this.amount = amount;
		this.threadId = threadId;
		this.towardsId = towardsId;
		this.semaphore = new Semaphore(1);
	}

	public void stopThread() {
		running = false;
	}

	public static byte[] tripleContent;
	public static boolean first = true;
	
	@Override
	public void run() {
		CloseableHttpClient httpClient = null;
		while(running) {
			try {
				this.semaphore.acquire();
				httpClient = HttpClients.createDefault();

				HttpGet httpget = null;
				if(towardsId > -1) {
					httpget = new HttpGet(this.restEndPoint + type.getRestName()+"/"+amount+"/party/"+this.myId+"/towards/"+towardsId+"/thread/"+threadId);
				} else {
					httpget = new HttpGet(this.restEndPoint + type.getRestName()+"/"+amount+"/party/"+this.myId+"/thread/"+threadId);
				}

				Reporter.fine("Executing request " + httpget.getRequestLine());            

				// Create a custom response handler
				ResponseHandler<Void> responseHandler = new ResponseHandler<Void>() {

					@Override
					public Void handleResponse(
							final HttpResponse response) throws ClientProtocolException, IOException {						
						int status = response.getStatusLine().getStatusCode();
						if (status >= 200 && status < 300) {													
							byte[] content = EntityUtils.toByteArray(response.getEntity());
							ByteArrayInputStream is = new ByteArrayInputStream(content);
							//
							if(!running) {
								//Shut down thread. Resources are dead. 
								return null;
							}
							int elmSize = Util.getModulusSize()*2;
							byte[] elm = new byte[Util.getModulusSize()*2];
							try {
								switch(type) {
								case TRIPLE:
									SpdzTriple t = null;
									for(int i = 0; i < amount; i++) {
										byte[] a = new byte[elmSize];
										byte[] b = new byte[elmSize];
										byte[] c = new byte[elmSize];
										is.read(a);
										is.read(b);
										is.read(c);										
										t = new SpdzTriple(new SpdzElement(a), new SpdzElement(b), new SpdzElement(c));
										supplier.addTriple(t);
									}									
									break;
								case EXP:																	
									for(int i = 0; i < amount; i++) {
										SpdzElement[] exp = new SpdzElement[is.read()];
										for(int inx = 0; inx < exp.length; inx++) {											
											is.read(elm);
											exp[inx] = new SpdzElement(elm);
										}
										supplier.addExp(exp);
									}
									break;
								case BIT:
									for(int i = 0; i < amount; i++) {										
										is.read(elm);
										supplier.addBit(new SpdzElement(elm));
									}
									break;
								case INPUT:									
									for(int i = 0; i < amount; i++) {										
										int length = is.read();										
										if(length == 0) {
											is.read(elm);
											supplier.addInput(new SpdzInputMask(new SpdzElement(elm)), towardsId);
										} else {											
											byte[] real = new byte[length];											
											is.read(real);
											is.read(elm);
											
											supplier.addInput(new SpdzInputMask(new SpdzElement(elm), new BigInteger(real)), towardsId);
										}
									}									
									break;
								}        
							} catch(InterruptedException e) {
								running = false;
							}
							//TODO: Consider releasing at the start to start fetching new stuff ASAP.
							semaphore.release();
						} else {
							throw new ClientProtocolException("Unexpected response status: " + status);
						}
						return null;
					}

				};    		
				httpClient.execute(httpget, responseHandler);  
			} catch (InterruptedException e1) {
				running = false;
				throw new MPCException("Retriever got interrupted", e1);
			} catch (ClientProtocolException e) {				
				Reporter.warn("Retriever could not reach client. Exception message: " + e.getMessage()+". Waiting for a "+waitTimeInMs+"ms before trying again.");
				try {
					Thread.sleep(waitTimeInMs);
					semaphore.release();
				} catch (InterruptedException e1) {
					running = false;
					throw new MPCException("Retriever Got interrupted while waiting for client to start.", e1);
				}				
			} catch (IOException e) {
				running = false;
				throw new MPCException("Retriever ran into an IOException:" + e.getMessage(), e);
			} finally {
				try {
					httpClient.close();
				} catch (IOException e) {
					//silent crashing - nothing to do at this point. 
				}        	
			}
		}
	}
}
