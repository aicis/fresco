package dk.alexandra.fresco.framework.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Random;

public class ByteArrayHelper {
  
  /**
   * Returns the "bit" number bit, reading from left-to-right, from a byte array.
   * 
   * @param input The arrays of which to retrieve a bit
   * @param bit The index of the bit, counting from 0
   * @return Returns the "bit" number bit, reading from left-to-right, from "input"
   */
  public static boolean getBit(byte[] input, int index) {
    if (index < 0) {
      throw new IllegalAccessError("Bit index must be 0 or positive.");
    }
    // Get the byte with the "bit"'th bit, and shift it to the left-most
    // position of the byte
    byte currentByte = (byte) (input[index / 8] >>> (7 - (index % 8)));
    boolean choiceBit = false;
    if ((currentByte & 1) == 1) {
      choiceBit = true;
    }
    return choiceBit;
  }

  public static void setBit(byte[] input, int index, boolean choice) {
    if (index < 0) {
      throw new IllegalAccessError("Bit index must be 0 or positive.");
    }
    if (choice == true) {
      // We read bits from left to right, hence the 7 - x.
      // Put a 1 in the correct position of a 
      // zero-byte and OR it into the correct byte to ensure that the position 
      // becomes 1 no matter whether it is currently set or not.
      input[index / 8] |= ((byte) 0x01) << (7 - (index % 8));
    } else {
      // Construct an all 1-byte, then construct a byte like above, where only 
      // the correct position is set to 1. We XOR these bytes to get a byte 
      // which is all 1's except in the correct position. We AND this into the 
      // correct byte to ensure that only the correct positions gets set to 0. 
      input[index / 8] &= 0xFF ^ ((byte) 0x01) << (7 - (index % 8));
    }
  }

  /**
   * Computes the XOR of each element in a list of byte arrays. This is done in-place in "vector1".
   * If the lists are not of equal length or any of the byte arrays are not of equal size, then an
   * IllegalArgument exception is thrown
   * 
   * @param vector1 First input list
   * @param vector2 Second input list
   * @return A new list which is the XOR of the two input lists
   */
  public static void xor(List<byte[]> vector1, List<byte[]> vector2) {
    if (vector1.size() != vector2.size()) {
      throw new IllegalArgumentException("The vectors are not of equal length");
    }
    for (int i = 0; i < vector1.size(); i++) {
      xor(vector1.get(i), vector2.get(i));
    }
  }

  /**
   * Computes the XOR of each element in a byte array. This is done in-place in "arr1". If the byte
   * arrays are not of equal size, then an IllegalArgument exception is thrown
   * 
   * @param arr1 First byte array
   * @param arr2 Second byte array
   * @return A new byte array which is the XOR of the two input arrays
   */
  public static void xor(byte[] arr1, byte[] arr2) {
    int bytesNeeded = arr1.length;
    if (bytesNeeded != arr2.length) {
      throw new IllegalArgumentException("The byte arrays are not of equal length");
    }
    for (int i = 0; i < bytesNeeded; i++) {
      // Compute the XOR (addition in GF2) of arr1 and arr2
      arr1[i] ^= arr2[i];
    }
  }

  public static void shiftArray(byte[] input, byte[] output, int positions) {
    for (int i = 0; i < input.length * 8; i++) {
      setBit(output, positions + i, getBit(input, i));
    }
  }

  /**
   * Serialize a serializable value
   * 
   * @param val
   *          The value to serialize
   * @return The serialized value
   * @throws IOException
   *           Thrown if an internal error occurs.
   */
  public static byte[] serialize(Serializable val) throws IOException {
    ByteArrayOutputStream bos = null;
    try {
      bos = new ByteArrayOutputStream();
      ObjectOutput out = new ObjectOutputStream(bos);
      out.writeObject(val);
      out.flush();
      return bos.toByteArray();
    } finally {
      bos.close();
    }
  }

  /**
   * Deserialize a serializable value
   * 
   * @param val
   *          The value to deserialize
   * @return The deserialized object
   * @throws IOException
   *           Thrown if an internal error occurs.
   * @throws ClassNotFoundException
   *           Thrown if an internal error occurs.
   */
  public static Serializable deserialize(byte[] val)
      throws IOException, ClassNotFoundException {
    ByteArrayInputStream bis = null;
    ObjectInput in = null;
    try {
      bis = new ByteArrayInputStream(val);
      in = new ObjectInputStream(bis);
      return (Serializable) in.readObject();
    } finally {
      bis.close();
      in.close();
    }
  }
  
  public static byte[] randomByteArray(int size, Random rand) {
    byte[] array = new byte[size];
    rand.nextBytes(array);
    return array;
  }
  
}
