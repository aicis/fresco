package dk.alexandra.fresco.tools.ot.otextension;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.tools.ot.base.DummyOT;
import dk.alexandra.fresco.tools.ot.base.OT;

/**
 * Superclass containing the common variables and methods 
 * for the sender and receiver parties of correlated OT with errors 
 * @author jot2re
 *
 */
public class COTeShared {
  // Constructor arguments
  protected int otherID;
  protected int kBitLength;
  protected int lambdaSecurityParam;
  protected Random rand;
  protected Network network;
  // Internal state variables
  protected boolean initialized;
  protected OT<BigInteger> ot;

  public COTeShared(int otherID, int kBitLength, int lambdaSecurityParam, Random rand, 
      Network network) {
    super();
    if (kBitLength < 1 | lambdaSecurityParam < 1 | rand == null
        | network == null)
      throw new IllegalArgumentException("Illegal constructor parameters");
    if (kBitLength % 8 != 0)
      throw new IllegalArgumentException(
          "Computational security parameter must be divisible by 8");
    this.otherID = otherID;
    this.kBitLength = kBitLength;
    this.lambdaSecurityParam = lambdaSecurityParam;
    this.rand = rand;
    this.ot = new DummyOT(otherID, network);
    this.network = network;
  }

  public int getOtherID() {
    return otherID;
  }

  public void setOtherID(int otherID) {
    this.otherID = otherID;
  }

  public int getkBitLength() {
    return kBitLength;
  }

  public void setkBitLength(int kBitLength) {
    this.kBitLength = kBitLength;
  }

  public int getLambdaSecurityParam() {
    return lambdaSecurityParam;
  }

  public void setLambdaSecurityParam(int lambdaSecurityParam) {
    this.lambdaSecurityParam = lambdaSecurityParam;
  }

  public Random getRand() {
    return rand;
  }

  public void setRand(Random rand) {
    this.rand = rand;
  }

  public Network getNetwork() {
    return network;
  }

  public void setNetwork(Network network) {
    this.network = network;
  }

  /**
   * Returns the "bit" number bit, reading from left-to-right, from a byte array
   * 
   * @param input
   *          The arrays of which to retrieve a bit
   * @param bit
   *          The index of the bit, counting from 0
   * @return Returns the "bit" number bit, reading from left-to-right, from
   *         "input"
   */
  protected static boolean getBit(byte[] input, int bit) {
    if (bit < 0)
      throw new IllegalAccessError("Bit index must be 0 or positive.");
    // Get the byte with the "bit"'th bit, and shift it to the left-most
    // position of the byte
    byte currentByte = (byte) (input[bit / 8] >>> (7 - (bit % 8)));
    boolean choiceBit = false;
    if ((currentByte & 1) == 1)
      choiceBit = true;
    return choiceBit;
  }

  /**
   * Computes the XOR of each element in a list of byte arrays. This is done
   * in-place in "vector1". If the lists are not of equal length or any of the
   * byte arrays are not of equal size, then an IllegalArgument exception is
   * thrown
   * 
   * @param vector1
   *          First input list
   * @param vector2
   *          Second input list
   * @return A new list which is the XOR of the two input lists
   */
  protected static void xor(List<byte[]> vector1,
      List<byte[]> vector2) {
    if (vector1.size() != vector2.size())
      throw new IllegalArgumentException("The vectors are not of equal length");
    for (int i = 0; i < vector1.size(); i++) {
      xor(vector1.get(i), vector2.get(i));
    }
  }

  /**
   * Computes the XOR of each element in a byte array. This is done in-place in
   * "arr1". If the byte arrays are not of equal size, then an IllegalArgument
   * exception is thrown
   * 
   * @param arr1
   *          First byte array
   * @param arr2
   *          Second byte array
   * @return A new byte array which is the XOR of the two input arrays
   */
  protected static void xor(byte[] arr1, byte[] arr2) {
    int bytesNeeded = arr1.length;
    if (bytesNeeded != arr2.length)
      throw new IllegalArgumentException(
          "The byte arrays are not of equal length");
    for (int i = 0; i < bytesNeeded; i++) {
      // Compute the XOR (addition in GF2) of arr1 and arr2
      arr1[i] ^= arr2[i];
    }
  }

  /**
   * Sends a list of byte arrays to the default (0) channel
   * 
   * @param vector
   *          Vector to send
   * @return Returns true if the transmission was successful
   */
  protected boolean sendList(List<byte[]> vector) {
    try {
      for (byte[] currentArr : vector) {
        network.send(0, otherID, currentArr);
      }
    } catch (IOException e) {
      System.out.println("Broke while sending " + e);
      return false;
    }
    return true;
  }

  /**
   * Receives a list of byte arrays from the default (0) channel
   * 
   * @param vector
   *          Vector to receive
   * @param size
   *          Amount of elements in vector to receive
   * @return The list of received elements, or null in case an error occurred.
   */
  protected List<byte[]> receiveList(int size) {
    List<byte[]> vector = new ArrayList<>(size);
    try {
      for (int i = 0; i < size; i++) {
        byte[] currentArr = network.receive(0, otherID);
        vector.add(currentArr);
      }
    } catch (IOException e) {
      System.out.println("Broke while sending " + e);
      return null;
    }
    return vector;
  }
}
