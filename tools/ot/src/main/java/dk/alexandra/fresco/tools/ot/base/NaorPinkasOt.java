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
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.spec.DHParameterSpec;

/**
 * Implementation of the Naor-Pinkas OT.
 */
public class NaorPinkasOt implements Ot {
  private final int myId;
  private final int otherId;
  private final Network network;
  private final Drng randNum;
  private final Drbg randBit;
  private final MessageDigest hashDigest;
  private static final String HASH_ALGORITHM = "SHA-256";
  /**
   * The PRG algorithm used internally by Java to generate the Diffie-Hellman
   * parameters based on some seed. This PRG MUST only be used for this purpose
   * since SHA1 is insecure in the general case.
   */
  private static final String PRG_ALGORITHM = "SHA1PRNG";
  private static final int DIFFIE_HELLMAN_SIZE = 2048;
  private DHParameterSpec dhParams;

  /**
   * Constructs a Naor-Pinkas OT instance by interactively and securely sampling
   * the underlying Diffie-Hellman parameters.
   *
   * @param myId
   *          The ID of the calling party
   * @param otherId
   *          The ID of the other party
   * @param rand
   *          The calling party's secure randomness generator
   * @param network
   *          The underlying network to use
   */
  public NaorPinkasOt(int myId, int otherId, Drbg rand, Network network) {
    this(myId, otherId, rand, network, null);
    computeSecureDhParams();
  }

  /**
   * Constructs a Naor-Pinkas OT instance using prespecified Diffie-Hellman
   * parameters.
   *
   * @param myId
   *          The ID of the calling party
   * @param otherId
   *          The ID of the other party
   * @param randBit
   *          The calling party's secure randomness generator
   * @param network
   *          The underlying network to use
   * @param params
   *          The Diffie-Hellman parameters to use
   */
  public NaorPinkasOt(int myId, int otherId, Drbg randBit, Network network,
      DHParameterSpec params) {
    this.myId = myId;
    this.otherId = otherId;
    this.randBit = randBit;
    this.network = network;
    this.hashDigest = ExceptionConverter.safe(() -> MessageDigest.getInstance(
        HASH_ALGORITHM),
        "Missing secure, hash function which is dependent in this library");
    this.dhParams = params;
    this.randNum = new DrngImpl(randBit);
  }

  @Override
  public void send(StrictBitVector messageZero, StrictBitVector messageOne) {
    // To avoid leakage both messages sent will have the length of the longest
    // of the two
    int maxBitLength = Math.max(messageZero.getSize(), messageOne.getSize());
    Pair<byte[], byte[]> seedMessages = sendBytesOt();
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
    byte[] seed = receiveByteOt(choiceBit);
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
  private byte[] padMessage(byte[] message, int maxSize,
      byte[] seed) {
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
   * Completes the sender's part of the Naor-Pinkas OT.
   *
   * @return The two random messages sent by the sender.
   */
  private Pair<byte[], byte[]> sendBytesOt() {
    BigInteger c = randNum.nextBigInteger(dhParams.getP());
    network.send(otherId, c.toByteArray());
    BigInteger publicKeyZero = new BigInteger(network.receive(otherId));
    // publicKeyOne = c / publicKeyZero
    BigInteger publicKeyOne = publicKeyZero.modInverse(dhParams.getP())
        .multiply(c);
    byte[] messageZero = new byte[hashDigest.getDigestLength()];
    byte[] messageOne = new byte[hashDigest.getDigestLength()];
    BigInteger encZero = encryptMessage(publicKeyZero, messageZero);
    BigInteger encOne = encryptMessage(publicKeyOne, messageOne);
    network.send(otherId, encZero.toByteArray());
    network.send(otherId, encOne.toByteArray());
    return new Pair<>(messageZero, messageOne);
  }

  /**
   * Completes the receiver's part of the Naor-Pinkas OT.
   *
   * @return The message received
   */
  private byte[] receiveByteOt(Boolean choiceBit) {
    BigInteger c = new BigInteger(network.receive(otherId));
    // Pick random element privateKey
    BigInteger privateKey = randNum.nextBigInteger(dhParams.getP());
    // publicKeySigma = G^privateKey mod P
    BigInteger publicKeySigma = dhParams.getG().modPow(privateKey, dhParams
        .getP());
    // publicKeyNotSigma = c / publicKeySigma mod P
    BigInteger publicKeyNotSigma = publicKeySigma.modInverse(dhParams.getP())
        .multiply(c);
    if (choiceBit == false) {
      network.send(otherId, publicKeySigma.toByteArray());
    } else {
      network.send(otherId, publicKeyNotSigma.toByteArray());
    }
    BigInteger encZero = new BigInteger(network.receive(otherId));
    BigInteger encOne = new BigInteger(network.receive(otherId));
    byte[] message;
    if (choiceBit == false) {
      message = decryptMessage(encZero, privateKey);
    } else {
      message = decryptMessage(encOne, privateKey);
    }
    return message;
  }

  /**
   * Completes the internal Naor-Pinkas encryption.
   *
   * @param publicKey
   *          The public key to encrypt with
   * @param message
   *          The message to encrypt
   * @return The ciphertext
   */
  private BigInteger encryptMessage(BigInteger publicKey,
      byte[] message) {
    // Pick random element r
    BigInteger r = randNum.nextBigInteger(dhParams.getP());
    // Compute encryption:
    // encryption = g^r mod P
    BigInteger encryption = dhParams.getG().modPow(r, dhParams.getP());
    // toHash = publicKey^r mod P
    BigInteger toHash = publicKey.modPow(r, dhParams.getP());
    // randomMessage = H(toHash)
    byte[] encB = hashDigest.digest(toHash.toByteArray());
    System.arraycopy(encB, 0, message, 0, hashDigest.getDigestLength());
    return encryption;
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
  private byte[] decryptMessage(BigInteger cipher, BigInteger privateKey) {
    BigInteger toHash = cipher.modPow(privateKey, dhParams.getP());
    return hashDigest.digest(toHash.toByteArray());
  }

  /**
   * Agree on Diffie-Hellman parameters using coin-tossing. The parameters are
   * stored internally and used to compute the OT.
   */
  private void computeSecureDhParams() {
    // Do coin-tossing to agree on a random seed of "kbitLength" bits
    CoinTossing ct = new CoinTossing(myId, otherId, randBit);
    ct.initialize(network);
    StrictBitVector seed = ct.toss(hashDigest.getDigestLength() * 8);
    computeDhParams(seed.toByteArray());
  }

  /**
   * Compute and set Diffie-Hellman parameters based on a seed.
   *
   * @param seed
   *          The seed used to sample the Diffie-Hellman parameters
   */
  private void computeDhParams(byte[] seed) {
    // Make a parameter generator for Diffie-Hellman parameters
    AlgorithmParameterGenerator paramGen = ExceptionConverter.safe(
        () -> AlgorithmParameterGenerator.getInstance("DH"),
        "Missing Java internal parameter generator which is needed in this library");
    SecureRandom commonRand = ExceptionConverter.safe(() -> SecureRandom
        .getInstance(PRG_ALGORITHM),
        "Missing Java internal PRG which is needed in this library");
    commonRand.setSeed(seed);
    // Construct DH parameters of a 2048 bit group based on the common seed
    paramGen.init(DIFFIE_HELLMAN_SIZE, commonRand);
    AlgorithmParameters params = paramGen.generateParameters();
    ExceptionConverter.safe(() -> this.dhParams = params.getParameterSpec(
        DHParameterSpec.class),
        "Missing Java internal parameter generator which is needed in this library");
  }
}
