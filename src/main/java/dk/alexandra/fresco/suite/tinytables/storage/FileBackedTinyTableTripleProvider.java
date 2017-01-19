package dk.alexandra.fresco.suite.tinytables.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import dk.alexandra.fresco.framework.Reporter;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesTriple;
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

	private File file;
	private int batchSize;
	private int batchesInFile;
	private Queue<TinyTablesTriple> currentBatch = new ConcurrentLinkedQueue<TinyTablesTriple>();
	private FileInputStream is;
	private TinyTablesTripleGenerator generator;

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
		this.file = file;
		this.batchSize = batchSize;
		this.batchesInFile = batchesInFile;
		this.generator = generator;
		
		if (!file.exists()) {
			file.createNewFile();
		}

		start();
		Reporter.info("Loaded " + triplesPerBytes(file.length()) + " triples from file " + file);
	}

	/**
	 * Open the input stream from the triples file.
	 * 
	 * @throws FileNotFoundException
	 */
	private void start() throws FileNotFoundException {
		this.is = new FileInputStream(file);
	}

	/**
	 * Generate new batches of triples and save them to the triples file.
	 */
	private void generateAndSaveNewTriples() {
		Reporter.info("Generating " + batchSize * batchesInFile
				+ " new triples and saving them to " + file);
		try {
			FileOutputStream os = new FileOutputStream(file, false);
			for (int i = 0; i < batchesInFile; i++) {
				List<TinyTablesTriple> batch = this.generator.generateTriples(batchSize);
				os.write(TinyTablesTriple.encode(batch));
			}
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read the next batch of triples into the memory.
	 */
	private void loadNextBatch() {
		try {
			byte[] buffer = new byte[(int) bytesPerTriples(batchSize)];
			int read = is.read(buffer);
			if (read == -1) {
				// We have reached the end of the fil
				stop();
				generateAndSaveNewTriples();
				start();
				
				loadNextBatch();
			}
			currentBatch.addAll(TinyTablesTriple.decode(buffer));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public TinyTablesTriple getNextTriple() {
		if (currentBatch.isEmpty()) {
			loadNextBatch();
		}
		return currentBatch.poll();
	}

	@Override
	public void close() {
		/*
		 * The remaining triples are kept in the file. We do this by copying the
		 * triples to a temporary file and then renaming it to overwrite the triples
		 * file. The input stream is closed here, so {@link start()} must be called
		 * again before reading triples from the file again.
		 */
		byte[] buffer = new byte[(int) bytesPerTriples(batchSize)];
		try {
			File tmp = File.createTempFile("triples", "tmp");
			FileOutputStream os = new FileOutputStream(tmp);

			int l = 0;
			while ((l = is.read(buffer)) != -1) {
				os.write(buffer, 0, l);
			}
			os.close();
			tmp.renameTo(file);
			Reporter.info("Kept " + triplesPerBytes(file.length()) + " triples in file " + file);
			stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close the file input stream to the triples file.
	 * 
	 * @throws IOException
	 */
	private void stop() throws IOException {
		is.close();
	}
	
	/**
	 * Returns how many triples can be stored in the given number of bytes,
	 * assuming that the number of bytes is divisible by 3.
	 * 
	 * @param bytes
	 * @return
	 */
	private long triplesPerBytes(long bytes) {
		return bytes * 8 / 3;
	}

	/**
	 * Returns the number of bytes needed to store a given number of triples,
	 * assuming that the number of triples is divisble by 8.
	 * 
	 * @param triples
	 * @return
	 */
	private long bytesPerTriples(long triples) {
		return triples * 3 / 8;
	}

}
