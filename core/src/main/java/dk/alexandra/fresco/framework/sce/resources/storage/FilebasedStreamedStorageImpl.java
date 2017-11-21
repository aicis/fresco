package dk.alexandra.fresco.framework.sce.resources.storage;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.sce.resources.storage.exceptions.NoMoreElementsException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Streamed Storage based on files.
 *
 */
public class FilebasedStreamedStorageImpl implements StreamedStorage {

  private ConcurrentHashMap<String, ObjectInputStream> oiss;
  private ConcurrentHashMap<String, ObjectOutputStream> ooss;
  private Storage storage;
  private static final Logger logger = LoggerFactory.getLogger(FilebasedStreamedStorageImpl.class);

  /**
   * Creates an instance of the file based streamed storage. For non-streamable object, the given
   * internal storage is used.
   * 
   * @param internalStorage The storage used for non-streamable objects
   */
  public FilebasedStreamedStorageImpl(Storage internalStorage) {
    this.storage = internalStorage;
    oiss = new ConcurrentHashMap<>();
    ooss = new ConcurrentHashMap<>();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Serializable> T getNext(String name) throws NoMoreElementsException {
    if (!oiss.containsKey(name)) {
      FileInputStream fis;
      try {
        fis = new FileInputStream(name);
      } catch (FileNotFoundException e) {
        throw new MPCException("File with filename '" + name + "' not found.");
      }
      ObjectInputStream ois = null;
      try {
        ois = new ObjectInputStream(fis);
      } catch (IOException e) {
        e.printStackTrace();
        if (fis != null) {
          try {
            fis.close();
          } catch (IOException e1) {
            //Ignore
          }
        }
        throw new MPCException("IOException: " + e.getMessage());
      }
      oiss.put(name, ois);
    }
    try {
      return (T) oiss.get(name).readObject();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new MPCException("Class not found: " + e.getMessage());
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
      try {
        fos = new FileOutputStream(name);
      } catch (FileNotFoundException e) {
        throw new MPCException("File with filename '" + name + "' not found.");
      }
      ObjectOutputStream oos = null;
      try {
        oos = new ObjectOutputStream(fos);
      } catch (IOException e) {
        e.printStackTrace();
        if (fos != null) {
          try {
            fos.close();
          } catch (IOException e1) {
            //Ignore
          }
        }
        throw new MPCException("IOException: " + e.getMessage());
      }
      ooss.put(name, oos);
    }
    try {
      ooss.get(name).writeObject(o);
    } catch (IOException e) {
      e.printStackTrace();
      throw new MPCException("IOException: " + e.getMessage());
    }
    return true;
  }

  @Override
  public void shutdown() {
    for (ObjectInputStream ois : oiss.values()) {
      try {
        ois.close();
      } catch (IOException e) {
        //Do nothing - nothing can be done
      }
    }

    for (ObjectOutputStream oos : ooss.values()) {
      try {
        oos.close();
      } catch (IOException e) {
        //Do nothing - nothing can be done
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
