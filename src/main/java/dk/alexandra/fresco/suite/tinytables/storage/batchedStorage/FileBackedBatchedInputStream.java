package dk.alexandra.fresco.suite.tinytables.storage.batchedStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class represents a data structure where which can store objects of the
 * type T. The idea is that many instances of T are stored in a file in an
 * efficient way (see {@link FileBackedBatchedPopulator}), and instances are
 * then loaded a batch at a time when needed.
 * 
 * @author Jonas Lindstrøm (jonas.lindstrom@alexandra.dk)
 *
 * @param <T>
 */
public abstract class FileBackedBatchedInputStream<T> {

	private Queue<T> currentBatch = new ConcurrentLinkedQueue<T>();
	private File file;
	private int batchSize;
	private FileInputStream is;

	public FileBackedBatchedInputStream(File file, int batchSize) {

		if (batchSize % 8 != 0) {
			throw new IllegalArgumentException(
					"Batch size should be divisble by 8 to make storage efficient");
		}

		this.file = file;
		this.batchSize = batchSize;

		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			is = new FileInputStream(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Reload this stream, eg. close the underlying file stream and create it
	 * again.
	 */
	public void reload() {
		try {
			is.close();
		} catch (IOException e) {
			// Ignore...
		}

		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// Should not happen
			e.printStackTrace();
		}
	}

	/**
	 * Read the next entry from this stream.
	 * 
	 * @return
	 * @throws NoMoreEntriesException
	 */
	public T getNext() throws NoMoreEntriesException {
		if (currentBatch.isEmpty()) {
			loadNextBatch();
		}
		return currentBatch.poll();
	}

	private void loadNextBatch() throws NoMoreEntriesException {
		byte[] buffer = new byte[batchSize * bitsPerEntry() / 8];
		try {
			int read = is.read(buffer);
			if (read == -1) {
				this.close();
				throw new NoMoreEntriesException("File is empty and no generator has been provided");
			}
			currentBatch.addAll(decodeEntries(buffer));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void keepRemaining() {
		byte[] buffer = new byte[batchSize * bitsPerEntry() / 8];
		try {
			File tmp = File.createTempFile("batched", "tmp");
			FileOutputStream os = new FileOutputStream(tmp);
			int l = 0;
			while ((l = is.read(buffer)) != -1) {
				os.write(buffer, 0, l);
			}
			os.close();
			tmp.renameTo(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close this stream.
	 */
	public void close() {
		try {
			keepRemaining();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The amount of bits used for storing an entry in the file.
	 * 
	 * @return
	 */
	protected abstract int bitsPerEntry();

	/**
	 * Decode the entries encoded in the given bytes.
	 * 
	 * @param bytes
	 * @return
	 */
	protected abstract List<T> decodeEntries(byte[] bytes);

}
