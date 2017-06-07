/*******************************************************************************
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
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestDEAInputEfficiencyPrefixBuilder {

  private List<List<SInt>> inputValues = new ArrayList<>();
  private List<List<SInt>> outputValues = new ArrayList<>();
  private List<List<SInt>> inputBasis = new ArrayList<>();
  private List<List<SInt>> outputBasis = new ArrayList<>();
  
  private DEAInputEfficiencyPrefixBuilder builder;
  
  @Before
  public void setup(){
    builder = new DEAInputEfficiencyPrefixBuilder();

    inputValues = new ArrayList<>();
    outputValues = new ArrayList<>();
    inputBasis = new ArrayList<>();
    outputBasis = new ArrayList<>();
    inputValues.add(new ArrayList<>());
    outputValues.add(new ArrayList<>());
    inputBasis.add(new ArrayList<>());
    outputBasis.add(new ArrayList<>());
  }
  
  @Test
  public void testBuildWithInconsistencies() {
    try{
      builder.basisInputs(null);
      builder.build();
      Assert.fail("Can not build when not ready.");
    } catch(IllegalStateException e) {
    }
    try{
      builder.basisInputs(new ArrayList<>());
      builder.getBasisInputs().add(new SInt[2]);
      builder.build();
      Assert.fail("Can not build on incosistent data.");
    }catch(IllegalStateException e) {
      
    }
  }
  
  @Test
  public void testPrefixHandling() {
    ProtocolProducer producer = new DummyProducer("first");
    builder.prefix(producer);
    Assert.assertThat(((DummyProducer)builder.getCircuit()).getName(), Is.is("first"));
    
    builder = new DEAInputEfficiencyPrefixBuilder();
    builder.addPrefix(producer);
    Assert.assertThat(((DummyProducer)builder.getCircuit()).getName(), Is.is("first"));
    ProtocolProducer second = new DummyProducer("second");
    
    builder.addPrefix(second);
    ParallelProtocolProducer par = (ParallelProtocolProducer) builder.getCircuit();
    List<String> producerNames = new ArrayList<>();
    for(ProtocolProducer prod :  par.getNextProtocolProducerLevel()) {
      producerNames.add(((DummyProducer)prod).getName());
    }
    Assert.assertThat(producerNames, IsCollectionContaining.hasItems("first", "second"));
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
    builder.addTargetInput(new DummySInt());
    Assert.assertThat(builder.getTargetInputs().size(), Is.is(1));
    builder.targetInputs(null);
    builder.addTargetInput(new DummySInt());
    Assert.assertThat(builder.getTargetInputs().size(), Is.is(1));
  }

  @Test
  public void testTargetOutputHandling() {
    Assert.assertThat(builder.getTargetOutputs().size(), Is.is(0));
    builder.addTargetOutput(new DummySInt());
    Assert.assertThat(builder.getTargetOutputs().size(), Is.is(1));
    builder.targetOutputs(null);
    builder.addTargetOutput(new DummySInt());
    Assert.assertThat(builder.getTargetOutputs().size(), Is.is(1));
  }
  
  private class DummyProducer implements ProtocolProducer {

    private final String name;
    
    public DummyProducer(String name) {
      this.name = name;
    }
    
    public String getName() {
      return name;
    }
    
    @Override
    public void getNextProtocols(ProtocolCollection protocolCollection) {
    }

    @Override
    public boolean hasNextProtocols() {
      // TODO Auto-generated method stub
      return false;
    }
    
  }
  
    
  @SuppressWarnings("serial")
  private class DummySInt implements SInt{
    public DummySInt() {
      
    }

    @Override
    public byte[] getSerializableContent() {
      return null;
    }

    @Override
    public void setSerializableContent(byte[] val) {
    }

    @Override
    public boolean isReady() {
      return false;
    }
  }
  
}
