package dk.alexandra.fresco.framework.sce.resources.storage;

import dk.alexandra.fresco.framework.sce.resources.storage.exceptions.NoMoreElementsException;
import java.io.Serializable;

public interface StreamedStorage extends Storage {

  /**
   * Returns the next object from the storage with the given name. This could be e.g. the given
   * filename.
   *
   * @param name The name of the storage to get from. This could e.g. be a filename.
   * @return the next object in line
   */
  public <T extends Serializable> T getNext(String name) throws NoMoreElementsException;

  /**
   * Inserts an object into the storage with the given name. This could be e.g. append to a file
   * with the filename as 'name'.
   *
   * @param name The storage to put objects into.
   * @param o The object to store.
   * @return true if the object was stored, false otherwise:
   */
  public boolean putNext(String name, Serializable o);

  /**
   * Closes any open connections to the storage.
   */
  public void shutdown();
}
