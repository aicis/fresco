/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL, and Bouncy Castle.
 * Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.crypto;

import static org.junit.Assert.assertTrue;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.SequentialBinaryBuilder;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SBool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

/**
 * Some generic tests for basic crypto primitives a la AES and SHA1.
 *
 * Can be used to test any protocol suite that supports BasicLogicFactory.
 */
public class BristolCryptoTests {

  /**
   * Convert hex string to boolean array.
   *
   * // 1 --> true, 0 --> false
   */
  private static boolean[] toBoolean(String hex) throws IllegalArgumentException {
    if (hex.length() % 2 != 0) {
      throw new IllegalArgumentException("Illegal hex string");
    }
    boolean[] res = new boolean[hex.length() * 4];
    for (int i = 0; i < hex.length() / 2; i++) {
      String sub = hex.substring(2 * i, 2 * i + 2);
      int value = Integer.parseInt(sub, 16);
      int numOfBits = 8;
      for (int j = 0; j < numOfBits; j++) {
        boolean val = (value & 1 << j) != 0;
        res[8 * i + (numOfBits - j - 1)] = val;
      }
    }
    return res;
  }


  @Test
  public void testToBoolean() throws Exception {
    boolean[] res = toBoolean("2b7e151628aed2a6abf7158809cf4f3c");
    assertTrue(Arrays.equals(
        new boolean[] {false, false, true, false, true, false, true, true, false, true, true, true},
        Arrays.copyOf(res, 12)));
  }


  /**
   * Testing AES encryption using standard test vectors.
   *
   * TODO: Include more FIPS test vectors.
   */
  public static class AesTest<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, SequentialBinaryBuilder> {

    private boolean doAsserts;

    public AesTest(boolean doAsserts) {
      this.doAsserts = doAsserts;
    }

    @Override
    public TestThread<ResourcePoolT, SequentialBinaryBuilder> next(
        TestThreadConfiguration<ResourcePoolT, SequentialBinaryBuilder> conf) {
      return new TestThread<ResourcePoolT, SequentialBinaryBuilder>() {

        // This is just some fixed test vectors for AES in ECB mode that was
        // found somewhere on the net, i.e., this is some known plaintexts and
        // corresponding cipher texts that can be used for testing.
        final String[] keyVec = new String[] {"000102030405060708090a0b0c0d0e0f"};
        final String plainVec = "00112233445566778899aabbccddeeff";
        final String[] cipherVec = new String[] {"69c4e0d86a7b0430d8cdb78070b4c55a"};

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, SequentialBinaryBuilder> multApp =
              new Application<List<Boolean>, ProtocolBuilderBinary.SequentialBinaryBuilder>() {

            @Override
            public Computation<List<Boolean>> prepareApplication(
                ProtocolBuilderBinary.SequentialBinaryBuilder producer) {
              return producer.seq(seq -> {
                List<Computation<SBool>> plainText = seq.binary().known(toBoolean(plainVec));
                List<Computation<SBool>> key = seq.binary().known(toBoolean(keyVec[0]));
                List<List<Computation<SBool>>> inputs = new ArrayList<>();
                inputs.add(plainText);
                inputs.add(key);
                return () -> inputs;
              }).seq((inputs, seq) -> {
                Computation<List<SBool>> l = seq.bristol().AES(inputs.get(0), inputs.get(1));
                return l;
              }).seq((res, seq) -> {
                List<Computation<Boolean>> outputs = new ArrayList<>();
                for (SBool boo : res) {
                  outputs.add(seq.binary().open(() -> boo));
                }
                return () -> outputs;
              }).seq((output, seq) -> {
                return () -> output.stream().map(Computation::out).collect(Collectors.toList());
              });

            }

          };

          List<Boolean> res = secureComputationEngine.runApplication(multApp,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          boolean[] expected = toBoolean(cipherVec[0]);
          boolean[] actual = new boolean[res.size()];
          for (int i = 0; i < res.size(); i++) {
            actual[i] = res.get(i);
          }

          if (doAsserts) {
            Assert.assertArrayEquals(expected, actual);
          }
        }
      };
    }
  }


  /**
   * Testing SHA-1 compression function.
   *
   * TODO: Include all three test vectors.
   */
  public static class Sha1Test<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, SequentialBinaryBuilder> {

    private boolean doAsserts;

    public Sha1Test(boolean doAsserts) {
      this.doAsserts = doAsserts;
    }

    @Override
    public TestThread<ResourcePoolT, SequentialBinaryBuilder> next(
        TestThreadConfiguration<ResourcePoolT, SequentialBinaryBuilder> conf) {
      return new TestThread<ResourcePoolT, SequentialBinaryBuilder>() {
        /*
         * IMPORTANT: These are NOT test vectors for the complete SHA-1 hash function, as the
         * padding rules are ignored. Therefore, use of tools like md5sum will produce a different
         * output if supplied with the same inputs.
         */
        String[] ins = {
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
            "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f",
            "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"};
        String[] outs = {"92b404e556588ced6c1acd4ebf053f6809f73a93",
            "b9ac757bbc2979252e22727406872f94cbea56a1", "bafbc2c87c33322603f38e06c3e0f79c1f1b1475"};

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, SequentialBinaryBuilder> multApp =
              new Application<List<Boolean>, ProtocolBuilderBinary.SequentialBinaryBuilder>() {

            @Override
            public Computation<List<Boolean>> prepareApplication(
                ProtocolBuilderBinary.SequentialBinaryBuilder producer) {
              return producer.seq(seq -> {
                List<Computation<SBool>> input1 = seq.binary().known(toBoolean(ins[0]));
                // List<Computation<SBool>> input2 = seq.binary().known(toBoolean(ins[1]));
                // List<Computation<SBool>> input3 = seq.binary().known(toBoolean(ins[2]));
                List<List<Computation<SBool>>> inputs = new ArrayList<>();
                inputs.add(input1);
                // inputs.add(input2);
                // inputs.add(input3);
                return () -> inputs;
              }).seq((inputs, seq) -> {
                // List<Computation<List<SBool>>> list = new ArrayList<>();
                // list.add(seq.bristol().getSHA1Circuit(inputs.get(0)));
                // list.add(seq.bristol().getSHA1Circuit(inputs.get(1)));
                // list.add(seq.bristol().getSHA1Circuit(inputs.get(2)));

                Computation<List<SBool>> list = seq.bristol().SHA1(inputs.get(0));
                return list;
              }).seq((res, seq) -> {
                List<Computation<Boolean>> outputs = new ArrayList<>();
                for (SBool boo : res) {
                  outputs.add(seq.binary().open(boo));
                }
                // List<List<Computation<Boolean>>> outputs = new ArrayList<>();
                // for (Computation<List<SBool>> elm : res) {
                // List<Computation<Boolean>> outList = new ArrayList<>();
                // for (SBool boo : elm.out()) {
                // outList.add(seq.binary().open(() -> boo));
                // }
                // outputs.add(outList);
                // }
                return () -> outputs;
              }).seq((output, seq) -> {
                // List<List<Boolean>> result = new ArrayList<>();
                // for (List<Computation<Boolean>> o : output) {
                // result.add(o.stream().map(Computation::out).collect(Collectors.toList()));
                // }
                return () -> output.stream().map(Computation::out).collect(Collectors.toList());
                // return () -> result;
              });

            }

          };

          List<Boolean> res = secureComputationEngine.runApplication(multApp,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          boolean[][] expected = new boolean[1][];
          boolean[][] actuals = new boolean[1][res.size()];
          for (int count = 0; count < 1; count++) {
            expected[count] = toBoolean(outs[count]);
            for (int i = 0; i < res.size(); i++) {
              actuals[count][i] = res.get(i);
            }
            if (doAsserts) {
              Assert.assertArrayEquals(expected[count], actuals[count]);
            }
          }
        }
      };
    }
  }


  /**
   * Testing SHA-1 compression function.
   *
   * TODO: Include all three test vectors.
   */
  public static class Sha256Test<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, SequentialBinaryBuilder> {

    private boolean doAsserts;

    public Sha256Test(boolean doAsserts) {
      this.doAsserts = doAsserts;
    }

    @Override
    public TestThread<ResourcePoolT, SequentialBinaryBuilder> next(
        TestThreadConfiguration<ResourcePoolT, SequentialBinaryBuilder> conf) {
      return new TestThread<ResourcePoolT, SequentialBinaryBuilder>() {
        /*
         * IMPORTANT: These are NOT test vectors for the complete SHA-256 hash function, as the
         * padding rules are ignored. Therefore, use of tools like md5sum will produce a different
         * output if supplied with the same inputs.
         */
        String in1 =
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        String out1 = "da5698be17b9b46962335799779fbeca8ce5d491c0d26243bafef9ea1837a9d8";
        String in2 =
            "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f";
        String out2 = "fc99a2df88f42a7a7bb9d18033cdc6a20256755f9d5b9a5044a9cc315abe84a7";
        String in3 =
            "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
        String out3 = "ef0c748df4da50a8d6c43c013edc3ce76c9d9fa9a1458ade56eb86c0a64492d2";
        String in4 =
            "243f6a8885a308d313198a2e03707344a4093822299f31d0082efa98ec4e6c89452821e638d01377be5466cf34e90c6cc0ac29b7c97c50dd3f84d5b5b5470917";
        String out4 = "cf0ae4eb67d38ffeb94068984b22abde4e92bc548d14585e48dca8882d7b09ce";

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, SequentialBinaryBuilder> multApp =
              new Application<List<Boolean>, ProtocolBuilderBinary.SequentialBinaryBuilder>() {

            @Override
            public Computation<List<Boolean>> prepareApplication(
                ProtocolBuilderBinary.SequentialBinaryBuilder producer) {
              return producer.seq(seq -> {
                List<Computation<SBool>> input1 = seq.binary().known(toBoolean(in1));
                List<List<Computation<SBool>>> inputs = new ArrayList<>();
                inputs.add(input1);
                return () -> inputs;
              }).seq((inputs, seq) -> {
                Computation<List<SBool>> list = seq.bristol().SHA256(inputs.get(0));
                return list;
              }).seq((res, seq) -> {
                List<Computation<Boolean>> outputs = new ArrayList<>();
                for (SBool boo : res) {
                  outputs.add(seq.binary().open(boo));
                }
                return () -> outputs;
              }).seq((output, seq) -> {
                return () -> output.stream().map(Computation::out).collect(Collectors.toList());
              });

            }

          };

          List<Boolean> res = secureComputationEngine.runApplication(multApp,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          boolean[] expected = toBoolean(out1);
          boolean[] actual = new boolean[res.size()];
          for (int i = 0; i < res.size(); i++) {
            actual[i] = res.get(i);
          }
          if (doAsserts) {
            Assert.assertArrayEquals(expected, actual);
          }
        }
      };
    }
  }


  /**
   * TestingMD5 compression function.
   *
   * TODO: Include all three test vectors.
   */
  public static class MD5Test<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, SequentialBinaryBuilder> {

    private boolean doAsserts;

    public MD5Test(boolean doAsserts) {
      this.doAsserts = doAsserts;
    }

    @Override
    public TestThread<ResourcePoolT, SequentialBinaryBuilder> next(
        TestThreadConfiguration<ResourcePoolT, SequentialBinaryBuilder> conf) {
      return new TestThread<ResourcePoolT, SequentialBinaryBuilder>() {
        /*
         * IMPORTANT: These are NOT test vectors for the complete SHA-1 hash function, as the
         * padding rules are ignored. Therefore, use of tools like md5sum will produce a different
         * output if supplied with the same inputs.
         */
        String in1 =
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        String out1 = "ac1d1f03d08ea56eb767ab1f91773174";
        String in2 =
            "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f";
        String out2 = "cad94491c9e401d9385bfc721ef55f62";
        String in3 =
            "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
        String out3 = "b487195651913e494b55c6bddf405c01";
        String in4 =
            "243f6a8885a308d313198a2e03707344a4093822299f31d0082efa98ec4e6c89452821e638d01377be5466cf34e90c6cc0ac29b7c97c50dd3f84d5b5b5470917";
        String out4 = "3715f568f422db75cc8d65e11764ff01";

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, SequentialBinaryBuilder> multApp =
              new Application<List<Boolean>, ProtocolBuilderBinary.SequentialBinaryBuilder>() {

            @Override
            public Computation<List<Boolean>> prepareApplication(
                ProtocolBuilderBinary.SequentialBinaryBuilder producer) {
              return producer.seq(seq -> {
                List<Computation<SBool>> input1 = seq.binary().known(toBoolean(in1));
                List<List<Computation<SBool>>> inputs = new ArrayList<>();
                inputs.add(input1);
                return () -> inputs;
              }).seq((inputs, seq) -> {
                Computation<List<SBool>> list = seq.bristol().MD5(inputs.get(0));
                return list;
              }).seq((res, seq) -> {
                List<Computation<Boolean>> outputs = new ArrayList<>();
                for (SBool boo : res) {
                  outputs.add(seq.binary().open(boo));
                }
                return () -> outputs;
              }).seq((output, seq) -> {
                return () -> output.stream().map(Computation::out).collect(Collectors.toList());
              });

            }

          };

          List<Boolean> res = secureComputationEngine.runApplication(multApp,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          boolean[] expected = toBoolean(out1);
          boolean[] actual = new boolean[res.size()];
          for (int i = 0; i < res.size(); i++) {
            actual[i] = res.get(i);
          }
          if (doAsserts) {
            Assert.assertArrayEquals(expected, actual);
          }
        }
      };
    }
  }


  /**
   * Testing circuit for mult of two 32-bit numbers.
   *
   * TODO: Include more test vectors. TODO: Strangely, the output needs to be shifted 1 to the right
   * before it is correct.
   */
  public static class Mult32x32Test<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, SequentialBinaryBuilder> {

    private boolean doAsserts;

    public Mult32x32Test(boolean doAsserts) {
      this.doAsserts = doAsserts;
    }

    @Override
    public TestThread<ResourcePoolT, SequentialBinaryBuilder> next(
        TestThreadConfiguration<ResourcePoolT, SequentialBinaryBuilder> conf) {
      return new TestThread<ResourcePoolT, SequentialBinaryBuilder>() {
        // 16*258 = 4128
        String inv1 = "00000010";
        String inv2 = "00000102";
        String outv = "0000000000001020";

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, SequentialBinaryBuilder> multApp =
              new Application<List<Boolean>, ProtocolBuilderBinary.SequentialBinaryBuilder>() {

            @Override
            public Computation<List<Boolean>> prepareApplication(
                ProtocolBuilderBinary.SequentialBinaryBuilder producer) {
              return producer.seq(seq -> {
                List<Computation<SBool>> in1 = seq.binary().known(toBoolean(inv1));
                List<Computation<SBool>> in2 = seq.binary().known(toBoolean(inv2));
                List<List<Computation<SBool>>> inputs = new ArrayList<>();
                inputs.add(in1);
                inputs.add(in2);
                return () -> inputs;
              }).seq((inputs, seq) -> {
                Computation<List<SBool>> l = seq.bristol().mult32x32(inputs.get(0), inputs.get(1));
                return l;
              }).seq((res, seq) -> {
                List<Computation<Boolean>> outputs = new ArrayList<>();
                for (SBool boo : res) {
                  outputs.add(seq.binary().open(() -> boo));
                }
                return () -> outputs;
              }).seq((output, seq) -> {
                return () -> output.stream().map(Computation::out).collect(Collectors.toList());
              });

            }

          };

          List<Boolean> res = secureComputationEngine.runApplication(multApp,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          boolean[] expected = toBoolean(outv);
          boolean[] actual = new boolean[res.size()];
          actual[0] = false;
          for (int i = 0; i < res.size() - 1; i++) {
            actual[i + 1] = res.get(i);
          }
          if (doAsserts) {
            Assert.assertArrayEquals(expected, actual);
          }
        }
      };
    }
  }


  /**
   * Testing circuit for DES encryption.
   *
   * TODO: Include more test vectors, e.g., from here:
   * https://dl.dropboxusercontent.com/u/25980826/des.test
   */
  public static class DesTest<ResourcePoolT extends ResourcePool>
      extends TestThreadFactory<ResourcePoolT, SequentialBinaryBuilder> {

    private boolean doAsserts;

    public DesTest(boolean doAsserts) {
      this.doAsserts = doAsserts;
    }

    @Override
    public TestThread<ResourcePoolT, SequentialBinaryBuilder> next(
        TestThreadConfiguration<ResourcePoolT, SequentialBinaryBuilder> conf) {
      return new TestThread<ResourcePoolT, SequentialBinaryBuilder>() {

        String keyV = "0101010101010101";
        String plainV = "8000000000000000";
        String cipherV = "95F8A5E5DD31D900".toLowerCase();

        @Override
        public void test() throws Exception {
          Application<List<Boolean>, SequentialBinaryBuilder> multApp =
              new Application<List<Boolean>, ProtocolBuilderBinary.SequentialBinaryBuilder>() {

            @Override
            public Computation<List<Boolean>> prepareApplication(
                ProtocolBuilderBinary.SequentialBinaryBuilder producer) {
              return producer.seq(seq -> {
                List<Computation<SBool>> plainText = seq.binary().known(toBoolean(plainV));
                List<Computation<SBool>> keyMaterial = seq.binary().known(toBoolean(keyV));
                List<List<Computation<SBool>>> inputs = new ArrayList<>();
                inputs.add(plainText);
                inputs.add(keyMaterial);
                return () -> inputs;
              }).seq((inputs, seq) -> {
                Computation<List<SBool>> l = seq.bristol().DES(inputs.get(0), inputs.get(1));
                return l;
              }).seq((res, seq) -> {
                List<Computation<Boolean>> outputs = new ArrayList<>();
                for (SBool boo : res) {
                  outputs.add(seq.binary().open(() -> boo));
                }
                return () -> outputs;
              }).seq((output, seq) -> {
                return () -> output.stream().map(Computation::out).collect(Collectors.toList());
              });

            }

          };

          List<Boolean> res = secureComputationEngine.runApplication(multApp,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          boolean[] expected = toBoolean(cipherV);
          boolean[] actual = new boolean[res.size()];
          for (int i = 0; i < res.size(); i++) {
            actual[i] = res.get(i);
          }
          if (doAsserts) {
            Assert.assertArrayEquals(expected, actual);
          }
        }
      };
    }
  }
}
