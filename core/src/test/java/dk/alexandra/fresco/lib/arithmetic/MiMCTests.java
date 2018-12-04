package dk.alexandra.fresco.lib.arithmetic;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.crypto.mimc.MiMCDecryption;
import dk.alexandra.fresco.lib.crypto.mimc.MiMCEncryption;
import dk.alexandra.fresco.lib.crypto.mimc.MimcDecryptionReducedRounds;
import dk.alexandra.fresco.lib.crypto.mimc.MimcEncryptionReducedRounds;
import java.math.BigInteger;
import org.junit.Assert;

public class MiMCTests {

  /*
   * Note: This unit test is a rather ugly workaround for the following issue: MiMC encryption is
   * deterministic, however its results depend on the modulus used by the backend arithmetic suite.
   * So in order to assert that a call to the encryption functionality always produces the same
   * result is to ensure that the modulus we use is the one we expect to see. I put in an explicit
   * assertion on the modulus because each suite that provides concrete implementations for this
   * test will do its own set up and if the modulus is not set correctly this test will fail (rather
   * mysteriously).
   */
  public static class TestMiMCEncryptsDeterministically<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    final boolean reduced;

    public TestMiMCEncryptsDeterministically(boolean reduced) {
      this.reduced = reduced;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {
        @Override
        public void test() throws Exception {
          final BigInteger[] modulus = new BigInteger[1];
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            Numeric intFactory = builder.numeric();
            modulus[0] = builder.getBasicNumericContext().getModulus();
            DRes<SInt> encryptionKey = intFactory.known(BigInteger.valueOf(527618));
            DRes<SInt> plainText = intFactory.known(BigInteger.TEN);
            DRes<SInt> cipherText;
            if (reduced) {
              cipherText = builder.seq(new MimcEncryptionReducedRounds(plainText, encryptionKey));
            } else {
              cipherText = builder.seq(new MiMCEncryption(plainText, encryptionKey));
            }
            return builder.numeric().open(cipherText);
          };

          BigInteger result = runApplication(app);

          BigInteger expectedModulus = new BigInteger(
              "134078079299425970995740249982058461274793658205923933777235614437217640300735469768"
              + "01874298166903427690031858186486050853753882811946569946433649006083527");
          Assert.assertEquals(expectedModulus, modulus[0]);
          BigInteger expectedCipherText = new BigInteger(
              "182057525656734328782253910304912356629754652441253640768029931143270255868903035292"
              + "5047622023903485129724476853331600792699010701523638266948303172055567");
          Assert.assertEquals(expectedCipherText, result);
        }
      };
    }
  }

  public static class TestMiMCEncSameEnc<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    final boolean reduced;

    public TestMiMCEncSameEnc(boolean reduced) {
      this.reduced = reduced;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {

          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric intFactory = builder.numeric();
            DRes<SInt> encryptionKey = intFactory.known(BigInteger.valueOf(527618));
            DRes<SInt> plainText = intFactory.known(BigInteger.TEN);
            DRes<SInt> cipherText;
            DRes<SInt> cipherText2;
            if (reduced) {
              cipherText = builder.seq(new MimcEncryptionReducedRounds(plainText, encryptionKey));
              cipherText2 = builder.seq(new MimcEncryptionReducedRounds(plainText, encryptionKey));
            } else {
              cipherText = builder.seq(new MiMCEncryption(plainText, encryptionKey));
              cipherText2 = builder.seq(new MiMCEncryption(plainText, encryptionKey));
            }
            DRes<BigInteger> result1 = builder.numeric().open(cipherText);
            DRes<BigInteger> result2 = builder.numeric().open(cipherText2);
            return () -> new Pair<>(result1.out(), result2.out());
          };

          Pair<BigInteger, BigInteger> result = runApplication(app);

          Assert.assertEquals(result.getFirst(), result.getSecond());
        }
      };
    }
  }

  public static class TestMiMCDifferentPlainTexts<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    final boolean reduced;

    public TestMiMCDifferentPlainTexts(boolean reduced) {
      this.reduced = reduced;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {

          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric intFactory = builder.numeric();
            DRes<SInt> encryptionKey = intFactory.known(BigInteger.valueOf(527618));
            DRes<SInt> plainTextA = intFactory.known(BigInteger.valueOf(10));
            DRes<SInt> plainTextB = intFactory.known(BigInteger.valueOf(11));
            DRes<SInt> cipherTextA = null;
            DRes<SInt> cipherTextB = null;
            if (reduced) {
              cipherTextA = builder.seq(new MimcEncryptionReducedRounds(plainTextA, encryptionKey));
              cipherTextB = builder.seq(new MimcEncryptionReducedRounds(plainTextB, encryptionKey));
            } else {
              cipherTextA = builder.seq(new MiMCEncryption(plainTextA, encryptionKey));
              cipherTextB = builder.seq(new MiMCEncryption(plainTextB, encryptionKey));
            }
            DRes<BigInteger> resultA = builder.numeric().open(cipherTextA);
            DRes<BigInteger> resultB = builder.numeric().open(cipherTextB);
            return () -> new Pair<>(resultA.out(), resultB.out());
          };

          Pair<BigInteger, BigInteger> result = runApplication(app);

          Assert.assertNotEquals(result.getFirst(), result.getSecond());
        }
      };
    }
  }

  public static class TestMiMCEncDec<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    boolean reduced = false;

    public TestMiMCEncDec(boolean reduced) {
      super();
      this.reduced = reduced;
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          BigInteger ten = BigInteger.TEN;
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            Numeric intFactory = builder.numeric();
            DRes<SInt> encryptionKey = intFactory.known(BigInteger.TEN);
            DRes<SInt> plainText = intFactory.known(ten);
            DRes<SInt> decrypted = null;
            if (reduced) {
              DRes<SInt> cipherText =
                  builder.seq(new MimcEncryptionReducedRounds(plainText, encryptionKey));
              decrypted = builder.seq(new MimcDecryptionReducedRounds(cipherText, encryptionKey));
            } else {
              DRes<SInt> cipherText = builder.seq(new MiMCEncryption(plainText, encryptionKey));
              decrypted = builder.seq(new MiMCDecryption(cipherText, encryptionKey));
            }
            return builder.numeric().open(decrypted);
          };

          BigInteger result = runApplication(app);
          Assert.assertEquals(ten, result);
        }
      };
    }
  }

  public static class TestMiMCEncDecFixedRounds<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          BigInteger ten = BigInteger.TEN;
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            Numeric intFactory = builder.numeric();
            DRes<SInt> encryptionKey = intFactory.known(BigInteger.valueOf(527619));
            DRes<SInt> plainText = intFactory.known(ten);
            DRes<SInt> cipherText = builder.seq(new MiMCEncryption(plainText, encryptionKey, 17));
            DRes<SInt> decrypted = builder.seq(new MiMCDecryption(cipherText, encryptionKey, 17));
            return builder.numeric().open(decrypted);
          };

          BigInteger result = runApplication(app);
          Assert.assertEquals(ten, result);
        }
      };
    }
  }

}
