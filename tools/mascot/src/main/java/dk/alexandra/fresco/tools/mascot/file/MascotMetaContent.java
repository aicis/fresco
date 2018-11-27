package dk.alexandra.fresco.tools.mascot.file;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;
import java.io.Serializable;
import java.math.BigInteger;

public class MascotMetaContent implements  Serializable{
  private final int noPlayer;
  private final int modBitLength;
  private final int statisticalSecurity;
  private final BigInteger modulus;
  private final FieldElement macKeyShare;
  private final StrictBitVector jointSeed;

  public MascotMetaContent(MascotResourcePool resources, FieldElement macKeyShare,
                                  StrictBitVector jointSeed) {
    this.noPlayer = resources.getNoOfParties();
    this.modBitLength = resources.getModBitLength();
    this.statisticalSecurity = resources.getLambdaSecurityParam();
    this.modulus = resources.getModulus();
    this.macKeyShare = macKeyShare;
    this.jointSeed = jointSeed;
  }


  public BigInteger getModulus() {
    return modulus;
  }

  public int getNoPlayers() {
    return noPlayer;
  }

  public boolean isCompatible(MascotMetaContent otherFile) {
    return noPlayer == otherFile.noPlayer && modBitLength == otherFile.modBitLength &&
        statisticalSecurity == otherFile.statisticalSecurity &&
        modulus.equals(otherFile.modulus) && macKeyShare.equals(otherFile.macKeyShare) &&
        jointSeed.equals(otherFile.jointSeed);
  }


}
