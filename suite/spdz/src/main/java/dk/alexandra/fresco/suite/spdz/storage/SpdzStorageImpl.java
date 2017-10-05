package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.sce.resources.storage.StreamedStorage;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzElement;
import dk.alexandra.fresco.suite.spdz.storage.rest.DataRestSupplierImpl;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

/**
 * Uses the D14.2 storage concept as backend
 *
 * @author Kasper Damgaard
 */
public class SpdzStorageImpl implements SpdzStorage {

  private List<BigInteger> opened_values;
  private List<SpdzElement> closed_values;

  private DataSupplier supplier;

  /**
   * @param storageId The unique id of the storage. This could e.g. be the threadId of the thread
   * that will use this storage object
   * @param noOfParties party number
   * @param myId my party id
   * @param streamedStorage spdz store
   */
  public SpdzStorageImpl(int storageId, int noOfParties, int myId,
      StreamedStorage streamedStorage) {
    int noOfThreadsUsed = 1;

    String storageName =
        SpdzStorageConstants.STORAGE_NAME_PREFIX + noOfThreadsUsed + "_" + myId + "_" + storageId
            + "_";

    opened_values = new LinkedList<>();
    closed_values = new LinkedList<>();

    this.supplier = new DataSupplierImpl(streamedStorage, storageName, noOfParties);
  }

  public SpdzStorageImpl(int storageId, int noOfParties, int myId,
      String fuelStationBaseUrl) {

    opened_values = new LinkedList<>();
    closed_values = new LinkedList<>();

    this.supplier = new DataRestSupplierImpl(myId, noOfParties, fuelStationBaseUrl, storageId);
  }

  @Override
  public void shutdown() {
    this.supplier.shutdown();
  }

  @Override
  public void reset() {
    opened_values.clear();
    closed_values.clear();
  }

  @Override
  public DataSupplier getSupplier() {
    return this.supplier;
  }

  @Override
  public void addOpenedValue(BigInteger val) {
    opened_values.add(val);
  }

  @Override
  public void addClosedValue(SpdzElement elem) {
    closed_values.add(elem);
  }

  @Override
  public List<BigInteger> getOpenedValues() {
    return opened_values;
  }

  @Override
  public List<SpdzElement> getClosedValues() {
    return closed_values;
  }

  @Override
  public BigInteger getSSK() {
    return this.supplier.getSSK();
  }

}
