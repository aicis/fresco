package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.preprocessing.MascotFormatConverter;
import dk.alexandra.fresco.tools.mascot.Mascot;
import dk.alexandra.fresco.tools.mascot.MascotResourcePoolImpl;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.MultTriple;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.utils.FieldElementPrgImpl;
import dk.alexandra.fresco.tools.ot.base.DummyOt;
import dk.alexandra.fresco.tools.ot.base.Ot;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import dk.alexandra.fresco.tools.ot.otextension.BristolRotBatch;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePool;
import dk.alexandra.fresco.tools.ot.otextension.OtExtensionResourcePoolImpl;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpdzMascotDataSupplier implements SpdzDataSupplier {

  private static final Logger logger = LoggerFactory.getLogger(SpdzMascotDataSupplier.class);
  private final int myId;
  private final int numberOfPlayers;
  private final Supplier<Network> tripleNetwork;
  private final BigInteger modulus;
  private final Function<Integer, SpdzSInt[]> preprocessedValues;
  private final FieldElement ssk;

  private Mascot mascot;
  private int prgSeedLength;
  private ArrayDeque<MultTriple> triples;
  private int maxBitLength;
  private int batchSize;

  public static SpdzMascotDataSupplier createTestSupplier(
      int myId, int numberOfPlayers, Supplier<Network> tripleNetwork,
      Function<Integer, SpdzSInt[]> preprocessedValues) {
    return new SpdzMascotDataSupplier(
        myId, numberOfPlayers, tripleNetwork,
        new BigInteger("340282366920938463463374607431768211297"),
        128, preprocessedValues, 256, 1000);
  }


  private SpdzMascotDataSupplier(
      int myId, int numberOfPlayers, Supplier<Network> tripleNetwork, BigInteger modulus,
      int maxBitLength,
      Function<Integer, SpdzSInt[]> preprocessedValues,
      int prgSeedLength, int batchSize) {
    this(myId, numberOfPlayers, tripleNetwork, modulus, maxBitLength, preprocessedValues,
        prgSeedLength, batchSize,
        createRandomSsk(myId, modulus, maxBitLength, prgSeedLength));
  }

  public SpdzMascotDataSupplier(
      int myId, int numberOfPlayers, Supplier<Network> tripleNetwork, BigInteger modulus,
      int maxBitLength, Function<Integer, SpdzSInt[]> preprocessedValues, int prgSeedLength,
      int batchSize,
      FieldElement ssk) {
    this.myId = myId;
    this.numberOfPlayers = numberOfPlayers;
    this.tripleNetwork = tripleNetwork;
    this.modulus = modulus;
    this.preprocessedValues = preprocessedValues;
    this.triples = new ArrayDeque<>();
    this.prgSeedLength = prgSeedLength;
    this.maxBitLength = maxBitLength;
    this.batchSize = batchSize;
    this.ssk = ssk;
  }

  private static FieldElement createRandomSsk(long myId, BigInteger modulus, int maxBitLength,
      int prgSeedLength) {
    StrictBitVector seed = new StrictBitVector(prgSeedLength,
        new PaddingAesCtrDrbg(new byte[3], 32 * 8));
    FieldElementPrg localSampler = new FieldElementPrgImpl(seed);
    return localSampler.getNext(modulus, maxBitLength);
  }

  @Override
  public SpdzTriple getNextTriple() {
    ensureInitialized();
    if (triples.isEmpty()) {
      logger.info("Getting another triple batch");
      triples.addAll(mascot.getTriples(batchSize));
      logger.info("Got another triple batch");
    }
    MultTriple triple = triples.pop();
    return MascotFormatConverter.toSpdzTriple(triple);
  }

  private void ensureInitialized() {
    if (mascot != null) {
      return;
    }
    List<Integer> partyIds =
        IntStream.range(1, numberOfPlayers + 1)
            .boxed()
            .collect(Collectors.toList());

    int numLeftFactors = 3;
    Network network = tripleNetwork.get();
    mascot = new Mascot(new MascotResourcePoolImpl(
        myId, partyIds,
        new PaddingAesCtrDrbg(new byte[]{7, 127, -1}, prgSeedLength),
        getModulus(), maxBitLength,
        128, prgSeedLength, numLeftFactors) {
      @Override
      public RotBatch createRot(int otherId, Network network) {
        Ot ot = new DummyOt(otherId, network);
        OtExtensionResourcePool otResources = new OtExtensionResourcePoolImpl(getMyId(), otherId,
            getModBitLength(), getLambdaSecurityParam(), getRandomGenerator());
        return new BristolRotBatch(otResources, network, ot);
      }
    }, network, ssk);
  }

  @Override
  public BigInteger getModulus() {
    return modulus;
  }

  public BigInteger getSSK() {
    return this.ssk.toBigInteger();
  }

  @Override
  public SpdzSInt getNextRandomFieldElement() {
    return new SpdzSInt(this.getNextTriple().getA());
  }

  @Override
  public SpdzSInt[] getNextExpPipe() {
    return preprocessedValues.apply(maxBitLength);
  }

  @Override
  public SpdzInputMask getNextInputMask(int towardPlayerID) {
    // TODO Peter Nordholt will fix this
    return new SpdzDummyDataSupplier(myId, numberOfPlayers).getNextInputMask(towardPlayerID);
  }

  @Override
  public SpdzSInt getNextBit() {
    // TODO Nikolaj Volgusjef will fix
    return new SpdzDummyDataSupplier(myId, numberOfPlayers).getNextBit();
  }
}
