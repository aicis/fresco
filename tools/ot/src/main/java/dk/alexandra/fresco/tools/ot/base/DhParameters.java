package dk.alexandra.fresco.tools.ot.base;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.ExceptionConverter;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.cointossing.CoinTossing;

import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.spec.DHParameterSpec;

/**
 * Class for generating Diffie-Hellman parameters using Java's internal functionality. The class can
 * be used to both generate the parameters securely using coin-tossing, locally using a seed or
 * simply to retrieve a pair of static parameters.
 */
public class DhParameters {
  /**
   * The PRG algorithm used internally by Java to generate the Diffie-Hellman
   * parameters based on some seed. This PRG MUST only be used for this purpose
   * since SHA1 is insecure in the general case.
   */
  private static final String PRG_ALGORITHM = "SHA1PRNG";
  private static final int DIFFIE_HELLMAN_SIZE = 2048;
  private static final String HASH_ALGORITHM = "SHA-256";
  private static final BigInteger DhGvalue = new BigInteger(
      "1817929693051677794042418360119535939035448877384059423016092223723589389"
          + "89386921540078076694389023214591116103022506752626702949377742490622411"
          + "36154252930934999558878557838951366230121689192613836661801579283976804"
          + "90566221950235571908449465416597162122008963523511429191971262704962062"
          + "23722995544735685829105160578247097947199471860741139749699562917671426"
          + "82888600060270321923905677901250333513320663621356005726499527794262632"
          + "80575136645831734174762968521856711608877942562412558950963899754610266"
          + "97615963606394464455636761856586890950014177457842992286652934126338664"
          + "99748366638338849983708609236396436614761807745");
  private static final BigInteger DhPvalue = new BigInteger(
      "2080109726332741595567900301553712643291061397185326442939225885200811703"
          + "46221477943683854922915625754365585955880683687623164529077074421717622"
          + "55815247681135202838112705300460371527291002353818384380395178484616163"
          + "81789931732016235932408088148285827220196826505807878031275264842308641"
          + "84386700540754381703938109115634660390655677474772619937553430208773150"
          + "06567328507885962926589890627547794887973720401310026543273364787901564"
          + "85827844212318499978829377355564689095172787513731965744913645190518423"
          + "06594567246898679968677700656495114013774368779648395287433119164167454"
          + "67731166272088057888135437754886129005590419051");

  /**
   * Agree on Diffie-Hellman parameters using coin-tossing.
   *
   * @param myId
   *          The ID of the calling party
   * @param otherId
   *          The ID of the other party
   * @param rand
   *          The calling party's secure randomness generator
   * @param network
   *          The underlying network to use
   * @return 2048 bit Diffie-Hellman parameters.
   */
  public DHParameterSpec computeSecureDhParams(int myId, int otherId,
      Drbg rand, Network network) {
    MessageDigest hashDigest = ExceptionConverter.safe(() -> MessageDigest.getInstance(
            HASH_ALGORITHM),
        "Missing secure, hash function which is dependent in this library");
    // Do coin-tossing to agree on a random seed of "kbitLength" bits
    CoinTossing ct = new CoinTossing(myId, otherId, rand);
    ct.initialize(network);
    StrictBitVector seed = ct.toss(hashDigest.getDigestLength() * 8);
    return computeDhParams(seed.toByteArray());
  }

  /**
   * Compute and set Diffie-Hellman parameters based on a seed.
   *
   * @param seed
   *          The seed used to sample the Diffie-Hellman parameters
   */
  private DHParameterSpec computeDhParams(byte[] seed) {
    // Make a parameter generator for Diffie-Hellman parameters
    AlgorithmParameterGenerator paramGen = ExceptionConverter.safe(
        () -> AlgorithmParameterGenerator.getInstance("DH"),
        "Missing Java internal parameter generator which is needed in this library");
    SecureRandom commonRand = ExceptionConverter.safe(() -> SecureRandom
        .getInstance(PRG_ALGORITHM),
        "Missing Java internal PRG which is needed in this library");
    commonRand.setSeed(seed);
    // Construct DH parameters of a group based on the common seed
    paramGen.init(DIFFIE_HELLMAN_SIZE, commonRand);
    AlgorithmParameters params = paramGen.generateParameters();
    return ExceptionConverter.safe(() ->
        params.getParameterSpec(DHParameterSpec.class),
        "Missing Java internal parameter generator which is needed in this library");
  }

  /**
   * Returns a static set of 2048 bit Diffie-Hellman parameters.
   * <p>
   * These are computed using Java's internal parameter generator using a {@code SecureRandom}
   * randomness generator seeded with the byte 0x42.
   * </p>
   *
   * @return Static Diffie-Hellman parameters
   */
  public static DHParameterSpec getStaticDhParams() {
    return new DHParameterSpec(DhPvalue, DhGvalue);
  }
}
