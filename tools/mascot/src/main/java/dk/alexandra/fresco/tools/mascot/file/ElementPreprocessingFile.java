package dk.alexandra.fresco.tools.mascot.file;

import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.mascot.MascotResourcePool;
import dk.alexandra.fresco.tools.mascot.field.AuthenticatedElement;
import dk.alexandra.fresco.tools.mascot.field.FieldElement;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ElementPreprocessingFile
    implements Serializable {
  private final int noPlayer;
  private final int modBitLength;
  private final int statisticalSecurity;
  private final BigInteger modulus;
  private final FieldElement macKeyShare;
  private final StrictBitVector jointSeed;
  private final int firstUnusedIndex = 0;
  // TODO maybe a linked list is better
  private final List<AuthenticatedElement> elements = new ArrayList<>();

  public ElementPreprocessingFile(MascotResourcePool resources, FieldElement macKeyShare,
                                  StrictBitVector jointSeed) {
    this.noPlayer = resources.getNoOfParties();
    this.modBitLength = resources.getModBitLength();
    this.statisticalSecurity = resources.getLambdaSecurityParam();
    this.modulus = resources.getModulus();
    this.macKeyShare = macKeyShare;
    this.jointSeed = jointSeed;
  }

  public List<AuthenticatedElement> getElements() {
    return elements;
  }

  public void appendElements(List<AuthenticatedElement> newElements) {
    elements.addAll(newElements);
  }

  public BigInteger getModulus() {
    return modulus;
  }

  public int getNoPlayers() {
    return noPlayer;
  }

  public int getFirstUnusedIndex() {
    return firstUnusedIndex;
  }

  public boolean isCompatible(ElementPreprocessingFile otherFile) {
    return noPlayer == otherFile.noPlayer && modBitLength == otherFile.modBitLength &&
        statisticalSecurity == otherFile.statisticalSecurity &&
        modulus.equals(otherFile.modulus) && macKeyShare.equals(otherFile.macKeyShare) &&
        jointSeed.equals(otherFile.jointSeed);
  }

  /**
   * Return size in bytes needed to represent an authenticated element.
   * This is the mac share plus value share. Each of these are max the value of the modulus minus 1.
   *
   * @return
   */
  public int getElementSize() {
    return 2 * (modBitLength / 8);
  }

  public byte[] serialize(AuthenticatedElement obj) throws IOException {
    byte[] arr = new byte[getElementSize()];
    System.arraycopy(obj.getShare().toByteArray(), 0, arr, 0, modBitLength / 8);
    System.arraycopy(obj.getMac().toByteArray(), 0, arr, modBitLength / 8, modBitLength / 8);
    return arr;
  }

  public AuthenticatedElement deserialize(byte[] byteArray) {
    byte[] byteShare = Arrays.copyOfRange(byteArray, 0, modBitLength / 8);
    byte[] byteMac = Arrays.copyOfRange(byteArray, modBitLength / 8, byteArray.length);
    FieldElement share = new FieldElement(byteShare, modulus);
    FieldElement mac = new FieldElement(byteMac, modulus);
    AuthenticatedElement res = new AuthenticatedElement(share, mac, modulus);
    return res;
  }
}
