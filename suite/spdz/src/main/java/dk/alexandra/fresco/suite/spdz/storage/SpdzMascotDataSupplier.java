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
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Random;
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
  private final FieldElement ssk;

  private Mascot mascot;
  private int prgSeedLength;
  private ArrayDeque<MultTriple> triples;
  private int maxBitLength;
  private int batchSize;

  private SpdzMascotDataSupplier(
      int myId, int numberOfPlayers, Supplier<Network> tripleNetwork, BigInteger modulus) {
    this.myId = myId;
    this.numberOfPlayers = numberOfPlayers;
    this.tripleNetwork = tripleNetwork;
    this.modulus = modulus;
    this.triples = new ArrayDeque<>();

    prgSeedLength = 256;
    maxBitLength = 128;
    batchSize = 1000;

    Random rand = new Random((long) myId);
    StrictBitVector seed = new StrictBitVector(prgSeedLength, rand);
    FieldElementPrg localSampler = new FieldElementPrgImpl(seed);
    ssk = localSampler.getNext(modulus, maxBitLength);
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
    mascot = new Mascot(new MascotResourcePoolImpl(
        myId, partyIds,
        new PaddingAesCtrDrbg(new byte[]{7, 127, -1}, prgSeedLength),
        getModulus(), maxBitLength,
        128, prgSeedLength, numLeftFactors) {
      @Override
      public RotBatch<StrictBitVector> createRot(int otherId, Network network) {
        Ot ot = new DummyOt(otherId, network);
        return new BristolRotBatch(getMyId(), otherId, getModBitLength(), getLambdaSecurityParam(),
            getRandomGenerator(), network, ot);
      }
    }, tripleNetwork.get(), ssk);
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
    return new SpdzSInt[0];
  }

  @Override
  public SpdzInputMask getNextInputMask(int towardPlayerID) {
    // TODO
    return null;
  }

  @Override
  public SpdzSInt getNextBit() {
    // TODO Nikolaj Volgusjef will fix
    return null;
  }
}
