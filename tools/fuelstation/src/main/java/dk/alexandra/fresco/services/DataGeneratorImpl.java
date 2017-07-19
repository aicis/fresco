package dk.alexandra.fresco.services;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.model.Type;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.FakeTripGen;
import dk.alexandra.fresco.suite.spdz.utils.Util;
import threads.GeneratorThread;

@Component
@PropertySource("classpath:prepro.properties")
public class DataGeneratorImpl implements DataGenerator{

	Logger logger = LoggerFactory.getLogger(DataGeneratorImpl.class);
	
	@Value("${mod}")
	private BigInteger mod;

	private List<BigInteger> alphas;		
	
	@Value("${noOfPlayers}")
	private int noOfPlayers;
	
	@Value("${noOfVMThreads}")
	private int noOfVMThreads;
	
	//Map<threadId, Map<partyId, queue<?>>>
	private final Map<Integer, Map<Integer, BlockingQueue<SpdzTriple>>> tripleQueue = new HashMap<>();
	private final Map<Integer, Map<Integer, BlockingQueue<SpdzElement>>> bitsQueue = new HashMap<>();
	private final Map<Integer, Map<Integer, BlockingQueue<SpdzElement[]>>> expPipesQueue = new HashMap<>();
	private final Map<Integer, Map<Integer, Map<Integer, BlockingQueue<SpdzInputMask>>>> inputMaskQueue = new HashMap<>();
	
	private final Map<Integer, Boolean> reset = new HashMap<>();
	private final List<GeneratorThread> threads = new ArrayList<>();
	
	/**
	 * Present for test purposes
	 * @param noOfParties
	 */
	public void setNoOfParties(int noOfParties) {
		this.noOfPlayers = noOfParties;
	}
	
	public void resetAndInit() throws IOException {
		for(GeneratorThread t : threads) {
			t.interrupt();
		}
		threads.clear();
		this.tripleQueue.clear();
		this.bitsQueue.clear();
		this.expPipesQueue.clear();
		this.inputMaskQueue.clear();
		
		clearAndInit();
	}
	
	@PostConstruct
	public void clearAndInit() throws IOException {
		Reporter.init(Level.INFO);
		this.alphas = FakeTripGen.generateAlphaShares(noOfPlayers, mod);
		init();
	}	
	
	private void init() {				
		BigInteger alpha = BigInteger.ZERO;
		for(BigInteger alphaShare : alphas) {
			alpha = alpha.add(alphaShare);
		}
		alpha = alpha.mod(mod);
		
		int tripleAmount = 10000;
		int bitAmount = 100000;
		int expPipeAmount = 2000;
		int inputAmount = 1000;
		for(int i = 0; i < noOfVMThreads; i++) {
			this.tripleQueue.put(i, new HashMap<>());
			this.bitsQueue.put(i, new HashMap<>());
			this.expPipesQueue.put(i, new HashMap<>());
			this.inputMaskQueue.put(i, new HashMap<>());
			for(int j = 1; j <= noOfPlayers; j++) {
				this.tripleQueue.get(i).put(j, new ArrayBlockingQueue<>(tripleAmount));
				this.bitsQueue.get(i).put(j, new ArrayBlockingQueue<>(bitAmount));
				this.expPipesQueue.get(i).put(j, new ArrayBlockingQueue<>(expPipeAmount));
				this.inputMaskQueue.get(i).put(j, new HashMap<>());
				for(int towards = 1; towards <= noOfPlayers; towards++) {
					this.inputMaskQueue.get(i).get(j).put(towards, new ArrayBlockingQueue<>(inputAmount));
				}
			}
		}		
		
		for(Type t : Type.values()) {
			int amount = 0;
			for(int threadId = 0; threadId < noOfVMThreads; threadId++) {
				switch(t) {
				case TRIPLES:
					amount = 1000;
					break;
				case BITS:
					amount = 3000;
					break;
				case EXPPIPES:
					amount = 50;
					break;
				case INPUT:
					amount = 100;
					for(int myId = 1; myId <= noOfPlayers; myId++) {
						GeneratorThread thread = new GeneratorThread(this, t, amount, noOfPlayers, threadId, alpha, mod, myId);
						thread.start();
						threads.add(thread);
					}
					//Created the threads for input, contiue to next thread/type
					continue;
				}
			
				GeneratorThread thread = new GeneratorThread(this, t, amount, noOfPlayers, threadId, alpha, mod);
				thread.start();
				threads.add(thread);
			}
		}
	}

	@Override
	public BigInteger getModulus() {
		return mod;
	}

	@Override
	public BigInteger getAlpha(int partyId) {
		return this.alphas.get(partyId-1);
	}
	
	@Override
	public void addTriples(List<SpdzTriple[]> triples, int threadId) throws InterruptedException {
		for(int i = 0; i < noOfPlayers; i++) {
			BlockingQueue<SpdzTriple> q = tripleQueue.get(threadId).get(i+1);
			for(SpdzTriple[] t : triples) {
				q.put(t[i]);
			}
		}
	}

	@Override
	public SpdzTriple[] getTriples(int amount, int partyId, int thread) throws InterruptedException {
		SpdzTriple[] res = new SpdzTriple[amount];
		for(int i = 0; i < amount; i++) {
			res[i] = this.tripleQueue.get(thread).get(partyId).take();
		}
		return res;
	}

	@Override
	public void addBits(List<SpdzSInt[]> bits, int thread) throws InterruptedException {
		for(int i = 0; i < noOfPlayers; i++) {
			BlockingQueue<SpdzElement> q = bitsQueue.get(thread).get(i+1);
			for(SpdzSInt[] b : bits) {
				q.put(b[i].value);
			}
		}
	}

	@Override
	public SpdzElement[] getBits(int amount, int partyId, int thread) throws InterruptedException {
		SpdzElement[] res = new SpdzElement[amount];
		for(int i = 0; i < amount; i++) {
			res[i] = this.bitsQueue.get(thread).get(partyId).take();
		}
		return res;
	}

	@Override
	public void addExpPipes(List<SpdzSInt[][]> expPipes, int thread) throws InterruptedException {
		for(int i = 0; i < noOfPlayers; i++) {
			BlockingQueue<SpdzElement[]> q = expPipesQueue.get(thread).get(i+1);
			for(SpdzSInt[][] e : expPipes) {
				SpdzElement[] elms = new SpdzElement[e[i].length];
				for(int j = 0; j < e[i].length; j++) {
					SpdzSInt sInt = e[i][j];
					elms[j] = sInt.value;
				}
				q.put(elms);
			}
		}
	}

	@Override
	public SpdzElement[][] getExpPipes(int amount, int partyId, int thread) throws InterruptedException {
		SpdzElement[][] res = new SpdzElement[amount][];
		for(int i = 0; i < amount; i++) {
			res[i] = this.expPipesQueue.get(thread).get(partyId).take();
		}
		return res;
	}

	@Override
	public void addInputMasks(int towards, List<SpdzInputMask[]> inpMasks, int thread) throws InterruptedException {
		for(int i = 0; i < noOfPlayers; i++) {
			BlockingQueue<SpdzInputMask> q = inputMaskQueue.get(thread).get(i+1).get(towards);
			for(SpdzInputMask[] b : inpMasks) {
				q.put(b[i]);
			}
		}
	}

	@Override
	public SpdzInputMask[] getInputMasks(int amount, int partyId, int towardsPartyId, int thread) throws InterruptedException {
		SpdzInputMask[] res = new SpdzInputMask[amount];
		for(int i = 0; i < amount; i++) {
			res[i] = this.inputMaskQueue.get(thread).get(partyId).get(towardsPartyId).take();
		}
		return res;
	}

	@Override
	public Boolean reset(int partyId) {
		reset.put(partyId, true);
		if(reset.values().size() == noOfPlayers) {
			boolean doReset = true;
			for(boolean b : reset.values()) {
				doReset = doReset & b;
			}
			if(doReset) {
				//Clear cache and restart generation threads. 
				//TODO
				for(GeneratorThread t : threads) {
					t.interrupt();
				}
				threads.clear();
				this.tripleQueue.clear();
				this.bitsQueue.clear();
				this.expPipesQueue.clear();
				this.inputMaskQueue.clear();
				init();
			}
		}
		return true;
	}
}
