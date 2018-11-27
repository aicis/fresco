package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.MultiplicationTriple;
import dk.alexandra.fresco.tools.mascot.file.AuthenticatedElementSerializer;
import dk.alexandra.fresco.tools.mascot.file.ElementIO;
import dk.alexandra.fresco.tools.mascot.file.MascotSettings;
import dk.alexandra.fresco.tools.mascot.file.MultiplicationTripleSerializer;
import dk.alexandra.fresco.tools.mascot.file.SettingsIO;
import java.math.BigInteger;

public class SpdzMascotFileSupplier implements SpdzDataSupplier {

  private final MascotSettings settings;
  private final ElementIO<AuthenticatedElementSerializer, AuthenticatedElement> authElemIO;
  private final ElementIO<MultiplicationTripleSerializer, MultiplicationTriple> tripleIO;

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
  public SpdzMascotFileSupplier(String fileDir) {
    SettingsIO<MascotSettings> settingsIO = new SettingsIO<>();
    settings = settingsIO.readFile(fileDir);

    authElemIO = new ElementIO<>(new AuthenticatedElementSerializer(settings.getModulus()));
    tripleIO = new ElementIO<>(new MultiplicationTripleSerializer(settings.getModulus()));
  }

  @Override
  public SpdzTriple getNextTriple() {
    logger.trace("Getting another triple batch");
    tripleIO
    triples.addAll(mascot.getTriples(batchSize));
    logger.trace("Got another triple batch");
  }

  @Override
  public SpdzSInt[] getNextExpPipe() {
    return new SpdzSInt[0];
  }

  @Override
  public SpdzInputMask getNextInputMask(int towardPlayerId) {
    return null;
  }

  @Override
  public SpdzSInt getNextBit() {
    return null;
  }

  @Override
  public BigInteger getModulus() {
    return null;
  }

  @Override
  public BigInteger getSecretSharedKey() {
    return null;
  }

  @Override
  public SpdzSInt getNextRandomFieldElement() {
    return null;
  }
}
