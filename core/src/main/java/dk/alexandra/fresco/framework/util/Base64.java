package dk.alexandra.fresco.framework.util;

/**
 * Everyone using base64 encoding within FRESCO should use this class to ensure compatibility with
 * the rest of FRESCO. Currently the underlying encoder/decoder used is java's own Base64 class
 * found within java.util.Base64.
 *
 */
public class Base64 {

  public static byte[] encode(byte[] bytesToEncode) {
    return java.util.Base64.getEncoder().encode(bytesToEncode);
  }

  public static byte[] decode(byte[] bytesToDecode) {
    return java.util.Base64.getDecoder().decode(bytesToDecode);
  }

  public static byte[] decodeFromString(String base64EncodedString) {
    return java.util.Base64.getDecoder().decode(base64EncodedString);
  }

  public static String encodeToString(byte[] bytesToEncode) {
    return java.util.Base64.getEncoder().encodeToString(bytesToEncode);
  }

}
