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

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.helper.bristol.BristolCircuitParser;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;

/**
 * Tests for invalid circuits
 * 
 */
public class BadBristolCryptoTests {


  public static class XorTest1<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public XorTest1() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<SBool>, ProtocolBuilderBinary> multApp =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> inp1 = new ArrayList<>();
                List<DRes<SBool>> inp2 = new ArrayList<>();
                inp1.add(null);
                inp2.add(null);

                DRes<List<SBool>> l = seq.seq(BristolCircuitParser
                    .readCircuitDescription("circuits/XOR.txt", inp1, inp2));
                return l;
              });
              try{
                runApplication(multApp);
                Assert.fail();
              } catch(RuntimeException e) {
                Assert.assertEquals(e.getCause().getClass(), MPCException.class);
              }

        }
      };
    }
  }

  public static class XorTest2<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public XorTest2() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<SBool>, ProtocolBuilderBinary> multApp =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> inp1 = new ArrayList<>();
                List<DRes<SBool>> inp2 = new ArrayList<>();
                inp1.add(() -> null);
                inp2.add(null);

                DRes<List<SBool>> l = seq.seq(BristolCircuitParser
                    .readCircuitDescription("circuits/XOR.txt", inp1, inp2));
                return l;
              });
              try{
                runApplication(multApp);
                Assert.fail();
              } catch(RuntimeException e) {
                Assert.assertEquals(e.getCause().getClass(), MPCException.class);
              }

        }
      };
    }
  }

  public static class XorTest3<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public XorTest3() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<SBool>, ProtocolBuilderBinary> multApp =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> inp1 = new ArrayList<>();
                List<DRes<SBool>> inp2 = new ArrayList<>();
                inp1.add(() -> null);
                inp2.add(null);

                DRes<List<SBool>> l = seq.seq(BristolCircuitParser
                    .readCircuitDescription("circuits/invalid-XOR.txt", inp1, inp2));
                return l;
              });
              try{
                runApplication(multApp);
                Assert.fail();
              } catch(RuntimeException e) {
                e.printStackTrace();
                Assert.assertEquals(e.getCause().getClass(), MPCException.class);
              }

        }
      };
    }
  }

  public static class XorTest4<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public XorTest4() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<SBool>, ProtocolBuilderBinary> multApp =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> inp1 = new ArrayList<>();
                List<DRes<SBool>> inp2 = new ArrayList<>();
                inp1.add(() -> null);
                inp2.add(null);

                DRes<List<SBool>> l = seq.seq(BristolCircuitParser
                    .readCircuitDescription("circuits/invalid-XOR2.txt", inp1, inp2));
                return l;
              });
              try{
                runApplication(multApp);
                Assert.fail();
              } catch(RuntimeException e) {
                e.printStackTrace();
                Assert.assertEquals(e.getCause().getClass(), MPCException.class);
              }

        }
      };
    }
  }


  public static class XorTest5<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public XorTest5() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<SBool>, ProtocolBuilderBinary> multApp =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> inp1 = new ArrayList<>();
                List<DRes<SBool>> inp2 = new ArrayList<>();
                inp1.add(() -> null);
                inp2.add(null);

                DRes<List<SBool>> l = seq.seq(BristolCircuitParser
                    .readCircuitDescription("circuits/invalid-XOR3.txt", inp1, inp2));
                return l;
              });
              try{
                runApplication(multApp);
                Assert.fail();
              } catch(RuntimeException e) {
                Assert.assertEquals(e.getCause().getClass(), MPCException.class);
              }
        }
      };
    }
  }


  public static class AndTest1<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public AndTest1() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<SBool>, ProtocolBuilderBinary> multApp =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> inp1 = new ArrayList<>();
                List<DRes<SBool>> inp2 = new ArrayList<>();
                inp1.add(null);
                inp2.add(null);

                DRes<List<SBool>> l = seq.seq(BristolCircuitParser
                    .readCircuitDescription("circuits/AND.txt", inp1, inp2));
                return l;
              });
              try{
                runApplication(multApp);
                Assert.fail();
              } catch(RuntimeException e) {
                Assert.assertEquals(e.getCause().getClass(), MPCException.class);
              }
        }
      };
    }
  }

  public static class AndTest2<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public AndTest2() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<SBool>, ProtocolBuilderBinary> multApp =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> inp1 = new ArrayList<>();
                List<DRes<SBool>> inp2 = new ArrayList<>();
                inp1.add(() -> null);
                inp2.add(null);

                DRes<List<SBool>> l = seq.seq(BristolCircuitParser
                    .readCircuitDescription("circuits/AND.txt", inp1, inp2));
                return l;
              });
              try{
                runApplication(multApp);
                Assert.fail();
              } catch(RuntimeException e) {
                Assert.assertEquals(e.getCause().getClass(), MPCException.class);
              }
        }
      };
    }
  }

  public static class AndTest3<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public AndTest3() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<SBool>, ProtocolBuilderBinary> multApp =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> inp1 = new ArrayList<>();
                List<DRes<SBool>> inp2 = new ArrayList<>();
                inp1.add(() -> null);
                inp2.add(null);

                DRes<List<SBool>> l = seq.seq(BristolCircuitParser
                    .readCircuitDescription("circuits/invalid-AND.txt", inp1, inp2));
                return l;
              });
              try{
                runApplication(multApp);
                Assert.fail();
              } catch(RuntimeException e) {
                Assert.assertEquals(e.getCause().getClass(), MPCException.class);
              }
        }
      };
    }
  }

  public static class AndTest4<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public AndTest4() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<SBool>, ProtocolBuilderBinary> multApp =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> inp1 = new ArrayList<>();
                List<DRes<SBool>> inp2 = new ArrayList<>();
                inp1.add(() -> null);
                inp2.add(null);

                DRes<List<SBool>> l = seq.seq(BristolCircuitParser
                    .readCircuitDescription("circuits/invalid-AND2.txt", inp1, inp2));
                return l;
              });
              try{
                runApplication(multApp);
                Assert.fail();
              } catch(RuntimeException e) {
                Assert.assertEquals(e.getCause().getClass(), MPCException.class);
              }
        }
      };
    }
  }


  public static class AndTest5<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public AndTest5() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<SBool>, ProtocolBuilderBinary> multApp =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> inp1 = new ArrayList<>();
                List<DRes<SBool>> inp2 = new ArrayList<>();
                inp1.add(() -> null);
                inp2.add(null);

                DRes<List<SBool>> l = seq.seq(BristolCircuitParser
                    .readCircuitDescription("circuits/invalid-AND3.txt", inp1, inp2));
                return l;
              });
              try{
                runApplication(multApp);
                Assert.fail();
              } catch(RuntimeException e) {
                Assert.assertEquals(e.getCause().getClass(), MPCException.class);
              }
        }
      };
    }
  }

  public static class InvTest1<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public InvTest1() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<SBool>, ProtocolBuilderBinary> multApp =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> inp1 = new ArrayList<>();
                List<DRes<SBool>> inp2 = new ArrayList<>();
                inp2.add(null);
                inp1.add(null);

                DRes<List<SBool>> l = seq.seq(BristolCircuitParser
                    .readCircuitDescription("circuits/INV.txt", inp1, inp2));
                return l;
              });
              try{
                runApplication(multApp);
                Assert.fail();
              } catch(RuntimeException e) {
                Assert.assertEquals(e.getCause().getClass(), MPCException.class);
              }
        }
      };
    }
  }

  public static class InvTest2<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public InvTest2() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<SBool>, ProtocolBuilderBinary> multApp =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> inp1 = new ArrayList<>();
                List<DRes<SBool>> inp2 = new ArrayList<>();
                inp1.add(() -> null);
                inp2.add(null);

                DRes<List<SBool>> l = seq.seq(BristolCircuitParser
                    .readCircuitDescription("circuits/invalid-INV.txt", inp1, inp2));
                return l;
              });
              try{
                runApplication(multApp);
                Assert.fail();
              } catch(RuntimeException e) {
                Assert.assertEquals(e.getCause().getClass(), MPCException.class);
              }
        }
      };
    }
  }

  public static class InvTest3<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public InvTest3() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<SBool>, ProtocolBuilderBinary> multApp =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> inp1 = new ArrayList<>();
                List<DRes<SBool>> inp2 = new ArrayList<>();
                inp1.add(() -> null);
                inp2.add(null);

                DRes<List<SBool>> l = seq.seq(BristolCircuitParser
                    .readCircuitDescription("circuits/invalid-INV2.txt", inp1, inp2));
                return l;
              });
              try{
                runApplication(multApp);
                Assert.fail();
              } catch(RuntimeException e) {
                Assert.assertEquals(e.getCause().getClass(), MPCException.class);
              }
        }
      };
    }
  }


  public static class InvTest4<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public InvTest4() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<SBool>, ProtocolBuilderBinary> multApp =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> inp1 = new ArrayList<>();
                List<DRes<SBool>> inp2 = new ArrayList<>();
                inp1.add(() -> null);
                inp2.add(null);

                DRes<List<SBool>> l = seq.seq(BristolCircuitParser
                    .readCircuitDescription("circuits/invalid-INV3.txt", inp1, inp2));
                return l;
              });
              try{
                runApplication(multApp);
                Assert.fail();
              } catch(RuntimeException e) {
                Assert.assertEquals(e.getCause().getClass(), MPCException.class);
              }
        }
      };
    }
  }

  public static class NoCircuitTest<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public NoCircuitTest() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<SBool>, ProtocolBuilderBinary> multApp =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> inp1 = new ArrayList<>();
                List<DRes<SBool>> inp2 = new ArrayList<>();
                inp1.add(() -> null);
                inp2.add(null);

                DRes<List<SBool>> l = seq.seq(BristolCircuitParser
                    .readCircuitDescription("circuits/No-Circuit.txt", inp1, inp2));
                return l;
              });
              try{
                runApplication(multApp);
                Assert.fail();
              } catch(RuntimeException e) {
                Assert.assertEquals(e.getCause().getClass(), MPCException.class);
              }
        }
      };
    }
  }

  public static class BadOperationTest<ResourcePoolT extends ResourcePool>
    extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public BadOperationTest() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next() {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
          Application<List<SBool>, ProtocolBuilderBinary> multApp =
              producer -> producer.seq(seq -> {
                List<DRes<SBool>> inp1 = new ArrayList<>();
                List<DRes<SBool>> inp2 = new ArrayList<>();
                inp1.add(() -> null);
                inp2.add(null);

                String circuit = "1 4\n1 1 1\n\n2 2 0 1 2 3 BAD";
                
                StringReader reader = new StringReader(circuit);
                
                BristolCircuitParser parser = new BristolCircuitParser(new BufferedReader(reader).lines(), inp1, inp2);
                DRes<List<SBool>> l = seq.seq(parser);
                return l;
              });
              try{
                runApplication(multApp);
                Assert.fail();
              } catch(RuntimeException e) {
                e.printStackTrace();
                Assert.assertEquals(e.getCause().getClass(), MPCException.class);
              }
        }
      };
    }
  }
  
}
