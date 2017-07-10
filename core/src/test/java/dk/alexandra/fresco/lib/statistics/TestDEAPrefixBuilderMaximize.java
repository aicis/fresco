/*
 * Copyright (c) 2015, 2016 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.lib.statistics;

import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolCollectionList;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticFactory;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticProtocol;
import dk.alexandra.fresco.suite.dummy.arithmetic.DummyArithmeticSInt;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestDEAPrefixBuilderMaximize {

  private DEAPrefixBuilderMaximize builder;
  
  @Before
  public void setup(){
    builder = new DEAPrefixBuilderMaximize();
  }

  @Test //This will test the abstract PrefixBuilder class
  public void testAppend() {
    try{
      BasicNumericFactory factory1 = new DummyArithmeticFactory(BigInteger.ONE, 1);
      BasicNumericFactory factory2 = new DummyArithmeticFactory(BigInteger.ONE, 2);
      builder.provider(factory1);
      DEAPrefixBuilder secondBuilder = new DEAPrefixBuilderMaximize();
      secondBuilder.provider(factory2);
      builder.append(secondBuilder);
      Assert.fail("Can not append builder with different provider.");
    } catch (IllegalArgumentException ignored) {
    }
  }
  
  @Test //This will test the abstract PrefixBuilder class
  public void testAppendBasisInputs() {
     BasicNumericFactory factory = new DummyArithmeticFactory(BigInteger.ONE, 1);
     builder.provider(factory);
     DEAPrefixBuilder secondBuilder = new DEAPrefixBuilderMaximize();
     secondBuilder.provider(factory);
     builder.basisInputs(null);
     secondBuilder.basisInputs(null);
     builder.append(secondBuilder);
     Assert.assertNull(builder.getBasisInputs());
     
     builder.basisInputs(new LinkedList<SInt[]>());
     builder.basisInputs.add(new SInt[]{new DummyArithmeticSInt(2)});
     builder.append(secondBuilder);
     Assert.assertEquals(1, builder.basisInputs.size());
     
     secondBuilder.basisInputs(new LinkedList<SInt[]>());
     secondBuilder.basisInputs.add(new SInt[]{new DummyArithmeticSInt(4)});
     builder.append(secondBuilder);
     Assert.assertEquals(2, builder.basisInputs.size());
  }

  @Test //This will test the abstract PrefixBuilder class
  public void testAppendBasisOutputs() {
     BasicNumericFactory factory = new DummyArithmeticFactory(BigInteger.ONE, 1);
     builder.provider(factory);
     DEAPrefixBuilder secondBuilder = new DEAPrefixBuilderMaximize();
     secondBuilder.provider(factory);
     builder.basisOutputs(null);
     secondBuilder.basisOutputs(null);
     builder.append(secondBuilder);
     Assert.assertNull(builder.getBasisOutputs());
     
     builder.basisOutputs(new LinkedList<SInt[]>());
     builder.basisOutputs.add(new SInt[]{new DummyArithmeticSInt(2)});
     builder.append(secondBuilder);
     Assert.assertEquals(1, builder.basisOutputs.size());
     
     secondBuilder.basisOutputs(new LinkedList<SInt[]>());
     secondBuilder.basisOutputs.add(new SInt[]{new DummyArithmeticSInt(4)});
     builder.append(secondBuilder);
     Assert.assertEquals(2, builder.basisOutputs.size());
  }

  @Test //This will test the abstract PrefixBuilder class
  public void testAppendTargetInputs() {
     BasicNumericFactory factory = new DummyArithmeticFactory(BigInteger.ONE, 1);
     builder.provider(factory);
     DEAPrefixBuilder secondBuilder = new DEAPrefixBuilderMaximize();
     secondBuilder.provider(factory);
     builder.targetInputs(null);
     secondBuilder.targetInputs(null);
     builder.append(secondBuilder);
     Assert.assertNull(builder.getTargetInputs());
     
     builder.targetInputs(new LinkedList<SInt>());
     builder.targetInputs.add(new DummyArithmeticSInt(2));
     builder.append(secondBuilder);
     Assert.assertEquals(1, builder.targetInputs.size());
     
     secondBuilder.targetInputs(new LinkedList<SInt>());
     secondBuilder.targetInputs.add(new DummyArithmeticSInt(4));
     builder.append(secondBuilder);
     Assert.assertEquals(2, builder.targetInputs.size());
  }

  @Test //This will test the abstract PrefixBuilder class
  public void testAppendTargetOutputs() {
     BasicNumericFactory factory = new DummyArithmeticFactory(BigInteger.ONE, 1);
     builder.provider(factory);
     DEAPrefixBuilder secondBuilder = new DEAPrefixBuilderMaximize();
     secondBuilder.provider(factory);
     builder.targetOutputs(null);
     secondBuilder.targetOutputs(null);
     builder.append(secondBuilder);
     Assert.assertNull(builder.getTargetOutputs());
     
     builder.targetOutputs(new LinkedList<SInt>());
     builder.targetOutputs.add(new DummyArithmeticSInt(2));
     builder.append(secondBuilder);
     Assert.assertEquals(1, builder.targetOutputs.size());
     
     secondBuilder.targetOutputs(new LinkedList<SInt>());
     secondBuilder.targetOutputs.add(new DummyArithmeticSInt(4));
     builder.append(secondBuilder);
     Assert.assertEquals(2, builder.targetOutputs.size());
  }
  
  @Test //This will test the abstract PrefixBuilder class
  public void testAppendPrefix() {
     BasicNumericFactory factory = new DummyArithmeticFactory(BigInteger.ONE, 1);
     builder.provider(factory);
     DEAPrefixBuilder secondBuilder = new DEAPrefixBuilderMaximize();
     secondBuilder.provider(factory);
     
     secondBuilder.prefix(new DummyProducer("second"));
     
     builder.append(secondBuilder);
     Assert.assertThat(((DummyProducer)builder.prefix).getName(), Is.is("second"));

     builder.prefix(new DummyProducer("first"));
     builder.append(secondBuilder);
     
     ProtocolCollectionList protocolCollection = new ProtocolCollectionList(10);
     builder.prefix.getNextProtocols(protocolCollection);
     Assert.assertEquals(2, protocolCollection.size());
  }
  
  @Test // This will test the abstract PrefixBuilder class
  // This test only covers cases not covered by the standard DEASolver case.
  public void testCopy() {
     BasicNumericFactory factory = new DummyArithmeticFactory(BigInteger.ONE, 1);
     builder.provider(factory);
     
     
     List<SInt> targetInputs = new ArrayList<SInt>();
     targetInputs.add(new DummyArithmeticSInt(1));
     builder.targetInputs(targetInputs);
     
     List<SInt> targetOutputs = new ArrayList<SInt>();
     targetOutputs.add(new DummyArithmeticSInt(2));
     builder.targetOutputs(targetOutputs);
     
     builder.prefix(new DummyProducer("original"));
     
     DEAPrefixBuilder copy = builder.copy();
     
     Assert.assertThat(copy.targetInputs.size(), Is.is(1));
     
     Assert.assertThat(copy.targetOutputs.size(), Is.is(1));
     
     ProtocolCollectionList protocolCollection = new ProtocolCollectionList(10);
     builder.prefix.getNextProtocols(protocolCollection);
     Assert.assertEquals(2, protocolCollection.size());
  }  
  
  @Test //This will test the abstract PrefixBuilder class
  public void testBuildWithInconsistencies() {
    try{
      builder.basisInputs(null);
      builder.build();
      Assert.fail("Can not build when not ready.");
    } catch (IllegalStateException ignored) {
    }

    
    try{
      builder.basisInputs(new ArrayList<>());
      builder.getBasisInputs().add(new SInt[2]);
      builder.build();
      Assert.fail("Can not build on inconsistent data.");
    } catch (IllegalStateException ignored) {
    }
    
    try{
      builder.getTargetInputs().add(new DummyArithmeticSInt());
      builder.getBasisOutputs().add(new SInt[2]);
      builder.build();
      Assert.fail("Can not build on inconsistent data.");
    } catch (IllegalStateException ignored) {
    }

    try{
      builder.getTargetOutputs().add(new DummyArithmeticSInt());
      builder.getBasisInputs().add(new SInt[4]);
      builder.getTargetInputs().add(new DummyArithmeticSInt());
      builder.build();
      Assert.fail("Can not build on inconsistent data.");
    } catch (IllegalStateException ignored) {
    }
    
    try{
      builder.basisInputs(new ArrayList<>());
      builder.getBasisInputs().add(new SInt[2]);
      builder.getTargetInputs().remove(1);
      builder.getTargetOutputs().add(new DummyArithmeticSInt());
      builder.getBasisOutputs().add(new SInt[4]);
      builder.build();
      Assert.fail("Can not build on inconsistent data.");
    } catch (IllegalStateException ignored) {
    }
  }
  
  @Test
  public void testPrefixHandling() {
    ProtocolProducer producer = new DummyProducer("first");
    builder.prefix(producer);
    Assert.assertThat(((DummyProducer)builder.getCircuit()).getName(), Is.is("first"));
    
    builder = new DEAPrefixBuilderMaximize();
    builder.addPrefix(producer);
    Assert.assertThat(((DummyProducer)builder.getCircuit()).getName(), Is.is("first"));
    ProtocolProducer second = new DummyProducer("second");
    
    builder.addPrefix(second);
    ProtocolProducer circuit = builder.getCircuit();
    Assert.assertNotNull(circuit);
    // TODO Introduce better test!
  }

  @Test
  public void testBasisInputHandling() {
    Assert.assertThat(builder.getBasisInputs().size(), Is.is(0));
    builder.addBasisInput(new SInt[2]);
    Assert.assertThat(builder.getBasisInputs().size(), Is.is(1));
    builder.basisInputs(null);
    builder.addBasisInput(new SInt[2]);
    Assert.assertThat(builder.getBasisInputs().size(), Is.is(1));
  }

  @Test
  public void testBasisOutputHandling() {
    Assert.assertThat(builder.getBasisOutputs().size(), Is.is(0));
    builder.addBasisOutput(new SInt[2]);
    Assert.assertThat(builder.getBasisOutputs().size(), Is.is(1));
    builder.basisOutputs(null);
    builder.addBasisOutput(new SInt[2]);
    Assert.assertThat(builder.getBasisOutputs().size(), Is.is(1));
  }

  @Test
  public void testTargetInputHandling() {
    Assert.assertThat(builder.getTargetInputs().size(), Is.is(0));
    builder.addTargetInput(new DummyArithmeticSInt());
    Assert.assertThat(builder.getTargetInputs().size(), Is.is(1));
    builder.targetInputs(null);
    builder.addTargetInput(new DummyArithmeticSInt());
    Assert.assertThat(builder.getTargetInputs().size(), Is.is(1));
  }

  @Test
  public void testTargetOutputHandling() {
    Assert.assertThat(builder.getTargetOutputs().size(), Is.is(0));
    builder.addTargetOutput(new DummyArithmeticSInt());
    Assert.assertThat(builder.getTargetOutputs().size(), Is.is(1));
    builder.targetOutputs(null);
    builder.addTargetOutput(new DummyArithmeticSInt());
    Assert.assertThat(builder.getTargetOutputs().size(), Is.is(1));
  }
  
  private class DummyProducer implements ProtocolProducer {

    private final String name;

    DummyProducer(String name) {
      this.name = name;
    }

    String getName() {
      return name;
    }
    
    @Override
    public void getNextProtocols(ProtocolCollection protocolCollection) {
      protocolCollection.addProtocol(new DummyArithmeticProtocol() {
        
        @Override
        public Object getOutputValues() {
          // TODO Auto-generated method stub
          return null;
        }
        
        @Override
        public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {
          // TODO Auto-generated method stub
          return null;
        }
      });
    }

    @Override
    public boolean hasNextProtocols() {
      // TODO Auto-generated method stub
      return false;
    }
    
  }

  
}
