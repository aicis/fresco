package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.List;

public interface BristolCryptoBuilder {

  /**
   * Computes the multiplication of two 32 bit numbers. The result will contain 64 bits.
   * 
   * @param in1 The first number
   * @param in2 The second number
   * @return A list of size 64 containing the result of in1*in2.
   */
  public Computation<List<SBool>> mult32x32(List<Computation<SBool>> in1,
      List<Computation<SBool>> in2);

  /**
   * Computes AES of the given plaintext using the given key.
   * 
   * @param plainText The secret shared plaintext. The plaintext must contain exactly 128 bits.
   * @param keyMaterial The secret shared key. The key must contain exactly 128 bits.
   * @return The AES function applied on the given plaintext using the given key material. The
   *         result will be exactly 128 bits long.
   */
  public Computation<List<SBool>> AES(List<Computation<SBool>> plainText,
      List<Computation<SBool>> keyMaterial);

  /**
   * Computes SHA-1 on the given input.
   * 
   * @param input The input for the SHA-1 function. Must contain exactly 512 bits.
   * @return The SHA-1(input) which contains 160 bits.
   */
  public Computation<List<SBool>> SHA1(List<Computation<SBool>> input);

  /**
   * Computes SHA-256 on the given input.
   * 
   * @param input The input for the function. Must be exactly 512 bits long.
   * @return SHA-256(input) which contains exactly 256 bits.
   */
  public Computation<List<SBool>> SHA256(List<Computation<SBool>> input);

  /**
   * Computes DES on the given plaintext using the given key material.
   * 
   * @param plaintext The secret shared plaintext. Must contain exactly 64 bits.
   * @param keyMaterial The secret shared key material. Must contain exactly 64 bits.
   * @return DES_keyMaterial(plainText) which contains exactly 64 bits.
   */
  public Computation<List<SBool>> DES(List<Computation<SBool>> plainText,
      List<Computation<SBool>> keyMaterial);

  /**
   * Computes the MD5 hash function on the given input.
   * 
   * @param input The input for the function. Must be exactly 512 bits long.
   * @return MD5(input) which contains exactly 128 bits.
   */
  public Computation<List<SBool>> MD5(List<Computation<SBool>> input);


}
