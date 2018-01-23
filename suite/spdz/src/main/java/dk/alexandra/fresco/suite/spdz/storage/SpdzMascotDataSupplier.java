package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.PaddingAesCtrDrbg;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.preprocessing.MascotFormatConverter;
import dk.alexandra.fresco.tools.mascot.Mascot;
import dk.alexandra.fresco.tools.mascot.MascotResourcePoolImpl;
import dk.alexandra.fresco.tools.mascot.MascotSecurityParameters;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.InputMask;
import dk.alexandra.fresco.tools.mascot.field.MultiplicationTriple;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrg;
import dk.alexandra.fresco.tools.mascot.prg.FieldElementPrgImpl;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A data supplier based on the Mascot protocol. Uses concrete implementation {@link Mascot}.
 */
public class SpdzMascotDataSupplier implements SpdzDataSupplier {

  private static final Logger logger = LoggerFactory.getLogger(SpdzMascotDataSupplier.class);
  private final int myId;
  private final int instanceId;
  private final int numberOfPlayers;
  private final Supplier<Network> tripleNetwork;
  private final BigInteger modulus;
  private final Function<Integer, SpdzSInt[]> preprocessedValues;
  private final FieldElement ssk;

  private final ArrayDeque<MultiplicationTriple> triples;
  private final ArrayDeque<InputMask> masks;
  private final ArrayDeque<AuthenticatedElement> randomElements;
  private final ArrayDeque<AuthenticatedElement> randomBits;
  private final int prgSeedLength;
  private final int modBitLength;
  private final int batchSize;
  private final Drbg drbg;
  private final Map<Integer, RotList> seedOts;
  private Mascot mascot;

  /**
   * Creates {@link SpdzMascotDataSupplier}.
   *
   * @param myId this party's id
   * @param numberOfPlayers number of players
   * @param instanceId identifier used to distinguish parallel instances of Mascot
   * @param tripleNetwork network supplier for network to be used by Mascot instance
   * @param modulus field modulus
   * @param modBitLength bit length of modulus
   * @param preprocessedValues callback to generate exponentiation pipes
   * @param prgSeedLength bit length of prg
   * @param batchSize batch size in which Mascot will generate pre-processed material
   * @param ssk mac key share
   * @param seedOts pre-computed base OTs
   * @param drbg source of randomness
   */
  public SpdzMascotDataSupplier(int myId, int numberOfPlayers, int instanceId,
      Supplier<Network> tripleNetwork, BigInteger modulus, int modBitLength,
      Function<Integer, SpdzSInt[]> preprocessedValues, int prgSeedLength, int batchSize,
      FieldElement ssk, Map<Integer, RotList> seedOts, Drbg drbg) {
    this.myId = myId;
    this.numberOfPlayers = numberOfPlayers;
    this.instanceId = instanceId;
    this.tripleNetwork = tripleNetwork;
    this.modulus = modulus;
    this.preprocessedValues = preprocessedValues;
    this.triples = new ArrayDeque<>();
    this.masks = new ArrayDeque<>();
    this.randomElements = new ArrayDeque<>();
    this.randomBits = new ArrayDeque<>();
    this.prgSeedLength = prgSeedLength;
    this.modBitLength = modBitLength;
    this.batchSize = batchSize;
    this.ssk = ssk;
    this.seedOts = seedOts;
    this.drbg = drbg;
  }

  /**
   * Creates instance of {@link SpdzMascotDataSupplier}.
   */
  public static SpdzMascotDataSupplier createSimpleSupplier(int myId, int numberOfPlayers,
      Supplier<Network> tripleNetwork, int modBitLength, BigInteger modulus,
      Function<Integer, SpdzSInt[]> preprocessedValues,
      Map<Integer, RotList> seedOts, Drbg drbg, FieldElement ssk) {
    int prgSeedLength = 256;
    return new SpdzMascotDataSupplier(myId, numberOfPlayers, 1, tripleNetwork, modulus,
        modBitLength, preprocessedValues, prgSeedLength, 16, ssk, seedOts, drbg);
  }

  /**
   * Creates random field element that can be used as the mac key share by the calling party.
   */
  public static FieldElement createRandomSsk(BigInteger modulus, int prgSeedLength) {
    byte[] seedBytes = new byte[prgSeedLength / 8];
    new SecureRandom().nextBytes(seedBytes);
    StrictBitVector seed = new StrictBitVector(prgSeedLength, new PaddingAesCtrDrbg(seedBytes));
    FieldElementPrg localSampler = new FieldElementPrgImpl(seed);
    return localSampler.getNext(modulus);
  }

  @Override
  public SpdzTriple getNextTriple() {
    ensureInitialized();
    if (triples.isEmpty()) {
      logger.trace("Getting another triple batch");
      triples.addAll(mascot.getTriples(batchSize));
      logger.trace("Got another triple batch");
    }
    MultiplicationTriple triple = triples.pop();
    return MascotFormatConverter.toSpdzTriple(triple);
  }

  @Override
  public SpdzSInt getNextRandomFieldElement() {
    ensureInitialized();
    if (randomElements.isEmpty()) {
      logger.trace("Getting another random element batch");
      randomElements.addAll(mascot.getRandomElements(batchSize));
      logger.trace("Got another random element batch");
    }
    return new SpdzSInt(MascotFormatConverter.toSpdzElement(randomElements.pop()));
  }

  @Override
  public SpdzSInt[] getNextExpPipe() {
    logger.trace("Getting another exp pipe");
    SpdzSInt[] pipe = preprocessedValues.apply(modBitLength);
    logger.trace("Got another exp pipe");
    return pipe;
  }

  @Override
  public SpdzInputMask getNextInputMask(int towardPlayerID) {
    ensureInitialized();
    if (masks.isEmpty()) {
      logger.trace("Getting another mask batch");
      masks.addAll(mascot.getInputMasks(towardPlayerID, batchSize));
      logger.trace("Got another mask batch");
    }
    return MascotFormatConverter.toSpdzInputMask(masks.pop());
  }

  @Override
  public SpdzSInt getNextBit() {
    ensureInitialized();
    if (randomBits.isEmpty()) {
      logger.trace("Getting another bit batch");
      randomBits.addAll(mascot.getRandomBits(batchSize));
      logger.trace("Got another bit batch");
    }
    return new SpdzSInt(MascotFormatConverter.toSpdzElement(randomBits.pop()));
  }

  @Override
  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public BigInteger getSecretSharedKey() {
    return this.ssk.toBigInteger();
  }

  private void ensureInitialized() {
    if (mascot != null) {
      return;
    }
    int numCandidatesPerTriple = 3;
    mascot = new Mascot(
        new MascotResourcePoolImpl(myId, numberOfPlayers, instanceId, drbg, seedOts,
            new MascotSecurityParameters(modBitLength, modBitLength, prgSeedLength,
                numCandidatesPerTriple)), tripleNetwork.get(), ssk);
  }

}
