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
	private int noOfParties, amount;

	public GeneratorThread(DataGenerator generator, Type type, int amount, int noOfParties, BigInteger alpha, BigInteger mod) {
		this.generator = generator;
		this.type = type;
		this.alpha = alpha;
		this.mod = mod;
		this.amount = amount;
		this.noOfParties = noOfParties;
	}

	@Override
	public void run() {		
		try {
			switch(type) {
			case TRIPLES:					
				List<SpdzTriple[]> triples = FakeTripGen.generateTriples(amount, noOfParties, mod, alpha);
				generator.addTriples(triples);
				break;
			case BITS:			
				List<SpdzSInt[]> bits = FakeTripGen.generateBits(amount, noOfParties, mod, alpha);
				generator.addBits(bits);
				break;
			case EXPPIPES:
				List<SpdzSInt[][]> exps = FakeTripGen.generateExpPipes(amount, noOfParties, mod, alpha);
				generator.addExpPipes(exps);
				break;
			case INPUT_1:
				List<SpdzInputMask[]> inpMasks = FakeTripGen.generateInputMasks(amount, 1, noOfParties, mod, alpha);
				generator.addInputMasks(1, inpMasks);
				break;
			case INPUT_2:
				inpMasks = FakeTripGen.generateInputMasks(amount, 2, noOfParties, mod, alpha);
				generator.addInputMasks(2, inpMasks);
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
