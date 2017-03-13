package threads;

import java.math.BigInteger;
import java.util.List;

import dk.alexandra.fresco.model.Type;
import dk.alexandra.fresco.services.DataGenerator;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.storage.FakeTripGen;

/**
 * Generates preprocessed data using the {@link FakeTripGen} class depending on
 * the type of preprocessed data it's responsible for.
 * 
 * @author Kasper Damgaard
 *
 */
public class GeneratorThread extends Thread {

	private DataGenerator generator;
	private Type type;
	private BigInteger alpha, mod;
	private int noOfParties, amount, threadId, towardsPartyId;

	public GeneratorThread(DataGenerator generator, Type type, int amount, int noOfParties, int threadId,
			BigInteger alpha, BigInteger mod, int towardsPartyId) {
		this.generator = generator;
		this.type = type;
		this.alpha = alpha;
		this.mod = mod;
		this.amount = amount;
		this.noOfParties = noOfParties;
		this.threadId = threadId;
		this.towardsPartyId = towardsPartyId;
	}

	public GeneratorThread(DataGenerator generator, Type type, int amount, int noOfParties, int threadId,
			BigInteger alpha, BigInteger mod) {
		this(generator, type, amount, noOfParties, threadId, alpha, mod, -1);
	}

	@Override
	public void run() {
		try {
			switch (type) {
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
			case INPUT:
				List<SpdzInputMask[]> inpMasks = FakeTripGen.generateInputMasks(amount, towardsPartyId, noOfParties, mod,
						alpha);
				generator.addInputMasks(towardsPartyId, inpMasks, threadId);
				break;
			}
			// rince and repeat
			run();
		} catch (InterruptedException ex) {
			// Stop what you're doing..
			return;
		}
	}
}
