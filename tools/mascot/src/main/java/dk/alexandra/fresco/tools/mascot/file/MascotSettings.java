package dk.alexandra.fresco.tools.mascot.file;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.math.BigInteger;

public class MascotSettings implements Settings {

  private final int noPlayer;
  private final int modBitLength;
  //  private final int statisticalSecurity;
  private final BigInteger modulus;
  private final FieldElement macKeyShare;
  private final StrictBitVector jointSeed;

  public MascotSettings(int noOfPlayers, BigInteger modulus, int modBitLength,
      FieldElement macKeyShare, StrictBitVector jointSeed) {
    this.noPlayer = noOfPlayers;
    this.modulus = modulus;
    this.modBitLength = modBitLength;
//    this.statisticalSecurity = lambdaSecurityParam;
    this.macKeyShare = macKeyShare;
    this.jointSeed = jointSeed;
  }

  public BigInteger getModulus() {
    return modulus;
  }

  @Override
  public int getNoPlayers() {
    return noPlayer;
  }

  @Override
  public boolean isCompatible(Settings otherSettings) {
    if (otherSettings.getClass() != MascotSettings.class) {
      return false;
    }
    MascotSettings otherMascot = (MascotSettings) otherSettings;
    return noPlayer == otherMascot.getNoPlayers() && modBitLength == otherMascot.modBitLength &&
//        statisticalSecurity == otherMascot.statisticalSecurity &&
        modulus.equals(otherMascot.modulus) && macKeyShare.equals(otherMascot.macKeyShare) &&
        jointSeed.equals(otherMascot.jointSeed);
  }

}
