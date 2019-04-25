package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import java.security.MessageDigest;
import java.util.function.Function;

public class SpdzResourcePoolImpl extends ResourcePoolImpl implements SpdzResourcePool {

  private final MessageDigest messageDigest;
  private final OpenedValueStore<SpdzSInt, FieldElement> openedValueStore;
  private final SpdzDataSupplier dataSupplier;
  private final Function<byte[], Drbg> drbgSupplier;

  /**
   * Construct a ResourcePool implementation suitable for the spdz protocol suite.
   *
   * @param myId The id of the party
   * @param noOfPlayers The amount of parties
   * @param openedValueStore Store for maintaining opened values for later mac check
   * @param dataSupplier Pre-processing material supplier
   * @param drbgSupplier Function instantiating DRBG with given seed
   */
  public SpdzResourcePoolImpl(int myId, int noOfPlayers,
      OpenedValueStore<SpdzSInt, FieldElement> openedValueStore, SpdzDataSupplier dataSupplier,
      Function<byte[], Drbg> drbgSupplier) {
    super(myId, noOfPlayers);
    this.dataSupplier = dataSupplier;
    this.openedValueStore = openedValueStore;
    this.messageDigest = ExceptionConverter.safe(
        () -> MessageDigest.getInstance("SHA-256"),
        "Configuration error, SHA-256 is needed for Spdz");
    this.drbgSupplier = drbgSupplier;
  }

  @Override
  public FieldDefinition getFieldDefinition() {
    return dataSupplier.getFieldDefinition();
  }

  @Override
  public OpenedValueStore<SpdzSInt, FieldElement> getOpenedValueStore() {
    return openedValueStore;
  }

  @Override
  public SpdzDataSupplier getDataSupplier() {
    return dataSupplier;
  }

  @Override
  public MessageDigest getMessageDigest() {
    return messageDigest;
  }

  @Override
  public Drbg createRandomGenerator(byte[] seed) {
    return drbgSupplier.apply(seed);
  }

}
