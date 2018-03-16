package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.otextension.PseudoOtp;
import java.math.BigInteger;
import java.security.MessageDigest;
import javax.crypto.spec.DHParameterSpec;

/**
 * Implementation of the Naor-Pinkas OT.
 */
public class NaorPinkasOt implements Ot {
  private static final String HASH_ALGORITHM = "SHA-256";
  private final int otherId;
  private final Network network;
  private final Drng randNum;
  private final MessageDigest hashDigest;
  /**
   * The modulus of the Diffie-Hellman group used in the OT.
   */
  private final BigInteger dhModulus;
  /**
   * The generator of the Diffie-Hellman group used in the OT.
   */
  private final BigInteger dhGenerator;

  /**
   * Constructs a Naor-Pinkas OT instance using prespecified Diffie-Hellman parameters.
   *
   * @param otherId The ID of the other party
   * @param randBit The calling party's secure randomness generator
   * @param network The underlying network to use
   * @param params The Diffie-Hellman parameters to use
   */
  public NaorPinkasOt(int otherId, Drbg randBit, Network network, DHParameterSpec params) {
    this.otherId = otherId;
    this.network = network;
    this.hashDigest = ExceptionConverter.safe(() -> MessageDigest.getInstance(HASH_ALGORITHM),
        "Missing secure, hash function which is dependent in this library");
    this.dhModulus = params.getP();
    this.dhGenerator = params.getG();
    this.randNum = new DrngImpl(randBit);
  }

  @Override
  public void send(StrictBitVector messageZero, StrictBitVector messageOne) {
    int maxBitLength = Math.max(messageZero.getSize(), messageOne.getSize());
    Pair<byte[], byte[]> seedMessages = sendRandomOt();
    byte[] encryptedZeroMessage = PseudoOtp.encrypt(messageZero.toByteArray(),
        seedMessages.getFirst(), maxBitLength / Byte.SIZE);
    byte[] encryptedOneMessage = PseudoOtp.encrypt(messageOne.toByteArray(),
        seedMessages.getSecond(), maxBitLength / Byte.SIZE);
    network.send(otherId, encryptedZeroMessage);
    network.send(otherId, encryptedOneMessage);
  }

  @Override
  public StrictBitVector receive(boolean choiceBit) {
    byte[] seed = receiveRandomOt(choiceBit);
    byte[] encryptedZeroMessage = network.receive(otherId);
    byte[] encryptedOneMessage = network.receive(otherId);
    return recoverTrueMessage(encryptedZeroMessage, encryptedOneMessage, seed, choiceBit);
  }

  /**
   * Receive one-time padded OT messages and remove the pad of the one of the messages chosen in the
   * OT.
   *
   * @param encryptedZeroMessage The one-time padded zero-message
   * @param encryptedOneMessage the one-time padded one-message
   * @param seed The seed used for padding of one of the messages
   * @param choiceBit A bit indicating which message the seed matches. False implies message zero
   *        and true message one.
   * @return The unpadded message as a StrictBitVector
   */
  private StrictBitVector recoverTrueMessage(byte[] encryptedZeroMessage,
      byte[] encryptedOneMessage, byte[] seed, boolean choiceBit) {
    if (encryptedZeroMessage.length != encryptedOneMessage.length) {
      throw new MaliciousException("The length of the two choice messages is not equal");
    }
    byte[] unpaddedMessage;
    if (choiceBit == false) {
      unpaddedMessage = PseudoOtp.decrypt(encryptedZeroMessage, seed);
    } else {
      unpaddedMessage = PseudoOtp.decrypt(encryptedOneMessage, seed);
    }
    return new StrictBitVector(unpaddedMessage);
  }

  /**
   * Completes the sender's part of the Naor-Pinkas OT in order to send two random messages of the
   * length of hash digest.
   *
   * @return The two random messages sent by the sender.
   */
  private Pair<byte[], byte[]> sendRandomOt() {
    BigInteger c = randNum.nextBigInteger(dhModulus);
    network.send(otherId, c.toByteArray());
    BigInteger publicKeyZero = new BigInteger(network.receive(otherId));
    BigInteger publicKeyOne = publicKeyZero.modInverse(dhModulus).multiply(c);
    Pair<BigInteger, byte[]> zeroChoiceData = encryptRandomMessage(publicKeyZero);
    Pair<BigInteger, byte[]> oneChoiceData = encryptRandomMessage(publicKeyOne);
    network.send(otherId, zeroChoiceData.getFirst().toByteArray());
    network.send(otherId, oneChoiceData.getFirst().toByteArray());
    return new Pair<>(zeroChoiceData.getSecond(), oneChoiceData.getSecond());
  }

  /**
   * Completes the receiver's part of the Naor-Pinkas OT in order to receive a random message of the
   * length of hash digest.
   *
   * @return The random message received
   */
  private byte[] receiveRandomOt(boolean choiceBit) {
    BigInteger c = new BigInteger(network.receive(otherId));
    BigInteger privateKey = randNum.nextBigInteger(dhModulus);
    BigInteger publicKeySigma = dhGenerator.modPow(privateKey, dhModulus);
    BigInteger publicKeyNotSigma = publicKeySigma.modInverse(dhModulus).multiply(c);
    if (choiceBit == false) {
      network.send(otherId, publicKeySigma.toByteArray());
    } else {
      network.send(otherId, publicKeyNotSigma.toByteArray());
    }
    BigInteger encZero = new BigInteger(network.receive(otherId));
    BigInteger encOne = new BigInteger(network.receive(otherId));
    byte[] message;
    if (choiceBit == false) {
      message = decryptRandomMessage(encZero, privateKey);
    } else {
      message = decryptRandomMessage(encOne, privateKey);
    }
    return message;
  }

  /**
   * Completes the internal Naor-Pinkas encryption.
   * <p>
   * Given a "public key" as input this method constructs an encryption of a random message. Both
   * the encryption and random message are returned.
   * </p>
   *
   * @param publicKey The public key to encrypt with
   * @return A pair where the first element is the ciphertext and the second element is the
   *         plaintext.
   */
  private Pair<BigInteger, byte[]> encryptRandomMessage(BigInteger publicKey) {
    BigInteger r = randNum.nextBigInteger(dhModulus);
    BigInteger cipherText = dhGenerator.modPow(r, dhModulus);
    BigInteger toHash = publicKey.modPow(r, dhModulus);
    byte[] message = hashDigest.digest(toHash.toByteArray());
    return new Pair<>(cipherText, message);
  }

  /**
   * Completes the internal Naor-Pinkas decryption.
   *
   * @param cipher The ciphertext to decrypt
   * @param privateKey The private key to use for decryption
   * @return The plain message
   */
  private byte[] decryptRandomMessage(BigInteger cipher, BigInteger privateKey) {
    BigInteger toHash = cipher.modPow(privateKey, dhModulus);
    return hashDigest.digest(toHash.toByteArray());
  }
}
