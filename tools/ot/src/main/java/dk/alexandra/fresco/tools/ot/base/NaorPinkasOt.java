package dk.alexandra.fresco.tools.ot.base;

import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;
import java.util.Arrays;

import javax.crypto.spec.DHParameterSpec;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.MaliciousException;
import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;

public class NaorPinkasOt implements Ot
{
  private int myId;
  private int otherId;
  private Network network;
  private Drng randNum;
  private Drbg randBit;
  private MessageDigest hashFunction;
  private final String hashAlgorithm;
  private final String prgAlgorithm;
  // The public Diffie-Hellman parameters
  private final int diffieHellmanSize;
  private DHParameterSpec params;

  public NaorPinkasOt(int myId, int otherId, Drbg rand, Network network)
      throws NoSuchAlgorithmException {
    this(myId, otherId, rand, network, null);
    setDhParams(computeSecureDhParams());
  }

  public NaorPinkasOt(int myId, int otherId, Drbg randBit, Network network,
      DHParameterSpec params) throws NoSuchAlgorithmException {
    this.hashAlgorithm = "SHA-256";
    // This MUST only be used for the generation of the DH parameters. Where it
    // is not insecure to use SHA1 as the seed has been picked through secure
    // coin-tossing. We cannot use our own implementation, since Java requires a
    // SecureRandom.
    this.prgAlgorithm = "SHA1PRNG";
    this.diffieHellmanSize = 2048;
    this.myId = myId;
    this.otherId = otherId;
    this.randBit = randBit;
    this.network = network;
    this.hashFunction = MessageDigest.getInstance(hashAlgorithm);
    this.params = params;
    this.randNum = new DrngImpl(randBit);
  }

  public DHParameterSpec getDhParams() {
    return params;
  }

  public void setDhParams(DHParameterSpec newParams) {
    this.params = newParams;
  }

  @Override
  public void send(StrictBitVector messageZero, StrictBitVector messageOne) {
    int maxBitLength = Math.max(messageZero.getSize(), messageOne.getSize());
    Pair<byte[], byte[]> seedMessages = sendBytesOt();
    // We divide the length with 8 to get the byte length
    byte[] encryptedZeroMessage = padMessage(messageZero.toByteArray(),
        maxBitLength / 8, seedMessages.getFirst());
    byte[] encryptedOneMessage = padMessage(messageOne.toByteArray(),
        maxBitLength / 8, seedMessages.getSecond());
    network.send(otherId, encryptedZeroMessage);
    network.send(otherId, encryptedOneMessage);
  }

  protected byte[] padMessage(byte[] message, int maxSize,
      byte[] seed) {
    byte[] maxLengthMessage = Arrays.copyOf(message, maxSize);
    byte[] encryptedMessage = new byte[maxSize];
    Drbg prg = new AesCtrDrbg(seed);
    prg.nextBytes(encryptedMessage);
    ByteArrayHelper.xor(encryptedMessage, maxLengthMessage);
    return encryptedMessage;
  }

  protected byte[] unpadMessage(byte[] paddedMessage, byte[] seed) {
    byte[] message = new byte[paddedMessage.length];
    Drbg prg = new AesCtrDrbg(seed);
    prg.nextBytes(message);
    ByteArrayHelper.xor(message, paddedMessage);
    return message;
  }

  @Override
  public StrictBitVector receive(Boolean choiceBit) {
    byte[] seed = receiveByteOt(choiceBit);
    byte[] encryptedZeroMessage = network.receive(otherId);
    byte[] encryptedOneMessage = network.receive(otherId);
    return recoverTrueMessage(encryptedZeroMessage, encryptedOneMessage, seed,
        choiceBit);
  }

  protected StrictBitVector recoverTrueMessage(byte[] encryptedZeroMessage,
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
    return new StrictBitVector(unpaddedMessage, 8 * unpaddedMessage.length);
  }

  private Pair<byte[], byte[]> sendBytesOt() {
    // Pick random element c
    BigInteger c = randNum.nextBigInteger(params.getP());
    network.send(otherId, c.toByteArray());
    BigInteger publicKeyZero = new BigInteger(network.receive(otherId));
    // keyOne = c / keyZero
    BigInteger publicKeyOne = publicKeyZero.modInverse(params.getP())
        .multiply(c);
    byte[] messageZero = new byte[hashFunction.getDigestLength()];
    byte[] messageOne = new byte[hashFunction.getDigestLength()];
    BigInteger encZero = encryptMessage(publicKeyZero, messageZero);
    BigInteger encOne = encryptMessage(publicKeyOne, messageOne);
    network.send(otherId, encZero.toByteArray());
    network.send(otherId, encOne.toByteArray());
    return new Pair<byte[], byte[]>(messageZero, messageOne);
  }

  private byte[] receiveByteOt(Boolean choiceBit) {
    BigInteger c = new BigInteger(network.receive(otherId));
    // Pick random element privateKey
    BigInteger privateKey = randNum.nextBigInteger(params.getP());
    // publicKeySigma = G^privateKey mod P
    BigInteger publicKeySigma = params.getG().modPow(privateKey, params.getP());
    // publicKeyNotSigma = c / publicKeySigma mod P
    BigInteger publicKeyNotSigma = publicKeySigma.modInverse(params.getP())
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

  protected BigInteger encryptMessage(BigInteger publicKey,
      byte[] message) {
    // Pick random element r
    BigInteger r = randNum.nextBigInteger(params.getP());
    // Compute encryption:
    // encryption = g^r mod P
    BigInteger encryption = params.getG().modPow(r, params.getP());
    // toHash = publicKey^r mod P
    BigInteger toHash = publicKey.modPow(r, params.getP());
    // randomMessage = H(toHash)
    byte[] encB = hashFunction.digest(toHash.toByteArray());
    for (int i = 0; i < hashFunction.getDigestLength(); i++) {
      message[i] = encB[i];
    }
    return encryption;
  }

  protected byte[] decryptMessage(BigInteger cipher, BigInteger privateKey) {
    BigInteger toHash = cipher.modPow(privateKey, params.getP());
    return hashFunction.digest(toHash.toByteArray());
  }

  /**
   * Agree on Diffie-Hellman parameters using coin-tossing. The parameters are
   * stored internally and used to compute the OT.
   * 
   * @return The computed Diffie-Hellman parameters
   */
  private DHParameterSpec computeSecureDhParams() {
    // Do coin-tossing to agree on a random seed of "kbitLength" bits
    CoinTossing ct = new CoinTossing(myId, otherId,
        randBit, network);
    ct.initialize();
    StrictBitVector seed = ct.toss(hashFunction.getDigestLength() * 8);
    return computeDhParams(seed.toByteArray());
  }

  /**
   * Compute Diffie-Hellman parameters based on a seed.
   * 
   * @param seed
   *          The seed used to sample the Diffie-Hellman parameters
   * @return The computed Diffie-Hellman parameters
   * @throws NoSuchAlgorithmException
   *           Thrown if the PRG algorithm used does not exist
   * @throws InvalidParameterSpecException
   *           Thrown if an error occurs with the Diffie-Hellman parameter class
   */
  private DHParameterSpec computeDhParams(byte[] seed) {
    try {
      // Make a parameter generator for Diffie-Hellman parameters
      AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator
          .getInstance("DH");
      SecureRandom commonRand = SecureRandom.getInstance(prgAlgorithm);
      commonRand.setSeed(seed);
      // Construct DH parameters of a 2048 bit group based on the common seed
      paramGen.init(diffieHellmanSize, commonRand);
      AlgorithmParameters params = paramGen.generateParameters();
      return params.getParameterSpec(DHParameterSpec.class);
    } catch (NoSuchAlgorithmException | InvalidParameterSpecException e) {
      throw new MPCException("Something, non-malicious, went wrong when "
          + "agreeing on the Diffie-Hellman parameters.", e);
    }
  }
}
