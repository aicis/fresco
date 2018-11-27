package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import dk.alexandra.fresco.tools.mascot.file.MascotSettings;
import dk.alexandra.fresco.tools.ot.otextension.RotList;
import java.math.BigInteger;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class SpdzMascotFileSupplier implements SpdzDataSupplier {

  private final MascotSettings meta;

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
  public SpdzMascotFileSupplier(int myId, int numberOfPlayers, int instanceId,
      Supplier<Network> tripleNetwork, BigInteger modulus, int modBitLength,
      Function<Integer, SpdzSInt[]> preprocessedValues, int prgSeedLength, int batchSize,
      FieldElement ssk, Map<Integer, RotList> seedOts, Drbg drbg) {

  @Override
  public SpdzTriple getNextTriple() {
    return null;
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
