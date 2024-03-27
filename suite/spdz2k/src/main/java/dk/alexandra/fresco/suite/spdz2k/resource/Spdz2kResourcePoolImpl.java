package dk.alexandra.fresco.suite.spdz2k.resource;

import dk.alexandra.fresco.tools.commitment.CoinTossingComputation;
import dk.alexandra.fresco.tools.commitment.HashBasedCommitmentSerializer;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.NetworkBatchDecorator;
import dk.alexandra.fresco.framework.sce.evaluator.ProtocolCollectionList;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.OpenedValueStore;
import dk.alexandra.fresco.framework.util.ValidationUtils;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.spdz2k.Spdz2kBuilder;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSInt;
import dk.alexandra.fresco.suite.spdz2k.resource.storage.Spdz2kDataSupplier;
import java.io.Closeable;
import java.math.BigInteger;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Default implementation of {@link Spdz2kResourcePool}. <p>If a securely generated, joint random
 * seed is needed, {@link #initializeJointRandomness(Supplier, Function, int)} must be called before
 * using this class.</p>
 */
public class Spdz2kResourcePoolImpl<PlainT extends CompUInt<?, ?, PlainT>>
    extends ResourcePoolImpl
    implements Spdz2kResourcePool<PlainT> {

  private final int effectiveBitLength;
  private final OpenedValueStore<Spdz2kSInt<PlainT>, PlainT> storage;
  private final Spdz2kDataSupplier<PlainT> supplier;
  private final CompUIntFactory<PlainT> factory;
  private final ByteSerializer<PlainT> rawSerializer;
  private final Drbg localDrbg;
  private Drbg drbg;

  /**
   * Creates new {@link Spdz2kResourcePoolImpl}.
   */
  public Spdz2kResourcePoolImpl(int myId, int noOfPlayers, Drbg drbg,
      OpenedValueStore<Spdz2kSInt<PlainT>, PlainT> storage,
      Spdz2kDataSupplier<PlainT> supplier, CompUIntFactory<PlainT> factory) {
    super(myId, noOfPlayers);
    ValidationUtils.assertValidId(myId, noOfPlayers);
    Objects.requireNonNull(storage);
    Objects.requireNonNull(supplier);
    Objects.requireNonNull(factory);
    this.effectiveBitLength = factory.getLowBitLength();
    this.storage = storage;
    this.supplier = supplier;
    this.factory = factory;
    this.rawSerializer = factory.getSerializer();
    this.drbg = drbg;
    this.localDrbg = new AesCtrDrbg();
  }

  @Override
  public int getMaxBitLength() {
    return effectiveBitLength;
  }

  @Override
  public OpenedValueStore<Spdz2kSInt<PlainT>, PlainT> getOpenedValueStore() {
    return storage;
  }

  @Override
  public Spdz2kDataSupplier<PlainT> getDataSupplier() {
    return supplier;
  }

  @Override
  public CompUIntFactory<PlainT> getFactory() {
    return factory;
  }

  @Override
  public void initializeJointRandomness(Supplier<Network> networkSupplier,
      Function<byte[], Drbg> drbgGenerator, int seedLength) {
    Network network = networkSupplier.get();
    Computation<byte[], ProtocolBuilderNumeric> coinTossing =
        new CoinTossingComputation(seedLength, new HashBasedCommitmentSerializer(),
            getLocalRandomGenerator());
    byte[] jointSeed = runCoinTossing(coinTossing, network);
    drbg = drbgGenerator.apply(jointSeed);
    ExceptionConverter.safe(() -> {
      ((Closeable) network).close();
      return null;
    }, "Failed to close network");
  }

  @Override
  public FieldDefinition getFieldDefinition() {
    return factory;
  }

  @Override
  public BigInteger getModulus() {
    return BigInteger.ONE.shiftLeft(effectiveBitLength);
  }

  @Override
  public Drbg getRandomGenerator() {
    if (drbg == null) {
      throw new IllegalStateException("Joint drbg must be initialized before use");
    }
    return drbg;
  }

  @Override
  public Drbg getLocalRandomGenerator() {
    return localDrbg;
  }

  /**
   * Evaluates, on the fly, a coin-tossing computation to get joint seed.
   */
  private byte[] runCoinTossing(Computation<byte[], ProtocolBuilderNumeric> coinTossing,
      Network network) {
    NetworkBatchDecorator networkBatchDecorator =
        new NetworkBatchDecorator(
            this.getNoOfParties(),
            network);
    BuilderFactoryNumeric builderFactory = new Spdz2kBuilder<>(factory,
        new BasicNumericContext(effectiveBitLength, getMyId(), getNoOfParties(), null, getMaxBitLength()));
    ProtocolBuilderNumeric root = builderFactory.createSequential();
    DRes<byte[]> jointSeed = coinTossing
        .buildComputation(root);
    ProtocolProducer coinTossingProducer = root.build();
    do {
      ProtocolCollectionList<Spdz2kResourcePool> protocolCollectionList =
          new ProtocolCollectionList<>(
              128); // batch size is irrelevant since this is a very light-weight protocol
      coinTossingProducer.getNextProtocols(protocolCollectionList);
      new BatchedStrategy<Spdz2kResourcePool>()
          .processBatch(protocolCollectionList, this, networkBatchDecorator);
    } while (coinTossingProducer.hasNextProtocols());
    return jointSeed.out();
  }
}
