package dk.alexandra.fresco.tools.ot.base;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;
import java.util.Random;

import javax.crypto.spec.DHParameterSpec;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;
import dk.alexandra.fresco.tools.cointossing.FailedCoinTossingException;
import dk.alexandra.fresco.tools.commitment.FailedCommitmentException;
import dk.alexandra.fresco.tools.commitment.MaliciousCommitmentException;

public class SimplestOT implements Ot<Serializable>
{
  private int myId;
  private int otherId;
  private int kbitLength;
  private Network network;
  private Random rand;

  // The public Diffie-Hellman parameters
  private DHParameterSpec params = null;
  private static final int diffieHellmanSize = 2048;

  public SimplestOT(int myId, int otherId, int kbitLength, Network network,
      Random rand) {
    this.myId = myId;
    this.otherId = otherId;
    this.kbitLength = kbitLength;
    this.network = network;
    this.rand = rand;
  }

  @Override
  public void send(Serializable messageZero, Serializable messageOne)
      throws MaliciousOtException, FailedOtException {
    if (params == null) {
      computeDHParams();
    }
    // Pick random element k
    BigInteger c = new BigInteger(diffieHellmanSize, rand);
    // Do rejection sampling to pick a random value k
    // While c > P
    while (c.compareTo(params.getP()) != -1) {
      c = new BigInteger(diffieHellmanSize, rand);
    }
    network.send(otherId, c.toByteArray());
    BigInteger keyZero = new BigInteger(network.receive(otherId));
    // keyOne = c / keyZero
    BigInteger keyOne = keyZero.modInverse(params.getP()).multiply(c);
    // Pick random element rZero
    BigInteger rZero = new BigInteger(diffieHellmanSize, rand);
    // Do rejection sampling to pick a random value rZero
    // While rZero > P
    while (rZero.compareTo(params.getP()) != -1) {
      rZero = new BigInteger(diffieHellmanSize, rand);
    }
    // Pick random element rOne
    BigInteger rOne = new BigInteger(diffieHellmanSize, rand);
    // Do rejection sampling to pick a random value rOne
    // While rOne > P
    while (rOne.compareTo(params.getP()) != -1) {
      rOne = new BigInteger(diffieHellmanSize, rand);
    }
    // Encrypt message Zero
    // eZeroA = g^rZero mod p
    BigInteger eZeroA = params.getG().modPow(rZero, params.getP());
    // eZeroB = PRG(keyZero^rZero) XOR messageZero
    // TODO finish
    BigInteger eZeroB = params.getG().modPow(rZero, params.getP());
    Pair<BigInteger, BigInteger> eZero = new Pair<>(eZeroA, eZeroB);
  }

  @Override
  public Serializable receive(Boolean choiceBit)
      throws MaliciousOtException, FailedOtException {
    if (params == null) {
      computeDHParams();
    }
    BigInteger c = new BigInteger(network.receive(otherId));
    // Pick random element k
    BigInteger k = new BigInteger(diffieHellmanSize, rand);
    // Do rejection sampling to pick a random value k
    // While k > P
    while (k.compareTo(params.getP()) != -1) {
      k = new BigInteger(diffieHellmanSize, rand);
    }
    // keySigma = G^k mod P
    BigInteger keySigma = params.getG().modPow(k, params.getP());
    // keyNotSigma = c / keySigma mod P
    BigInteger keyNotSigma = keySigma.modInverse(params.getP()).multiply(c);
    if (choiceBit == false) {
      network.send(otherId, keySigma.toByteArray());
    } else {
      network.send(otherId, keyNotSigma.toByteArray());
    }
    return null;
  }

  public void computeDHParams() {
    try {
      // Do coin-tossing to agree on a random seed of "kbitLength" bits
      CoinTossing ct = new CoinTossing(myId, otherId, kbitLength, rand,
          network);
      ct.initialize();
      StrictBitVector seed = ct.toss(kbitLength);
      // Make a parameter generator for Diffie-Hellman parameters
      AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator
          .getInstance("DH");
      SecureRandom commonRand = SecureRandom.getInstance("SHA1PRNG");
      commonRand.setSeed(seed.toByteArray());
      // Construct DH parameters of a 2048 bit group based on the common seed
      paramGen.init(diffieHellmanSize, commonRand);
      AlgorithmParameters params = paramGen.generateParameters();
      this.params = params.getParameterSpec(DHParameterSpec.class);
    } catch (MaliciousCommitmentException | FailedCommitmentException
        | FailedCoinTossingException | NoSuchAlgorithmException
        | InvalidParameterSpecException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
