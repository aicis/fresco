package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import java.util.Arrays;

public final class PseudoOtpImpl implements PseudoOtp {

  public PseudoOtpImpl() {
  }

  @Override
  public byte[] encrypt(byte[] message, byte[] keyCandidate) {
    return encrypt(message, keyCandidate, message.length);
  }

  @Override
  public byte[] encrypt(byte[] message, byte[] keyCandidate, int messageLength) {
    byte[] cipherText = LengthAdjustment.adjust(keyCandidate, messageLength);
    ByteArrayHelper.xor(cipherText, Arrays.copyOf(message, messageLength));
    return cipherText;
  }

  @Override
  public byte[] decrypt(byte[] cipherText, byte[] keyCandidate) {
    return decrypt(cipherText, keyCandidate, cipherText.length);
  }

  @Override
  public byte[] decrypt(byte[] cipherText, byte[] keyCandidate, int messageLength) {
    byte[] key = LengthAdjustment.adjust(keyCandidate, messageLength);
    ByteArrayHelper.xor(key, Arrays.copyOf(cipherText, messageLength));
    return key;
  }

}
