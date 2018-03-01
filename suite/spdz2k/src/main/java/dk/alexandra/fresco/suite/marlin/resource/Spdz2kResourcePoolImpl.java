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
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.marlin.Spdz2kBuilder;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUInt;
import dk.alexandra.fresco.suite.marlin.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.marlin.protocols.computations.Spdz2kCommitmentComputation;
import dk.alexandra.fresco.suite.marlin.resource.storage.Spdz2kDataSupplier;
import dk.alexandra.fresco.suite.marlin.resource.storage.Spdz2kOpenedValueStore;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Spdz2kResourcePoolImpl<PlainT extends CompUInt<?, ?, PlainT>>
    extends ResourcePoolImpl
    implements Spdz2kResourcePool<PlainT> {

  private static final Logger logger = LoggerFactory.getLogger(Spdz2kResourcePoolImpl.class);

  private final int operationalBitLength;
  private final int effectiveBitLength;
  private final BigInteger modulus;
  private final Spdz2kOpenedValueStore<PlainT> storage;
  private final Spdz2kDataSupplier<PlainT> supplier;
  private final CompUIntFactory<PlainT> factory;
  private final ByteSerializer<PlainT> rawSerializer;
  private Drbg drbg;

  /**
   * Creates new {@link Spdz2kResourcePoolImpl}.
   */
  public Spdz2kResourcePoolImpl(int myId, int noOfPlayers, Drbg drbg,
      Spdz2kOpenedValueStore<PlainT> storage,
      Spdz2kDataSupplier<PlainT> supplier, CompUIntFactory<PlainT> factory) {
    super(myId, noOfPlayers);
    this.operationalBitLength = factory.getCompositeBitLength();
    this.effectiveBitLength = factory.getLowBitLength();
    this.modulus = BigInteger.ONE.shiftLeft(effectiveBitLength);
    this.storage = storage;
    this.supplier = supplier;
    this.factory = factory;
    this.rawSerializer = factory.createSerializer();
    this.drbg = drbg;
  }

  @Override
  public int getMaxBitLength() {
    return effectiveBitLength;
  }

  @Override
  public Spdz2kOpenedValueStore<PlainT> getOpenedValueStore() {
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
  public ByteSerializer<PlainT> getRawSerializer() {
    return rawSerializer;
  }

  @Override
  public void initializeJointRandomness(Supplier<Network> networkSupplier,
      Function<byte[], Drbg> drbgGenerator, int seedLength) {
    // TODO clean this up
//    byte[] fixedSeed = new byte[32];
//    fixedSeed[0] = 1;
//    drbg = drbgGenerator.apply(fixedSeed);
    BasicNumericContext numericContext = new BasicNumericContext(effectiveBitLength, modulus,
        getMyId(), getNoOfParties());
    Network network = networkSupplier.get();
    logger.debug("Got network " + getMyId());

    NetworkBatchDecorator networkBatchDecorator =
        new NetworkBatchDecorator(
            this.getNoOfParties(),
            network);
    logger.debug("Got network decorator" + getMyId());
    BuilderFactoryNumeric builderFactory = new Spdz2kBuilder<>(factory, numericContext);
    ProtocolBuilderNumeric root = builderFactory.createSequential();
    byte[] ownSeed = new byte[seedLength];
    new SecureRandom().nextBytes(ownSeed);
    DRes<List<byte[]>> seeds = new Spdz2kCommitmentComputation(
        this.getCommitmentSerializer(),
        ownSeed)
        .buildComputation(root);
    ProtocolProducer commitmentProducer = root.build();
    logger.debug("Build producer " + getMyId());
    int counter = 0;
    do {
      logger.debug("Processing  " + getMyId() + " batch number " + counter++);
      ProtocolCollectionList<Spdz2kResourcePool> protocolCollectionList =
          new ProtocolCollectionList<>(
              1); // batch size is irrelevant since this is a very light-weight protocol
      commitmentProducer.getNextProtocols(protocolCollectionList);
      new BatchedStrategy<Spdz2kResourcePool>()
          .processBatch(protocolCollectionList, this, networkBatchDecorator);
    } while (commitmentProducer.hasNextProtocols());
    byte[] jointSeed = new byte[seedLength];
    for (byte[] seed : seeds.out()) {
      ByteArrayHelper.xor(jointSeed, seed);
    }
    drbg = drbgGenerator.apply(jointSeed);
    logger.debug("Generated joint seed successfully " + getMyId() + " seed " + Arrays
        .toString(jointSeed));
//    ExceptionConverter.safe(() -> {
//      ((Closeable) network).close();
//      logger.debug("Closed network " + getMyId() + " seed " + Arrays
//          .toString(jointSeed));
//      return null;
//    }, "Failed to close network");

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
