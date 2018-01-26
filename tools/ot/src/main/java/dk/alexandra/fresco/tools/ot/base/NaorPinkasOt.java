package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
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
  private final BigInteger p;
  /**
   * The generator of the Diffie-Hellman group used in the OT.
   */
  private final BigInteger g;

  /**
   * Constructs a Naor-Pinkas OT instance using prespecified Diffie-Hellman
   * parameters.
   *
   * @param otherId
   *          The ID of the other party
   * @param randBit
   *          The calling party's secure randomness generator
   * @param network
   *          The underlying network to use
   * @param params
   *          The Diffie-Hellman parameters to use
   */
  public NaorPinkasOt(int otherId, Drbg randBit, Network network,
      DHParameterSpec params) {
    this.otherId = otherId;
    this.network = network;
    this.hashDigest = ExceptionConverter.safe(() -> MessageDigest.getInstance(
        HASH_ALGORITHM),
        "Missing secure, hash function which is dependent in this library");
    this.p = params.getP();
    this.g = params.getG();
    this.randNum = new DrngImpl(randBit);
  }

  @Override
  public void send(StrictBitVector messageZero, StrictBitVector messageOne) {
    // To avoid leakage both messages sent will have the length of the longest
    // of the two
    int maxBitLength = Math.max(messageZero.getSize(), messageOne.getSize());
    // Completes the actual Naor-Pinkas to send two random messages of the length
    // of the hash digest
    Pair<byte[], byte[]> seedMessages = sendRandomOt();
    // We divide the length with 8 to get the byte length and then use the short
    // messages from the OT as seeds for a PRG which does a one-time-pad
    // encryption of the true messages to send
    byte[] encryptedZeroMessage = padMessage(messageZero.toByteArray(),
        maxBitLength / 8, seedMessages.getFirst());
    byte[] encryptedOneMessage = padMessage(messageOne.toByteArray(),
        maxBitLength / 8, seedMessages.getSecond());
    // Send the padded messages
    network.send(otherId, encryptedZeroMessage);
    network.send(otherId, encryptedOneMessage);
  }

  @Override
  public StrictBitVector receive(boolean choiceBit) {
    byte[] seed = receiveRandomOt(choiceBit);
    byte[] encryptedZeroMessage = network.receive(otherId);
    byte[] encryptedOneMessage = network.receive(otherId);
    return recoverTrueMessage(encryptedZeroMessage, encryptedOneMessage, seed,
        choiceBit);
  }

  /**
   * Make a one-time padding of a {@code message} using a PRG seeded on
   * {@code seed}.
   *
   * @param message
   *          The message to one-time pad
   *
   * @param maxSize
   *          The size of the resultant ciphertext. MUST be at least the length
   *          of the message itself. If longer, it will be padded with zeros
   * @param seed
   *          The seed to use for a PRG
   * @return The one-time padded message
   */
  private byte[] padMessage(byte[] message, int maxSize, byte[] seed) {
    byte[] maxLengthMessage = Arrays.copyOf(message, maxSize);
    byte[] encryptedMessage = new byte[maxSize];
    Drbg prg = new AesCtrDrbg(seed);
    prg.nextBytes(encryptedMessage);
    ByteArrayHelper.xor(encryptedMessage, maxLengthMessage);
    return encryptedMessage;
  }

  /**
   * Decrypt a one-time padded {@code paddedMessage} using a PRG seeded on
   * {@code seed}.
   *
   * @param paddedMessage
   *          The message to one-time pad
   * @param seed
   *          The seed to use for a PRG
   * @return The plain message
   */
  private byte[] unpadMessage(byte[] paddedMessage, byte[] seed) {
    byte[] message = new byte[paddedMessage.length];
    Drbg prg = new AesCtrDrbg(seed);
    prg.nextBytes(message);
    ByteArrayHelper.xor(message, paddedMessage);
    return message;
  }

  /**
   * Receive one-time padded OT messages and remove the pad of the one of the
   * messages chosen in the OT.
   *
   * @param encryptedZeroMessage
   *          The one-time padded zero-message
   * @param encryptedOneMessage
   *          the one-time padded one-message
   * @param seed
   *          The seed used for padding of one of the messages
   * @param choiceBit
   *          A bit indicating which message the seed matches. False implies
   *          message zero and true message one.
   * @return The unpadded message as a StrictBitVector
   */
  private StrictBitVector recoverTrueMessage(byte[] encryptedZeroMessage,
      byte[] encryptedOneMessage, byte[] seed, boolean choiceBit) {
    if (encryptedZeroMessage.length != encryptedOneMessage.length) {
      throw new MaliciousException(
          "The length of the two choice messages is not equal");
    }
    byte[] unpaddedMessage;
    if (choiceBit == false) {
      unpaddedMessage = unpadMessage(encryptedZeroMessage, seed);
    } else {
      unpaddedMessage = unpadMessage(encryptedOneMessage, seed);
    }
    return new StrictBitVector(unpaddedMessage);
  }

  /**
   * Completes the sender's part of the Naor-Pinkas OT in order to send two
   * random messages of the length of hash digest.
   *
   * @return The two random messages sent by the sender.
   */
  private Pair<byte[], byte[]> sendRandomOt() {
    // Pick a random value c mod p
    BigInteger c = randNum.nextBigInteger(p);
    network.send(otherId, c.toByteArray());
    BigInteger publicKeyZero = new BigInteger(network.receive(otherId));
    // publicKeyOne = c / publicKeyZero mod p
    BigInteger publicKeyOne = publicKeyZero.modInverse(p).multiply(c);
    // Construct ciphertexts encrypting random messages
    Pair<BigInteger, byte[]> zeroChoiceData = encryptRandomMessage(publicKeyZero);
    Pair<BigInteger, byte[]> oneChoiceData = encryptRandomMessage(publicKeyOne);
    // Send the ciphertexts
    network.send(otherId, zeroChoiceData.getFirst().toByteArray());
    network.send(otherId, oneChoiceData.getFirst().toByteArray());
    // Return the random messages
    return new Pair<>(zeroChoiceData.getSecond(), oneChoiceData.getSecond());
  }

  /**
   * Completes the receiver's part of the Naor-Pinkas OT in order to receive a
   * random message of the length of hash digest.
   *
   * @return The random message received
   */
  private byte[] receiveRandomOt(boolean choiceBit) {
    BigInteger c = new BigInteger(network.receive(otherId));
    // Pick random element privateKey mod p
    BigInteger privateKey = randNum.nextBigInteger(p);
    BigInteger publicKeySigma = g.modPow(privateKey, p);
    // publicKeyNotSigma = c / publicKeySigma mod p
    BigInteger publicKeyNotSigma = publicKeySigma.modInverse(p).multiply(c);
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
   * Given a "public key" as input this method constructs an encryption of a
   * random message. Both the encryption and random message are returned.
   * </p>
   *
   * @param publicKey
   *          The public key to encrypt with
   * @return A pair where the first element is the ciphertext and the second
   *         element is the plaintext.
   */
  private Pair<BigInteger, byte[]> encryptRandomMessage(BigInteger publicKey) {
    // Pick random element r mod p
    BigInteger r = randNum.nextBigInteger(p);
    // Compute encryption:
    BigInteger cipherText = g.modPow(r, p);
    BigInteger toHash = publicKey.modPow(r, p);
    // message = H(toHash)
    byte[] message = hashDigest.digest(toHash.toByteArray());
    return new Pair<BigInteger, byte[]>(cipherText, message);
  }

  /**
   * Completes the internal Naor-Pinkas decryption.
   *
   * @param cipher
   *          The ciphertext to decrypt
   * @param privateKey
   *          The private key to use for decryption
   * @return The plain message
   */
  private byte[] decryptRandomMessage(BigInteger cipher, BigInteger privateKey) {
    BigInteger toHash = cipher.modPow(privateKey, p);
    return hashDigest.digest(toHash.toByteArray());
  }
}
