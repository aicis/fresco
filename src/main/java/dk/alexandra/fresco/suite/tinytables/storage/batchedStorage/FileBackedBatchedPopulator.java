package dk.alexandra.fresco.suite.tinytables.storage.batchedStorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class FileBackedBatchedPopulator<T> {

	private File file;
	private int batchSize;
	private int batchesInFile;
	private EntryProvider<T> entryProvider;

	public FileBackedBatchedPopulator(File file, int batchSize, int batchesInFile, EntryProvider<T> generator) {
		this.file = file;
		this.batchSize =batchSize;
		this.batchesInFile = batchesInFile;
		this.entryProvider = generator;
	}

	public void populateFile() {

		try {
			FileOutputStream os = new FileOutputStream(file, false);
			for (int i = 0; i < batchesInFile; i++) {
				List<T> batch = this.entryProvider.get(batchSize);
				os.write(encodeEntries(batch));
			}
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}	
	
	protected abstract byte[] encodeEntries(List<T> entries);

}
