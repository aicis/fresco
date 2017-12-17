package dk.alexandra.fresco.tools.mascot.field;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class FieldElementGeneratorCache {

  private static final BigInteger[] powers = {new BigInteger("1"), new BigInteger("2"),
      new BigInteger("4"), new BigInteger("8"), new BigInteger("16"), new BigInteger("32"),
      new BigInteger("64"), new BigInteger("128"), new BigInteger("256"), new BigInteger("512"),
      new BigInteger("1024"), new BigInteger("2048"), new BigInteger("4096"),
      new BigInteger("8192"), new BigInteger("16384"), new BigInteger("32768"),
      new BigInteger("65536"), new BigInteger("131072"), new BigInteger("262144"),
      new BigInteger("524288"), new BigInteger("1048576"), new BigInteger("2097152"),
      new BigInteger("4194304"), new BigInteger("8388608"), new BigInteger("16777216"),
      new BigInteger("33554432"), new BigInteger("67108864"), new BigInteger("134217728"),
      new BigInteger("268435456"), new BigInteger("536870912"), new BigInteger("1073741824"),
      new BigInteger("2147483648"), new BigInteger("4294967296"), new BigInteger("8589934592"),
      new BigInteger("17179869184"), new BigInteger("34359738368"), new BigInteger("68719476736"),
      new BigInteger("137438953472"), new BigInteger("274877906944"),
      new BigInteger("549755813888"), new BigInteger("1099511627776"),
      new BigInteger("2199023255552"), new BigInteger("4398046511104"),
      new BigInteger("8796093022208"), new BigInteger("17592186044416"),
      new BigInteger("35184372088832"), new BigInteger("70368744177664"),
      new BigInteger("140737488355328"), new BigInteger("281474976710656"),
      new BigInteger("562949953421312"), new BigInteger("1125899906842624"),
      new BigInteger("2251799813685248"), new BigInteger("4503599627370496"),
      new BigInteger("9007199254740992"), new BigInteger("18014398509481984"),
      new BigInteger("36028797018963968"), new BigInteger("72057594037927936"),
      new BigInteger("144115188075855872"), new BigInteger("288230376151711744"),
      new BigInteger("576460752303423488"), new BigInteger("1152921504606846976"),
      new BigInteger("2305843009213693952"), new BigInteger("4611686018427387904"),
      new BigInteger("9223372036854775808"), new BigInteger("18446744073709551616"),
      new BigInteger("36893488147419103232"), new BigInteger("73786976294838206464"),
      new BigInteger("147573952589676412928"), new BigInteger("295147905179352825856"),
      new BigInteger("590295810358705651712"), new BigInteger("1180591620717411303424"),
      new BigInteger("2361183241434822606848"), new BigInteger("4722366482869645213696"),
      new BigInteger("9444732965739290427392"), new BigInteger("18889465931478580854784"),
      new BigInteger("37778931862957161709568"), new BigInteger("75557863725914323419136"),
      new BigInteger("151115727451828646838272"), new BigInteger("302231454903657293676544"),
      new BigInteger("604462909807314587353088"), new BigInteger("1208925819614629174706176"),
      new BigInteger("2417851639229258349412352"), new BigInteger("4835703278458516698824704"),
      new BigInteger("9671406556917033397649408"), new BigInteger("19342813113834066795298816"),
      new BigInteger("38685626227668133590597632"), new BigInteger("77371252455336267181195264"),
      new BigInteger("154742504910672534362390528"), new BigInteger("309485009821345068724781056"),
      new BigInteger("618970019642690137449562112"), new BigInteger("1237940039285380274899124224"),
      new BigInteger("2475880078570760549798248448"),
      new BigInteger("4951760157141521099596496896"),
      new BigInteger("9903520314283042199192993792"),
      new BigInteger("19807040628566084398385987584"),
      new BigInteger("39614081257132168796771975168"),
      new BigInteger("79228162514264337593543950336"),
      new BigInteger("158456325028528675187087900672"),
      new BigInteger("316912650057057350374175801344"),
      new BigInteger("633825300114114700748351602688"),
      new BigInteger("1267650600228229401496703205376"),
      new BigInteger("2535301200456458802993406410752"),
      new BigInteger("5070602400912917605986812821504"),
      new BigInteger("10141204801825835211973625643008"),
      new BigInteger("20282409603651670423947251286016"),
      new BigInteger("40564819207303340847894502572032"),
      new BigInteger("81129638414606681695789005144064"),
      new BigInteger("162259276829213363391578010288128"),
      new BigInteger("324518553658426726783156020576256"),
      new BigInteger("649037107316853453566312041152512"),
      new BigInteger("1298074214633706907132624082305024"),
      new BigInteger("2596148429267413814265248164610048"),
      new BigInteger("5192296858534827628530496329220096"),
      new BigInteger("10384593717069655257060992658440192"),
      new BigInteger("20769187434139310514121985316880384"),
      new BigInteger("41538374868278621028243970633760768"),
      new BigInteger("83076749736557242056487941267521536"),
      new BigInteger("166153499473114484112975882535043072"),
      new BigInteger("332306998946228968225951765070086144"),
      new BigInteger("664613997892457936451903530140172288"),
      new BigInteger("1329227995784915872903807060280344576"),
      new BigInteger("2658455991569831745807614120560689152"),
      new BigInteger("5316911983139663491615228241121378304"),
      new BigInteger("10633823966279326983230456482242756608"),
      new BigInteger("21267647932558653966460912964485513216"),
      new BigInteger("42535295865117307932921825928971026432"),
      new BigInteger("85070591730234615865843651857942052864"),
      new BigInteger("170141183460469231731687303715884105728")};
  private static final HashMap<BigInteger, List<FieldElement>> generators = new HashMap<>();
  
  static {
    BigInteger smallPrime = new BigInteger("65521");
    int smallBitLength = 16;
    generators.put(smallPrime, precompute(smallPrime, smallBitLength));
    BigInteger largePrime = new BigInteger("340282366920938463463374607431768211297");
    int largeBitLength = 128;
    generators.put(largePrime, precompute(largePrime, largeBitLength));
  }

  static List<FieldElement> precompute(BigInteger modulus, int modBitLength) {
    List<FieldElement> fePowers = new ArrayList<>(modBitLength);
    for (int i = 0; i < modBitLength; i++) {
      fePowers.add(new FieldElement(powers[i], modulus, modBitLength));
    }
    return fePowers;
  }

  public static boolean isCached(BigInteger modulus) {
    return generators.containsKey(modulus);
  }
  
  /**
   * Returns field element representations of powers of two from 0 to modBitLength.
   */
  public static List<FieldElement> getGenerators(BigInteger modulus, int modBitLength) {
    if (generators.containsKey(modulus)) {
      return generators.get(modulus);
    } else {
      // TODO could cache this dynamically
      return precompute(modulus, modBitLength);
    }
  }

}