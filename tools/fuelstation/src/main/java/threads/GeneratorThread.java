package threads;

import java.math.BigInteger;
import java.util.List;

import dk.alexandra.fresco.model.Type;
import dk.alexandra.fresco.services.DataGenerator;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.FakeTripGen;

public class GeneratorThread extends Thread{

	private DataGenerator generator;
	private Type type;
	private BigInteger alpha, mod;
	private int noOfParties, amount, threadId;

	public GeneratorThread(DataGenerator generator, Type type, int amount, int noOfParties, int threadId, BigInteger alpha, BigInteger mod) {
		this.generator = generator;
		this.type = type;
		this.alpha = alpha;
		this.mod = mod;
		this.amount = amount;
		this.noOfParties = noOfParties;
		this.threadId = threadId;
	}

	@Override
	public void run() {		
		try {
			switch(type) {
			case TRIPLES:					
				List<SpdzTriple[]> triples = FakeTripGen.generateTriples(amount, noOfParties, mod, alpha);
				generator.addTriples(triples, threadId);
				break;
			case BITS:			
				List<SpdzSInt[]> bits = FakeTripGen.generateBits(amount, noOfParties, mod, alpha);
				generator.addBits(bits, threadId);
				break;
			case EXPPIPES:
				List<SpdzSInt[][]> exps = FakeTripGen.generateExpPipes(amount, noOfParties, mod, alpha);
				generator.addExpPipes(exps, threadId);
				break;
			case INPUT_1:
				List<SpdzInputMask[]> inpMasks = FakeTripGen.generateInputMasks(amount, 1, noOfParties, mod, alpha);
				generator.addInputMasks(1, inpMasks, threadId);
				break;
			case INPUT_2:
				inpMasks = FakeTripGen.generateInputMasks(amount, 2, noOfParties, mod, alpha);
				generator.addInputMasks(2, inpMasks, threadId);
				break;
			}		

			//rince and repeat
			run();
		} catch(InterruptedException ex) {
			//Stop what you're doing.. 
			return;
		}
	}
}
