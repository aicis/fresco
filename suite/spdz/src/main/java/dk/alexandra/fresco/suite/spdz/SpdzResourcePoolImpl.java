package dk.alexandra.fresco.suite.spdz;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.ValidationUtils;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.storage.SpdzDataSupplier;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.function.Function;

public final class SpdzResourcePoolImpl extends ResourcePoolImpl implements SpdzResourcePool {

  private static final int DRBG_SEED_LENGTH = 256;

  private final MessageDigest messageDigest;
  private final OpenedValueStore<SpdzSInt, FieldElement> openedValueStore;
  private final SpdzDataSupplier dataSupplier;
  private final Function<byte[], Drbg> drbgSupplier;
  private final int drbgSeedBitLength;

  /**
   * Construct a ResourcePool implementation suitable for the spdz protocol suite.
   *
   * @param myId The id of the party
   * @param noOfPlayers The amount of parties
   * @param openedValueStore Store for maintaining opened values for later mac check. Not nullable.
   * @param dataSupplier Pre-processing material supplier. Not nullable.
   * @param drbgSupplier Function instantiating DRBG with given seed. Not nullable.
   * @param drbgSeedBitLength Required bit length of seed used for DRBGs
   */
  public SpdzResourcePoolImpl(int myId, int noOfPlayers,
      OpenedValueStore<SpdzSInt, FieldElement> openedValueStore, SpdzDataSupplier dataSupplier,
      Function<byte[], Drbg> drbgSupplier, int drbgSeedBitLength) {
    super(myId, noOfPlayers);
    ValidationUtils.assertValidId(myId, noOfPlayers);
    this.dataSupplier = Objects.requireNonNull(dataSupplier);
    this.openedValueStore = Objects.requireNonNull(openedValueStore);
    this.messageDigest = ExceptionConverter.safe(
        () -> MessageDigest.getInstance("SHA-256"),
        "Configuration error, SHA-256 is needed for Spdz");
    this.drbgSupplier = Objects.requireNonNull(drbgSupplier);
    this.drbgSeedBitLength = drbgSeedBitLength;
  }

  /**
   * Default call to {@link #SpdzResourcePoolImpl(int, int, OpenedValueStore, SpdzDataSupplier,
   * Function, int)} with default DRBG seed length.
   */
  public SpdzResourcePoolImpl(int myId, int noOfPlayers,
      OpenedValueStore<SpdzSInt, FieldElement> openedValueStore, SpdzDataSupplier dataSupplier,
      Function<byte[], Drbg> drbgSupplier) {
    this(myId, noOfPlayers, openedValueStore, dataSupplier, drbgSupplier, DRBG_SEED_LENGTH);
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
  public int getDrbgSeedBitLength() {
    return drbgSeedBitLength;
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
