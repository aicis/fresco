package dk.alexandra.fresco.suite.spdz.storage;

import static dk.alexandra.fresco.suite.spdz.storage.SpdzMascotFileSupplier.AUTH_ELEM_SUFFIX;
import static dk.alexandra.fresco.suite.spdz.storage.SpdzMascotFileSupplier.BIT_SUFFIX;
import static dk.alexandra.fresco.suite.spdz.storage.SpdzMascotFileSupplier.INPUT_SUFFIX;
import static dk.alexandra.fresco.suite.spdz.storage.SpdzMascotFileSupplier.TRIPLE_SUFFIX;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.mascot.Mascot;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class MascotFilePreprocess {

  private static final Logger logger = LoggerFactory.getLogger(MascotFilePreprocess.class);
  private final Mascot mascot;
  private final int noPlayers;
  private final ElementIO<AuthenticatedElement> authElemIO;
  private final ElementIO<AuthenticatedElement> bitElemIO;
  private final Map<Integer, ElementIO<InputMask>> inputMaskIO;
  private final ElementIO<MultiplicationTriple> tripleIO;

  public MascotFilePreprocess(String prefixDir, MascotResourcePool resourcePool, BigInteger modulus,
      FieldElement macShare, Supplier<Network> network) {
    noPlayers = resourcePool.getNoOfParties();
    SettingsIO<MascotSettings> settingsIO = new SettingsIO<>();
    MascotSettings settings = new MascotSettings(resourcePool.getNoOfParties(), modulus, macShare);
    settingsIO.writeFile(prefixDir, settings);
    mascot = new Mascot(resourcePool, network.get(), macShare);
    authElemIO = new ElementIO<>(
        prefixDir + AUTH_ELEM_SUFFIX, new AuthenticatedElementSerializer(modulus));
    bitElemIO = new ElementIO<>(
        prefixDir + BIT_SUFFIX, new AuthenticatedElementSerializer(modulus));
    tripleIO = new ElementIO<>(
        prefixDir + TRIPLE_SUFFIX, new MultiplicationTripleSerializer(modulus));
    inputMaskIO = new HashMap<>();
    for (int partyId = 1; partyId <= settings.getNoPlayers(); partyId++) {
      inputMaskIO.put(partyId, new ElementIO<>(prefixDir + INPUT_SUFFIX + "-" + partyId,
          new InputMaskSerializer(settings.getModulus())));
    }

  }

  public static MascotFilePreprocess extendPreprocessing(String fileDir) {
    throw new NotImplementedException();
  }

  public void close() {
    for (int partyId = 1; partyId <= noPlayers; partyId++) {
      inputMaskIO.get(partyId).close();
    }
    authElemIO.close();
    bitElemIO.close();
    tripleIO.close();
  }

  public void preprocess(int inputs, int randomElements, int bits, int triples) {
    for (int partyId = 1; partyId <= noPlayers; partyId++) {
      processInputs(partyId, inputs);
    }
    processRandomElements(randomElements);
    processBits(bits);
    processTriples(triples);
  }

  public void processInputs(int towardsPartyId, int amount) {
    logger.trace("Constructing " + amount + " input masks");
    List<InputMask> newMasks = mascot.getInputMasks(towardsPartyId, amount);
    inputMaskIO.get(towardsPartyId).writeData(newMasks);
    logger.trace("Done constructing input masks");
  }

  public void processRandomElements(int amount) {
    logger.trace("Constructing " + amount + " random elements");
    List<AuthenticatedElement> newElements = mascot.getRandomElements(amount);
    authElemIO.writeData(newElements);
    logger.trace("Done constructing random elements");
  }

  public void processBits(int amount) {
    logger.trace("Constructing " + amount + " random bits");
    List<AuthenticatedElement> newElements = mascot.getRandomBits(amount);
    bitElemIO.writeData(newElements);
    logger.trace("Done constructing random bits");
  }

  public void processTriples(int amount) {
    logger.trace("Constructing " + amount + " triples");
    List<AuthenticatedElement> newElements = mascot.getRandomBits(amount);
    bitElemIO.writeData(newElements);
    logger.trace("Done constructing triples");
  }
}
