package dk.alexandra.fresco.suite.tinytables.storage;

import java.io.File;
import java.io.IOException;
import java.util.List;

import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesTriple;
import dk.alexandra.fresco.suite.tinytables.storage.batchedStorage.FileBackedBatchedInputStream;
import dk.alexandra.fresco.suite.tinytables.storage.batchedStorage.FileBackedBatchedPopulator;
import dk.alexandra.fresco.suite.tinytables.storage.batchedStorage.NoMoreEntriesException;
import dk.alexandra.fresco.suite.tinytables.util.TinyTablesTripleGenerator;

/**
 * In this implementation of {@link TinyTablesTripleProvider} we use a file for
 * persisitant storage of triples. This allows us to keep only a few triples in
 * the memory at a time, to generate triples before the preprocessing begins and
 * to keep unused triples for future use.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 */
public class FileBackedTinyTableTripleProvider implements TinyTablesTripleProvider {

	private FileBackedBatchedInputStream<TinyTablesTriple> is;
	private FileBackedBatchedPopulator<TinyTablesTriple> os;

	/**
	 * Create a new TinuTablesTripleProvider with triples from the given file.
	 * If we run out of triples we generate new ones and save them to the same
	 * file.
	 * 
	 * @param file
	 *            The file used to store triples.
	 * @param generator
	 *            A TinyTabelsTripleGenerator used in case we run out of
	 *            triples.
	 * @param batchSize
	 *            Amount of triples to load at a time.
	 * @param batchesInFile
	 *            Amount of batches to store in the file.
	 * @throws IOException
	 */
	public FileBackedTinyTableTripleProvider(File file, TinyTablesTripleGenerator generator,
			int batchSize, int batchesInFile) throws IOException {
		
		this.is = new FileBackedBatchedInputStream<TinyTablesTriple>(file, batchSize) {

			@Override
			protected int bitsPerEntry() {
				return 3;
			}

			@Override
			protected List<TinyTablesTriple> decodeEntries(byte[] bytes) {
				return TinyTablesTriple.decode(bytes);
			}
			
		};
		
		this.os = new FileBackedBatchedPopulator<TinyTablesTriple>(file, batchSize, batchesInFile, generator) {

			@Override
			protected byte[] encodeEntries(List<TinyTablesTriple> entries) {
				return TinyTablesTriple.encode(entries);
			}
			
		};
	}

	@Override
	public TinyTablesTriple getNextTriple() {
		try {
			return is.getNext();
		} catch (NoMoreEntriesException e) {
			this.os.populateFile();
			is.reload();
			return getNextTriple(); // Try again
		}
	}

	@Override
	public void close() {
		this.is.close();
	}

}
