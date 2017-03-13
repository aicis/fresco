package dk.alexandra.fresco.suite.tinytables.storage;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesTriple;
import dk.alexandra.fresco.suite.tinytables.util.TinyTablesTripleGenerator;

/**
 * This class implements a simple TinyTablesTripleProvider where triples are
 * generated in batches, and when a batch is used up a new batch is generated
 * using the provided generator. The triples are kept in memory and discarded
 * when the program is closed.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class BatchTinyTablesTripleProvider implements TinyTablesTripleProvider {
	
	private Queue<TinyTablesTriple> triples = new ConcurrentLinkedQueue<>();
	private TinyTablesTripleGenerator generator;
	private int batchSize;
	
	public BatchTinyTablesTripleProvider(TinyTablesTripleGenerator generator, int batchSize) {
		this.generator = generator;
		this.batchSize = batchSize;
		generateNewTriples();
	}
	
	@Override
	public synchronized TinyTablesTriple getNextTriple() {
		if (triples.isEmpty()) {
			generateNewTriples();
		}
		return triples.poll();
	}
	
	private void generateNewTriples() {
		Reporter.info("Generating " + batchSize + " new TinyTableTriples...");
		List<TinyTablesTriple> newTriples = generator.generate(batchSize);
		for (TinyTablesTriple triple : newTriples) {
			triples.offer(triple);
		}
	}

	@Override
	public void close() {
		
	}

}
