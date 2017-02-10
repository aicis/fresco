package dk.alexandra.fresco.suite.spdz.storage;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.h2.util.IOUtils;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.framework.sce.resources.storage.FilebasedStreamedStorageImpl;
import dk.alexandra.fresco.framework.sce.resources.storage.InMemoryStorage;
import dk.alexandra.fresco.framework.sce.resources.storage.exceptions.NoMoreElementsException;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;

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
	private final static int expAmount = 1000;
	private final static int bitAmount = 100000;
	private final static int inputAmount = 1000;
	
	private String restEndPoint;
	private int myId;
	private Path triplePath;
	private Path expPath;
	private Path bitPath;
	private Map<Integer, Path> inputPaths;
	private FilebasedStreamedStorageImpl storage;
	
	private BigInteger modulus;
	private BigInteger alpha;
	
	private boolean alreadyTriedGetNextTriple = false;
	private boolean alreadyTriedGetNextExp = false;
	private boolean alreadyTriedGetNextBit = false;	
	private boolean alreadyTriedGetNextInput = false;	
	
	public DataRestSupplierImpl(int myId, String restEndPoint) {		
		this.myId = myId;
		this.restEndPoint = restEndPoint;
		if(!this.restEndPoint.endsWith("/")) {
			this.restEndPoint += "/";
		}		
		this.restEndPoint += "api/fuel/";
		storage = new FilebasedStreamedStorageImpl(new InMemoryStorage());
		this.inputPaths = new HashMap<>();
	}
	
	private Path fetchData(String type, int amount, int towardsId) {
		Path result = null;
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {			
			HttpGet httpget = null;
			if(towardsId > -1) {
				httpget = new HttpGet(this.restEndPoint + type+"/"+amount+"/"+this.myId+"/towards/"+towardsId);
			} else {
				httpget = new HttpGet(this.restEndPoint + type+"/"+amount+"/"+this.myId);
			}

            Reporter.fine("Executing request " + httpget.getRequestLine());            

            // Create a custom response handler
            ResponseHandler<Path> responseHandler = new ResponseHandler<Path>() {

                @Override
                public Path handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        Path res = Files.createTempFile("spdz", type);
                        FileOutputStream fos = new FileOutputStream(res.toFile());
                        IOUtils.copy(response.getEntity().getContent(), fos);
                        return res;
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
	
	/**
	 * Creates a REST call towards the fuel station and returns the path to the file where the received content is located. 
	 * @param amount The amount of triples to fetch.
	 * @return the path to the newly fetched triples.
	 */
	private Path fetchData(String type, int amount) {
		return fetchData(type, amount, -1);
	}
	
	@Override
	public SpdzTriple getNextTriple() {		
		try {
			if(this.triplePath == null) {
				this.triplePath = fetchData("triples", tripleAmount);
			}
			return storage.getNext(this.triplePath.toString());
		} catch (NoMoreElementsException e) {
			if(alreadyTriedGetNextTriple) {
				throw new MPCException("Could not get any more preprocessed triples. Sry.. ");
			}
			this.triplePath = fetchData("triples", tripleAmount);
			alreadyTriedGetNextTriple = true;
			SpdzTriple trip = getNextTriple();
			//If we get to here without exceptions, then we successfully fetched more triples.
			alreadyTriedGetNextTriple = false;
			return trip;
		}
	}

	@Override
	public SpdzSInt[] getNextExpPipe() {
		try {
			if(this.expPath == null) {
				this.expPath = fetchData("exp", expAmount);
			}
			return storage.getNext(this.expPath.toString());
		} catch (NoMoreElementsException e) {
			if(alreadyTriedGetNextExp) {
				throw new MPCException("Could not get any more preprocessed exponentiation pipes. Sry.. ");
			}
			this.expPath = fetchData("exp", expAmount);
			alreadyTriedGetNextExp = true;
			SpdzSInt[] exp = getNextExpPipe();
			//If we get to here without exceptions, then we successfully fetched more triples.
			alreadyTriedGetNextExp = false;
			return exp;
		}
	}

	@Override
	public SpdzInputMask getNextInputMask(int towardPlayerID) {
		try {
			if(this.inputPaths.get(towardPlayerID) == null) {
				this.inputPaths.put(towardPlayerID, fetchData("inputs", inputAmount, towardPlayerID));
			}
			return storage.getNext(this.inputPaths.get(towardPlayerID).toString());
		} catch(NoMoreElementsException e) {
			if(alreadyTriedGetNextInput) {
				throw new MPCException("Could not get any more preprocessed Inputs. Sry.. ");
			}
			this.inputPaths.put(towardPlayerID, fetchData("inputs", inputAmount, towardPlayerID));
			alreadyTriedGetNextInput = true;
			SpdzInputMask mask = getNextInputMask(towardPlayerID);
			alreadyTriedGetNextInput = false;
			return mask;
		}
	}

	@Override
	public SpdzSInt getNextBit() {
		try {
			if(this.bitPath == null) {
				this.bitPath = fetchData("bits", bitAmount);
			}
			return storage.getNext(this.bitPath.toString());
		} catch(NoMoreElementsException e) {
			if(alreadyTriedGetNextBit) {
				throw new MPCException("Could not get any more preprocessed bits. Sry.. ");
			}
			this.bitPath = fetchData("bits", bitAmount);
			alreadyTriedGetNextBit = true;
			SpdzSInt bit = getNextBit();
			alreadyTriedGetNextBit = false;
			return bit;
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

}
