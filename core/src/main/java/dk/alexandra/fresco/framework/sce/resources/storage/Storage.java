package dk.alexandra.fresco.framework.sce.resources.storage;

import java.io.Serializable;

/**
 * Generic storage interface for the different run-times.
 *
 */
public interface Storage {

  /**
   * Stores a serializable object under the given key. If the key already exists, an exception
   * should be thrown. If the name does not exist, an entry will be created under that name for
   * future use.
   * 
   * @param name The name of the databaseId/filename that you want to use
   * @param key the id to store by.
   * @param o the (serializable) object to store.
   * @return true if all went well, false if we could not insert the object in the storage.
   */
  public boolean putObject(String name, String key, Serializable o);

  /**
   * Returns all objects stored under the given id. If none are stored, null is returned instead.
   * 
   * @param name The name of the databaseId/filename that you want to use
   * @param key The id to search for.
   * @return the object stored under the given id. If the storage contains nothing, null is
   *         returned.
   * 
   */
  public <T extends Serializable> T getObject(String name, String key);

}
