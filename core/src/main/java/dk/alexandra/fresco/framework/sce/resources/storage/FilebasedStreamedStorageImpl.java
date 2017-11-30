package dk.alexandra.fresco.framework.sce.resources.storage;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.sce.resources.storage.exceptions.NoMoreElementsException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Streamed Storage based on files.
 *
 */
public class FilebasedStreamedStorageImpl implements StreamedStorage {

  private Map<String, ObjectInputStream> oiss;
  private Map<String, ObjectOutputStream> ooss;
  private Storage storage;
  private static final Logger logger = LoggerFactory.getLogger(FilebasedStreamedStorageImpl.class);

  /**
   * Creates an instance of the file based streamed storage. For non-streamable object, the given
   * internal storage is used.
   *
   * @param internalStorage The storage used for non-streamable objects
   */
  public FilebasedStreamedStorageImpl(Storage internalStorage) {
    this(internalStorage, new HashMap<>(), new HashMap<>());
  }

  /**
   * Creates an instance of the file based streamed storage. For non-streamable object, the given
   * internal storage is used.
   *
   * @param internalStorage The storage used for non-streamable objects
   * @param inputs a map from store names to input streams reading from the named store
   * @param inputs a map from store names to output streams writing to the named store
   */
  protected FilebasedStreamedStorageImpl(Storage internalStorage,
      Map<String, ObjectInputStream> inputs,
      Map<String, ObjectOutputStream> outputs) {
    this.storage = internalStorage;
    oiss = inputs;
    ooss = outputs;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Serializable> T getNext(String name) throws NoMoreElementsException {
    if (!oiss.containsKey(name)) {
      FileInputStream fis;
      ObjectInputStream ois;
      try {
        fis = new FileInputStream(name);
        ois = new ObjectInputStream(fis);
      } catch (IOException e) {
        throw new MPCException("IOException accessing store name: " + name, e);
      }
      oiss.put(name, ois);
    }
    try {
      return (T) oiss.get(name).readObject();
    } catch (ClassNotFoundException e) {
      throw new MPCException("Class not found", e);
    } catch (IOException e) {
      logger.error("IO-Exception. Could not read object from: " + name
          + ". This is most likely because there are no more elements available.");
      throw new NoMoreElementsException(
          "IOException - most likely because there are no more elements available.", e);
    }
  }

  @Override
  public boolean putNext(String name, Serializable o) {
    if (!ooss.containsKey(name)) {
      FileOutputStream fos;
      ObjectOutputStream oos;
      try {
        fos = new FileOutputStream(name);
        oos = new ObjectOutputStream(fos);
      } catch (IOException e) {
        throw new MPCException("IOException accessing store name: " + name, e);
      }
      ooss.put(name, oos);
    }
    try {
      ooss.get(name).writeObject(o);
    } catch (IOException e) {
      throw new MPCException("IOException writing to store name " + name, e);
    }
    return true;
  }

  @Override
  public void shutdown() {
    for (ObjectInputStream ois : oiss.values()) {
      try {
        ois.close();
      } catch (IOException e) {
        // Do nothing - nothing can be done
      }
    }

    for (ObjectOutputStream oos : ooss.values()) {
      try {
        oos.close();
      } catch (IOException e) {
        // Do nothing - nothing can be done
      }
    }
  }

  @Override
  public boolean putObject(String name, String key, Serializable o) {
    return this.storage.putObject(name, key, o);
  }

  @Override
  public <T extends Serializable> T getObject(String name, String key) {
    return this.storage.getObject(name, key);
  }

}
