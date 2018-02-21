package dk.alexandra.fresco.suite.marlin.resource;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.network.serializers.ByteSerializer;
import dk.alexandra.fresco.framework.sce.evaluator.BatchedStrategy;
import dk.alexandra.fresco.framework.sce.evaluator.NetworkBatchDecorator;
import dk.alexandra.fresco.framework.sce.evaluator.ProtocolCollectionList;
import dk.alexandra.fresco.framework.sce.resources.Broadcast;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.marlin.MarlinBuilder;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.datatypes.UInt;
import dk.alexandra.fresco.suite.marlin.protocols.computations.MarlinCommitmentComputation;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.MarlinOpenedValueStore;
import java.io.Closeable;
import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class MarlinResourcePoolImpl<
    H extends UInt<H>,
    L extends UInt<L>,
    T extends CompUInt<H, L, T>> extends ResourcePoolImpl
    implements MarlinResourcePool<H, L, T> {

  private final int operationalBitLength;
  private final int effectiveBitLength;
  private final BigInteger modulus;
  private final MarlinOpenedValueStore<H, L, T> storage;
  private final MarlinDataSupplier<H, L, T> supplier;
  private final CompUIntFactory<H, L, T> factory;
  private final ByteSerializer<T> rawSerializer;
  private Drbg drbg;

  /**
   * Creates new {@link MarlinResourcePool}.
   */
  public MarlinResourcePoolImpl(int myId, int noOfPlayers, Drbg drbg,
      MarlinOpenedValueStore<H, L, T> storage,
      MarlinDataSupplier<H, L, T> supplier, CompUIntFactory<H, L, T> factory) {
    super(myId, noOfPlayers);
    this.operationalBitLength = factory.getCompositeBitLength();
    this.effectiveBitLength = factory.getLowBitLength();
    this.modulus = BigInteger.ONE.shiftLeft(operationalBitLength);
    this.storage = storage;
    this.supplier = supplier;
    this.factory = factory;
    this.rawSerializer = factory.createSerializer();
    this.drbg = drbg;
  }

  @Override
  public int getOperationalBitLength() {
    return operationalBitLength;
  }

  @Override
  public int getEffectiveBitLength() {
    return effectiveBitLength;
  }

  @Override
  public MarlinOpenedValueStore<H, L, T> getOpenedValueStore() {
    return storage;
  }

  @Override
  public MarlinDataSupplier<H, L, T> getDataSupplier() {
    return supplier;
  }

  @Override
  public CompUIntFactory<H, L, T> getFactory() {
    return factory;
  }

  @Override
  public ByteSerializer<T> getRawSerializer() {
    return rawSerializer;
  }

  @Override
  public void initializeJointRandomness(Supplier<Network> networkSupplier,
      Function<byte[], Drbg> drbgGenerator, int seedLength) {
    // TODO clean this up
    BasicNumericContext numericContext = new BasicNumericContext(effectiveBitLength, modulus,
        getMyId(), getNoOfParties());
    Network network = networkSupplier.get();
    NetworkBatchDecorator networkBatchDecorator =
        new NetworkBatchDecorator(
            this.getNoOfParties(),
            network);
    BuilderFactoryNumeric builderFactory = new MarlinBuilder<>(factory, numericContext);
    ProtocolBuilderNumeric root = builderFactory.createSequential();
    byte[] ownSeed = new byte[seedLength];
    System.out.println(MarlinResourcePool.class + " change me back!");
//    new SecureRandom().nextBytes(ownSeed);
    DRes<List<byte[]>> seeds = new MarlinCommitmentComputation(
        this.getCommitmentSerializer(),
        ownSeed)
        .buildComputation(root);
    ProtocolProducer commitmentProducer = root.build();
    do {
      ProtocolCollectionList<MarlinResourcePool> protocolCollectionList =
          new ProtocolCollectionList<>(
              128); // batch size is irrelevant since this is a very light-weight protocol
      commitmentProducer.getNextProtocols(protocolCollectionList);
      new BatchedStrategy<MarlinResourcePool>()
          .processBatch(protocolCollectionList, this, networkBatchDecorator);
    } while (commitmentProducer.hasNextProtocols());
    byte[] jointSeed = new byte[seedLength];
    for (byte[] seed : seeds.out()) {
      ByteArrayHelper.xor(jointSeed, seed);
    }
    drbg = drbgGenerator.apply(jointSeed);
    ExceptionConverter.safe(() -> {
      ((Closeable) network).close();
      return null;
    }, "Failed to close network");
  }

  @Override
  public Broadcast createBroadcast(Network network) {
    // TODO come up with way to cache this
    return new Broadcast(network);
  }

  @Override
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public ByteSerializer<BigInteger> getSerializer() {
    throw new UnsupportedOperationException("This suite does not support serializing big integers");
  }

  @Override
  public Drbg getRandomGenerator() {
    if (drbg == null) {
      throw new IllegalStateException("Joint drbg must be initialized before use");
    }
    return drbg;
  }

}
