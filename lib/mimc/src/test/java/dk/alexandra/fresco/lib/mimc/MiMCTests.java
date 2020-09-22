package dk.alexandra.fresco.lib.mimc;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;
import org.junit.Assert;

public class MiMCTests {

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

          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app =
              builder -> {
                Numeric intFactory = builder.numeric();
                DRes<SInt> encryptionKey = intFactory.known(BigInteger.valueOf(527618));
                DRes<SInt> plainText = intFactory.known(BigInteger.TEN);
                DRes<SInt> cipherText;
                DRes<SInt> cipherText2;
                if (reduced) {
                  cipherText =
                      builder.seq(new MiMCEncryptionReducedRounds(plainText, encryptionKey));
                  cipherText2 =
                      builder.seq(new MiMCEncryptionReducedRounds(plainText, encryptionKey));
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

          Application<Pair<BigInteger, BigInteger>, ProtocolBuilderNumeric> app =
              builder -> {
                Numeric intFactory = builder.numeric();
                DRes<SInt> encryptionKey = intFactory.known(BigInteger.valueOf(527618));
                DRes<SInt> plainTextA = intFactory.known(BigInteger.valueOf(10));
                DRes<SInt> plainTextB = intFactory.known(BigInteger.valueOf(11));
                DRes<SInt> cipherTextA = null;
                DRes<SInt> cipherTextB = null;
                if (reduced) {
                  cipherTextA =
                      builder.seq(new MiMCEncryptionReducedRounds(plainTextA, encryptionKey));
                  cipherTextB =
                      builder.seq(new MiMCEncryptionReducedRounds(plainTextB, encryptionKey));
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
          Application<BigInteger, ProtocolBuilderNumeric> app =
              builder -> {
                Numeric intFactory = builder.numeric();
                DRes<SInt> encryptionKey = intFactory.known(BigInteger.TEN);
                DRes<SInt> plainText = intFactory.known(ten);
                DRes<SInt> decrypted = null;
                if (reduced) {
                  DRes<SInt> cipherText =
                      builder.seq(new MiMCEncryptionReducedRounds(plainText, encryptionKey));
                  decrypted =
                      builder.seq(new MiMCDecryptionReducedRounds(cipherText, encryptionKey));
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
          Application<BigInteger, ProtocolBuilderNumeric> app =
              builder -> {
                Numeric intFactory = builder.numeric();
                DRes<SInt> encryptionKey = intFactory.known(BigInteger.valueOf(527619));
                DRes<SInt> plainText = intFactory.known(ten);
                DRes<SInt> cipherText =
                    builder.seq(new MiMCEncryption(plainText, encryptionKey, 17));
                DRes<SInt> decrypted =
                    builder.seq(new MiMCDecryption(cipherText, encryptionKey, 17));
                return builder.numeric().open(decrypted);
              };

          BigInteger result = runApplication(app);
          Assert.assertEquals(ten, result);
        }
      };
    }
  }
}
