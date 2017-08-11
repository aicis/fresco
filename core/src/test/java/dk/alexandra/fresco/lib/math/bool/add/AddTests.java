/*
 * Copyright (c) 2016 FRESCO (http://github.com/aicis/fresco).
 *
 * This file is part of the FRESCO project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * FRESCO uses SCAPI - http://crypto.biu.ac.il/SCAPI, Crypto++, Miracl, NTL,
 * and Bouncy Castle. Please see these projects for any further licensing issues.
 *******************************************************************************/
package dk.alexandra.fresco.lib.math.bool.add;

import dk.alexandra.fresco.framework.Application;
import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.TestBoolApplication;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThread;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadConfiguration;
import dk.alexandra.fresco.framework.TestThreadRunner.TestThreadFactory;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderNumeric.SequentialNumericBuilder;
import dk.alexandra.fresco.framework.builder.binary.BuilderFactoryBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary.SequentialBinaryBuilder;
import dk.alexandra.fresco.framework.network.ResourcePoolCreator;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.util.ByteArithmetic;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.SingleProtocolProducer;
import dk.alexandra.fresco.lib.field.bool.generic.GenericBinaryBuilderAdvanced;
import dk.alexandra.fresco.lib.helper.SequentialProtocolProducer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.core.Is;
import org.junit.Assert;

public class AddTests {

  public static class TestOnebitHalfAdder<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestOnebitHalfAdder() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
            Application<List<Boolean>, ProtocolBuilderBinary> app =
                new Application<List<Boolean>, ProtocolBuilderBinary>() {

                  @Override
                  public Computation<List<Boolean>> prepareApplication(
                      ProtocolBuilderBinary producer) {
                    
                    List<Computation<Pair<SBool, SBool>>> data = new ArrayList<Computation<Pair<SBool, SBool>>>();
                    
                    SequentialBinaryBuilder builder = (SequentialBinaryBuilder)producer;
                    
                    return builder.seq( seq -> {
                      GenericBinaryBuilderAdvanced prov = new GenericBinaryBuilderAdvanced(seq);
                      Computation<SBool> inp0 = seq.binary().known(false);
                      Computation<SBool> inp1 = seq.binary().known(true);
                      data.add(prov.oneBitHalfAdder(inp0, inp0));  
                      data.add(prov.oneBitHalfAdder(inp0, inp1));
                      data.add(prov.oneBitHalfAdder(inp1, inp0));
                      data.add(prov.oneBitHalfAdder(inp1, inp1));
                        return () -> data;
                      }
                    ).seq( (dat, seq) -> {
                      List<Computation<Boolean>> out = new ArrayList<Computation<Boolean>>();
                      for(Computation<Pair<SBool, SBool>> o : dat) {
                       out.add(seq.binary().open(o.out().getFirst()));
                       out.add(seq.binary().open(o.out().getSecond()));
                      }
                      return () -> out.stream().map(Computation::out).collect(Collectors.toList());
                      }
                    );
                  }
          };

          
          List<Boolean> outputs = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          Assert.assertThat(outputs.get(0), Is.is(false));
          Assert.assertThat(outputs.get(1), Is.is(false));
          Assert.assertThat(outputs.get(2), Is.is(true));
          Assert.assertThat(outputs.get(3), Is.is(false));
          Assert.assertThat(outputs.get(4), Is.is(true));
          Assert.assertThat(outputs.get(5), Is.is(false));
          Assert.assertThat(outputs.get(6), Is.is(false));
          Assert.assertThat(outputs.get(7), Is.is(true));
        }
      };
    }
  }
  
  public static class TestOnebitFullAdder<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestOnebitFullAdder() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        @Override
        public void test() throws Exception {
            Application<List<Boolean>, ProtocolBuilderBinary> app =
                new Application<List<Boolean>, ProtocolBuilderBinary>() {

                  @Override
                  public Computation<List<Boolean>> prepareApplication(
                      ProtocolBuilderBinary producer) {
                    
                    List<Computation<Pair<SBool, SBool>>> data = new ArrayList<Computation<Pair<SBool, SBool>>>();
                    
                    SequentialBinaryBuilder builder = (SequentialBinaryBuilder)producer;
                    
                    return builder.seq( seq -> {
                      GenericBinaryBuilderAdvanced prov = new GenericBinaryBuilderAdvanced(seq);
                      Computation<SBool> inp0 = seq.binary().known(false);
                      Computation<SBool> inp1 = seq.binary().known(true);
                      data.add(prov.oneBitFullAdder(inp0, inp0, inp0));  
                      data.add(prov.oneBitFullAdder(inp0, inp0, inp1));
                      data.add(prov.oneBitFullAdder(inp0, inp1, inp0));
                      data.add(prov.oneBitFullAdder(inp0, inp1, inp1));
                      data.add(prov.oneBitFullAdder(inp1, inp0, inp0));
                      data.add(prov.oneBitFullAdder(inp1, inp0, inp1));
                      data.add(prov.oneBitFullAdder(inp1, inp1, inp0));
                      data.add(prov.oneBitFullAdder(inp1, inp1, inp1));
                        return () -> data;
                      }
                    ).seq( (dat, seq) -> {
                      List<Computation<Boolean>> out = new ArrayList<Computation<Boolean>>();
                      for(Computation<Pair<SBool, SBool>> o : dat) {
                       out.add(seq.binary().open(o.out().getFirst()));
                       out.add(seq.binary().open(o.out().getSecond()));
                      }
                      return () -> out.stream().map(Computation::out).collect(Collectors.toList());
                      }
                    );
                  }
          };
          
          List<Boolean> outputs = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
          Assert.assertThat(outputs.get(0), Is.is(false)); //000
          Assert.assertThat(outputs.get(1), Is.is(false)); //000
          Assert.assertThat(outputs.get(2), Is.is(true));  //001
          Assert.assertThat(outputs.get(3), Is.is(false)); //001
          Assert.assertThat(outputs.get(4), Is.is(true));  //010
          Assert.assertThat(outputs.get(5), Is.is(false)); //010
          Assert.assertThat(outputs.get(6), Is.is(false));  //011
          Assert.assertThat(outputs.get(7), Is.is(true));  //011
          Assert.assertThat(outputs.get(8), Is.is(true)); //100
          Assert.assertThat(outputs.get(9), Is.is(false)); //100
          Assert.assertThat(outputs.get(10), Is.is(false)); //101
          Assert.assertThat(outputs.get(11), Is.is(true)); //101
          Assert.assertThat(outputs.get(12), Is.is(false)); //110
          Assert.assertThat(outputs.get(13), Is.is(true)); //110
          Assert.assertThat(outputs.get(14), Is.is(true)); //111
          Assert.assertThat(outputs.get(15), Is.is(true)); //111
        }
      };
    }
  }
  
  public static class TestFullAdder<ResourcePoolT extends ResourcePool>
  extends TestThreadFactory<ResourcePoolT, ProtocolBuilderBinary> {

    public TestFullAdder() {
    }

    @Override
    public TestThread<ResourcePoolT, ProtocolBuilderBinary> next(
        TestThreadConfiguration<ResourcePoolT, ProtocolBuilderBinary> conf) {
      return new TestThread<ResourcePoolT, ProtocolBuilderBinary>() {

        List<Boolean> rawFirst = Arrays.asList(ByteArithmetic.toBoolean("11"));
        List<Boolean> rawSecond = Arrays.asList(ByteArithmetic.toBoolean("22"));

        
        @Override
        public void test() throws Exception {
            Application<List<Boolean>, ProtocolBuilderBinary> app =
                new Application<List<Boolean>, ProtocolBuilderBinary>() {

                  @Override
                  public Computation<List<Boolean>> prepareApplication(
                      ProtocolBuilderBinary producer) {
                    
                    List<Computation<Pair<SBool, SBool>>> data = new ArrayList<Computation<Pair<SBool, SBool>>>();
                    
                    //GenericBinaryBuilderAdvanced advancedBuilder = new GenericBinaryBuilderAdvanced(producer);
                    SequentialBinaryBuilder builder = (SequentialBinaryBuilder)producer;
                    
                    return builder.seq( seq -> {
                      GenericBinaryBuilderAdvanced prov = new GenericBinaryBuilderAdvanced(seq);
                      Computation<SBool> carry = seq.binary().known(true);
                      
                      List<Computation<SBool>> first = rawFirst.stream().map(seq.binary()::known)
                          .collect(Collectors.toList());
                      List<Computation<SBool>> second = rawSecond.stream().map(seq.binary()::known)
                          .collect(Collectors.toList());
                      
                      Computation<List<Computation<SBool>>> adder = prov.fullAdder(first, second, carry); 
                      
                        return () -> adder.out();
                      }
                    ).seq( (dat, seq) -> {
                      List<Computation<Boolean>> out = new ArrayList<Computation<Boolean>>();
                  //    System.out.println("dat: " +dat + "out ");
                  //    for(Computation<SBool> o : dat.out()) {
                  //     out.add(seq.binary().open(o.out()));
                  //    }
                      return () -> out.stream().map(Computation::out).collect(Collectors.toList());
                      }
                    );
                  }
          };
          
          List<Boolean> outputs = secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));
     //     Assert.assertThat(outputs.get(0), Is.is(false)); //000
        }
      };
    }
  }
  /*
  public static class TestFullAdder extends TestThreadFactory {

    public TestFullAdder() {
    }
    
    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
          
          boolean[] rawFirst = ByteArithmetic.toBoolean("11");
          boolean[] rawSecond = ByteArithmetic.toBoolean("22");
                
          TestBoolApplication app = new TestBoolApplication() {

            private static final long serialVersionUID = 4338818809103728010L;

            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              AbstractBinaryFactory prov = (AbstractBinaryFactory) producer;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
          
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              
              SBool[] first = builder.knownSBool(rawFirst);
              SBool[] second = builder.knownSBool(rawSecond);
              SBool carry = builder.knownSBool(false);
              SBool[] result = prov.getSBools(8); 
              SBool outCarry = prov.getSBool();
              
              seq.append(builder.getProtocol());
              seq.append(prov.getFullAdderProtocol(first, second, carry, result, outCarry));
              OBool[] open = builder.output(result);
              OBool openedCarry = builder.output(outCarry);
              seq.append(builder.getProtocol());
              this.outputs = new OBool[open.length+1];
              for(int i = 0; i< open.length; i++) {
                this.outputs[i] = open[i];
              }
              this.outputs[open.length] = openedCarry;
              return seq;
            }
          };

          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          boolean[] raw = convert(app.getOutputs());
          
          boolean[] value = new boolean[raw.length-1];
          System.arraycopy(raw, 0, value, 0, raw.length-1);
          Assert.assertThat(raw[raw.length-1], Is.is(false));    
          Assert.assertThat(ByteArithmetic.toHex(value), Is.is("33"));
        }
      };
    }
    private boolean[] convert(OBool[] outputs) {
      boolean[] output = new boolean[outputs.length];
      for(int i = 0; i< outputs.length; i++) {
        output[i] = outputs[i].getValue();
      }
      return output;
    }
  }*/

  public static class TestBitIncrement extends TestThreadFactory {

    public TestBitIncrement() {
    }
    
    @Override
    public TestThread next(TestThreadConfiguration conf) {
      return new TestThread() {
        @Override
        public void test() throws Exception {
    /*      
          boolean[] rawSmall = ByteArithmetic.toBoolean("11");
          boolean[] rawBig = ByteArithmetic.toBoolean("ff");
          
          
          OBool[] res1 = new OBool[8];
          OBool[] res2 = new OBool[8];
          OBool[] res3 = new OBool[9];
          OBool[] res4 = new OBool[9];
          
          TestBoolApplication app = new TestBoolApplication() {

            private static final long serialVersionUID = 4338818809103728010L;

            @Override
            public ProtocolProducer prepareApplication(
                BuilderFactory factoryProducer) {
              ProtocolFactory producer = factoryProducer.getProtocolFactory();
              AbstractBinaryFactory prov = (AbstractBinaryFactory) producer;
              BasicLogicBuilder builder = new BasicLogicBuilder(prov);
          
              SequentialProtocolProducer seq = new SequentialProtocolProducer();
              
              SBool[] small = builder.knownSBool(rawSmall);
              SBool[] big = builder.knownSBool(rawBig);
              SBool noIncrement = builder.knownSBool(false);
              SBool withIncrement = builder.knownSBool(true);
              
              SBool[] result = prov.getSBools(8); 
              SBool[] result2 = prov.getSBools(8);
              SBool[] result3 = prov.getSBools(9);
              SBool[] result4 = prov.getSBools(9);
              
        
              seq.append(builder.getProtocol());
              seq.append(prov.getBitIncrementerProtocol(small, noIncrement, result));
              seq.append(prov.getBitIncrementerProtocol(small, withIncrement, result2));

              seq.append(prov.getBitIncrementerProtocol(big, noIncrement, result3));
              seq.append(prov.getBitIncrementerProtocol(big, withIncrement, result4));

              
              OBool[] openFirst = builder.output(result);
              OBool[] openSecond = builder.output(result2);
              OBool[] openThird = builder.output(result3);
              OBool[] openFourth = builder.output(result4);
              seq.append(builder.getProtocol());
              for(int i = 0; i< openFirst.length; i++) {
                res1[i] = openFirst[i];
                res2[i] = openSecond[i];
                res3[i] = openThird[i];
                res4[i] = openFourth[i];
              }
              res3[8] = openThird[8];
              res4[8] = openFourth[8];
              return seq;
            }
          };

          secureComputationEngine.runApplication(app,
              ResourcePoolCreator.createResourcePool(conf.sceConf));

          boolean[] raw1 = convert(res1);
          boolean[] raw2 = convert(res2);
          boolean[] raw3 = convert(res3);
          boolean[] raw4 = convert(res4);
          Assert.assertThat(ByteArithmetic.toHex(raw1), Is.is("11"));    
          Assert.assertThat(ByteArithmetic.toHex(raw2), Is.is("12"));
          Assert.assertThat(ByteArithmetic.toHex(raw3), Is.is("00ff"));
          Assert.assertThat(ByteArithmetic.toHex(raw4), Is.is("0100"));*/
        }
      };
    }
  }
  
  
}
