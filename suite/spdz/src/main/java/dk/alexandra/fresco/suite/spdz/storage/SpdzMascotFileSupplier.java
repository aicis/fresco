package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.suite.spdz.preprocessing.MascotFormatConverter;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.field.InputMask;
import dk.alexandra.fresco.tools.mascot.field.MultiplicationTriple;
import dk.alexandra.fresco.tools.mascot.file.AuthenticatedElementSerializer;
import dk.alexandra.fresco.tools.mascot.file.ElementIO;
import dk.alexandra.fresco.tools.mascot.file.InputMaskSerializer;
import dk.alexandra.fresco.tools.mascot.file.MascotSettings;
import dk.alexandra.fresco.tools.mascot.file.MultiplicationTripleSerializer;
import dk.alexandra.fresco.tools.mascot.file.SettingsIO;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class SpdzMascotFileSupplier implements SpdzDataSupplier {

  public static final String AUTH_ELEM_SUFFIX = "-authElem";
  public static final String BIT_SUFFIX = "-bit";
  public static final String INPUT_SUFFIX = "-input";
  public static final String TRIPLE_SUFFIX = "-trip";
  private static final Logger logger = LoggerFactory.getLogger(SpdzMascotFileSupplier.class);
  private final MascotSettings settings;
  private final ElementIO<AuthenticatedElement> authElemIO;
  private final ElementIO<AuthenticatedElement> bitElemIO;
  private final Map<Integer, ElementIO<InputMask>> inputMaskIO;
  private final ElementIO<MultiplicationTriple> tripleIO;
  private final ArrayDeque<AuthenticatedElement> authElems;
  private final ArrayDeque<AuthenticatedElement> bitElems;
  private final ArrayDeque<MultiplicationTriple> triples;
  private final Map<Integer, ArrayDeque<InputMask>> masks;
  private final int batchSize;
  private final FieldElement macShare;
  private final BigInteger modulus;

  /**
   * Creates {@link SpdzMascotDataSupplier}.
   *
   * @param fileDir the name of the settings file
   * @param batchSize batch size in which Mascot will generate pre-processed material
   */
  public SpdzMascotFileSupplier(String fileDir, int batchSize) {
    SettingsIO<MascotSettings> settingsIO = new SettingsIO<>();
    settings = settingsIO.readFile(fileDir);
    this.modulus = settings.getModulus();
    this.macShare = settings.getMacShare();
    this.batchSize = batchSize;

    authElemIO = new ElementIO<>(fileDir + AUTH_ELEM_SUFFIX,
        new AuthenticatedElementSerializer(settings.getModulus()));
    bitElemIO = new ElementIO<>(fileDir + BIT_SUFFIX,
        new AuthenticatedElementSerializer(settings.getModulus()));
    tripleIO = new ElementIO<>(fileDir + TRIPLE_SUFFIX,
        new MultiplicationTripleSerializer(settings.getModulus()));
    inputMaskIO = new HashMap<>();

    authElems = new ArrayDeque<>();
    bitElems = new ArrayDeque<>();
    triples = new ArrayDeque<>();
    this.masks = new HashMap<>();
    for (int partyId = 1; partyId <= settings.getNoPlayers(); partyId++) {
      masks.put(partyId, new ArrayDeque<>());
      inputMaskIO.put(partyId, new ElementIO<>(fileDir + INPUT_SUFFIX + "-" + partyId,
          new InputMaskSerializer(settings.getModulus())));
    }


  }

  public static SpdzMascotFileSupplier createSimpleSypplier(String fileDir) {
    return new SpdzMascotFileSupplier(fileDir, 16);
  }

  @Override
  public SpdzSInt getNextRandomFieldElement() {
    if (authElems.isEmpty()) {
      logger.trace("Getting another random element batch");
      List<AuthenticatedElement> newElements = authElemIO.readData(batchSize);
      authElems.addAll(newElements);
      logger.trace("Got another random element batch");
    }
    return MascotFormatConverter.toSpdzSInt(authElems.pop());
  }

  @Override
  public SpdzSInt getNextBit() {
    if (bitElems.isEmpty()) {
      logger.trace("Getting another bit batch");
      List<AuthenticatedElement> newElements = authElemIO.readData(batchSize);
      bitElems.addAll(newElements);
      logger.trace("Got another bit batch");
    }
    return MascotFormatConverter.toSpdzSInt(bitElems.pop());
  }


  @Override
  public SpdzTriple getNextTriple() {
    if (triples.isEmpty()) {
      logger.trace("Getting another triple batch");
      List<MultiplicationTriple> newTriples = tripleIO.readData(batchSize);
      triples.addAll(newTriples);
      logger.trace("Got another triple batch");
    }
    MultiplicationTriple triple = triples.pop();
    return MascotFormatConverter.toSpdzTriple(triple);
  }

  @Override
  public SpdzInputMask getNextInputMask(int towardPlayerId) {
    ArrayDeque<InputMask> inputMasks = masks.get(towardPlayerId);
    if (inputMasks.isEmpty()) {
      logger.trace("Getting another triple batch");
      List<InputMask> newMasks = inputMaskIO.get(towardPlayerId).readData(batchSize);
      inputMasks.addAll(newMasks);
      logger.trace("Got another triple batch");
    }
    return MascotFormatConverter.toSpdzInputMask(inputMasks.pop());
  }

  @Override
  public SpdzSInt[] getNextExpPipe() {
    throw new NotImplementedException();
  }

  @Override
  public BigInteger getModulus() {
    return this.modulus;
  }

  @Override
  public BigInteger getSecretSharedKey() {
    return this.macShare.toBigInteger();
  }

}
