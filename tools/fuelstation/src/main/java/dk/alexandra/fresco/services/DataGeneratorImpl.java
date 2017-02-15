package dk.alexandra.fresco.services;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import dk.alexandra.fresco.model.Type;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.FakeTripGen;
import threads.GeneratorThread;

@Component
@PropertySource("classpath:prepro.properties")
public class DataGeneratorImpl implements DataGenerator{

	Logger logger = LoggerFactory.getLogger(DataGeneratorImpl.class);
	
	@Value("${mod}")
	private BigInteger mod;

	private List<BigInteger> alphas;	
	
	@Value("${noOfPlayers}")
	private int noOfPlayers = 2;
	
	private final Map<Integer, BlockingQueue<SpdzTriple>> tripleQueue = new HashMap<>();
	private final Map<Integer, BlockingQueue<SpdzElement>> bitsQueue = new HashMap<>();
	private final Map<Integer, BlockingQueue<SpdzElement[]>> expPipesQueue = new HashMap<>();
	private final Map<Integer, Map<Integer, BlockingQueue<SpdzInputMask>>> inputMaskQueue = new HashMap<>();
	
	public static final String FUEL_DIR = "fuel";
	
	@PostConstruct
	public void clearAndInit() throws IOException {
		this.alphas = FakeTripGen.generateAlphaShares(noOfPlayers, mod);
		
		BigInteger alpha = BigInteger.ZERO;
		for(BigInteger alphaShare : alphas) {
			alpha = alpha.add(alphaShare);
		}
		alpha = alpha.mod(mod);
			
		int tripleAmount = 10000;
		int bitAmount = 100000;
		int expPipeAmount = 2000;
		int inputAmount = 1000;
		for(int i = 1; i <= noOfPlayers; i++) {
			this.tripleQueue.put(i, new ArrayBlockingQueue<>(tripleAmount));
			this.bitsQueue.put(i, new ArrayBlockingQueue<>(bitAmount));
			this.expPipesQueue.put(i, new ArrayBlockingQueue<>(expPipeAmount));
			this.inputMaskQueue.put(i, new HashMap<>());
			for(int towards = 1; towards <= noOfPlayers; towards++) {
				this.inputMaskQueue.get(i).put(towards, new ArrayBlockingQueue<>(inputAmount));
			}
		}
		
		for(Type t : Type.values()) {
			int amount = 0;
			switch(t) {
			case TRIPLES:
				amount = tripleAmount;
				break;
			case BITS:
				amount = bitAmount;
				break;
			case EXPPIPES:
				amount = expPipeAmount;
				break;
			case INPUT_1:				
			case INPUT_2:
				amount = inputAmount;
				break;
			}
			GeneratorThread thread = new GeneratorThread(this, t, amount, noOfPlayers, alpha, mod);
			thread.start();
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
	public void addTriples(List<SpdzTriple[]> triples) throws InterruptedException {
		for(SpdzTriple[] t : triples) {
			for(int i = 0; i < noOfPlayers; i++) {
				tripleQueue.get(i+1).put(t[i]);
			}
		}
	}

	@Override
	public SpdzTriple[] getTriples(int amount, int partyId) throws InterruptedException {
		SpdzTriple[] res = new SpdzTriple[amount];
		for(int i = 0; i < amount; i++) {
			res[i] = this.tripleQueue.get(partyId).take();
		}
		return res;
	}

	@Override
	public void addBits(List<SpdzSInt[]> bits) throws InterruptedException {
		for(SpdzSInt[] b : bits) {
			for(int i = 0; i < noOfPlayers; i++) {
				bitsQueue.get(i+1).put(b[i].value);
			}
		}
	}

	@Override
	public SpdzElement[] getBits(int amount, int partyId) throws InterruptedException {
		SpdzElement[] res = new SpdzElement[amount];
		for(int i = 0; i < amount; i++) {
			res[i] = this.bitsQueue.get(partyId).take();
		}
		return res;
	}

	@Override
	public void addExpPipes(List<SpdzSInt[][]> expPipes) throws InterruptedException {
		for(SpdzSInt[][] e : expPipes) {
			for(int i = 0; i < noOfPlayers; i++) {
				SpdzElement[] elms = new SpdzElement[e[i].length];
				for(int j = 0; j < e[i].length; j++) {
					SpdzSInt sInt = e[i][j];
					elms[j] = sInt.value;
				}
				expPipesQueue.get(i+1).put(elms);
			}
		}
	}

	@Override
	public SpdzElement[][] getExpPipes(int amount, int partyId) throws InterruptedException {
		SpdzElement[][] res = new SpdzElement[amount][];
		for(int i = 0; i < amount; i++) {
			res[i] = this.expPipesQueue.get(partyId).take();
		}
		return res;
	}

	@Override
	public void addInputMasks(int towards, List<SpdzInputMask[]> inpMasks) throws InterruptedException {
		for(SpdzInputMask[] b : inpMasks) {
			for(int i = 0; i < noOfPlayers; i++) {
				inputMaskQueue.get(i+1).get(towards).put(b[i]);
			}
		}
	}

	@Override
	public SpdzInputMask[] getInputMasks(int amount, int partyId, int towardsPartyId) throws InterruptedException {
		SpdzInputMask[] res = new SpdzInputMask[amount];
		for(int i = 0; i < amount; i++) {
			res[i] = this.inputMaskQueue.get(partyId).get(towardsPartyId).take();
		}
		return res;
	}
}
