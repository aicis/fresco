package dk.alexandra.fresco.suite.tinytables.storage;

import java.io.Serializable;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesTriple;
import dk.alexandra.fresco.suite.tinytables.util.TinyTablesTripleGenerator;

public class TinyTablesTripleStorage implements TinyTablesTripleProvider, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6270358217493071871L;

	private final static int BATCH = 15000;
	
	private Queue<TinyTablesTriple> triples = new ConcurrentLinkedQueue<>();
	private transient TinyTablesTripleGenerator generator;
	
	public TinyTablesTripleStorage(TinyTablesTripleGenerator generator) {
		this.generator = generator;
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
		Reporter.info("Generating " + BATCH + " new TinyTableTriples...");
		List<TinyTablesTriple> newTriples = generator.generateTriples(BATCH);
		for (TinyTablesTriple triple : newTriples) {
			triples.offer(triple);
		}
	}

}
