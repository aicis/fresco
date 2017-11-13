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
            DRes<SInt> plainText = intFactory.known(BigInteger.valueOf(10));
            DRes<SInt> cipherText = builder.seq(new MiMCEncryption(plainText, encryptionKey));
            return builder.numeric().open(cipherText);
          };

          BigInteger result = runApplication(app);

          BigInteger expectedModulus = new BigInteger(
              "2582249878086908589655919172003011874329705792829223512830659356540647622016841194629645353280137831435903171972747493557");
          Assert.assertEquals(expectedModulus, modulus[0]);
          BigInteger expectedCipherText = new BigInteger(
              "10388336824440235723309131431891968131690383663436711590309818298349333623568340591094832870178074855376232596303647115");
          Assert.assertEquals(expectedCipherText, result);
        }
      };
    }
  }

  public static class TestMiMCEncSameEnc<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, ProtocolBuilderNumeric> {

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {

          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app = builder -> {
            Numeric intFactory = builder.numeric();
            DRes<SInt> encryptionKey = intFactory.known(BigInteger.valueOf(527618));
            DRes<SInt> plainText = intFactory.known(BigInteger.valueOf(10));
            DRes<SInt> cipherText = builder.seq(new MiMCEncryption(plainText, encryptionKey));
            DRes<SInt> cipherText2 = builder.seq(new MiMCEncryption(plainText, encryptionKey));
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
            DRes<SInt> cipherTextA = builder.seq(new MiMCEncryption(plainTextA, encryptionKey));
            DRes<SInt> cipherTextB = builder.seq(new MiMCEncryption(plainTextB, encryptionKey));
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

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderNumeric> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderNumeric>() {

        @Override
        public void test() throws Exception {
          BigInteger x_big = BigInteger.valueOf(10);
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            Numeric intFactory = builder.numeric();
            DRes<SInt> encryptionKey = intFactory.known(BigInteger.valueOf(10));
            DRes<SInt> plainText = intFactory.known(x_big);
            DRes<SInt> cipherText = builder.seq(new MiMCEncryption(plainText, encryptionKey));
            DRes<SInt> decrypted = builder.seq(new MiMCDecryption(cipherText, encryptionKey));
            return builder.numeric().open(decrypted);
          };

          BigInteger result = runApplication(app);
          Assert.assertEquals(x_big, result);
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
          BigInteger x_big = BigInteger.valueOf(10);
          Application<BigInteger, ProtocolBuilderNumeric> app = builder -> {
            Numeric intFactory = builder.numeric();
            builder.getBasicNumericContext();
            DRes<SInt> encryptionKey = intFactory.known(BigInteger.valueOf(527619));
            DRes<SInt> plainText = intFactory.known(x_big);
            DRes<SInt> cipherText = builder.seq(new MiMCEncryption(plainText, encryptionKey, 17));
            DRes<SInt> decrypted = builder.seq(new MiMCDecryption(cipherText, encryptionKey, 17));
            return builder.numeric().open(decrypted);
          };

          BigInteger result = runApplication(app);
          Assert.assertEquals(x_big, result);
        }
      };
    }
  }

}
