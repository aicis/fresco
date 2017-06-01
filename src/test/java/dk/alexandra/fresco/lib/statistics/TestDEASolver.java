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

import java.util.ArrayList;
import java.util.List;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dk.alexandra.fresco.framework.MPCException;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.statistics.DEASolver.AnalysisType;

public class TestDEASolver {

  private List<List<SInt>> inputValues = new ArrayList<List<SInt>>(); 
  private List<List<SInt>> outputValues = new ArrayList<List<SInt>>();
  private List<List<SInt>> inputBasis = new ArrayList<List<SInt>>(); 
  private List<List<SInt>> outputBasis = new ArrayList<List<SInt>>();
  
  @Before
  public void setup(){
    inputValues = new ArrayList<List<SInt>>(); 
    outputValues = new ArrayList<List<SInt>>();
    inputBasis = new ArrayList<List<SInt>>(); 
    outputBasis = new ArrayList<List<SInt>>();
    inputValues.add(new ArrayList<SInt>());
    outputValues.add(new ArrayList<SInt>());
    inputBasis.add(new ArrayList<SInt>());
    outputBasis.add(new ArrayList<SInt>());
  }
  
  @Test
  public void testConsistentData() {
    try{
      new DEASolver(DEASolver.AnalysisType.INPUT_EFFICIENCY, inputValues, outputValues, inputBasis, outputBasis);
    } catch(MPCException e) {
      Assert.fail("Consistent data should be accepted");
    }
  }

  @Test
  public void testBasisMismatch() {
    inputBasis.add(new ArrayList<SInt>());
    
    try{
      new DEASolver(DEASolver.AnalysisType.INPUT_EFFICIENCY, inputValues, outputValues, inputBasis, outputBasis);
      Assert.fail("Inconsistent data should not be accepted");
    } catch(MPCException e) {
      Assert.assertThat(e.getMessage(), Is.is("Inconsistent dataset / query data"));
    }
  }

  @Test
  public void testQueryMismatch() {
    inputValues.add(new ArrayList<SInt>());
    
    try{
      new DEASolver(DEASolver.AnalysisType.INPUT_EFFICIENCY, inputValues, outputValues, inputBasis, outputBasis);
      Assert.fail("Inconsistent data should not be accepted");
    } catch(MPCException e) {
      Assert.assertThat(e.getMessage(), Is.is("Inconsistent dataset / query data"));
    }
  }

  @Test
  public void testInputMismatchWithBasis() {
    inputValues.get(0).add(new DummySInt());
    inputBasis.get(0).add(new DummySInt());
    inputBasis.get(0).add(new DummySInt());
    
    try{
      new DEASolver(DEASolver.AnalysisType.INPUT_EFFICIENCY, inputValues, outputValues, inputBasis, outputBasis);
      Assert.fail("Inconsistent data should not be accepted");
    } catch(MPCException e) {
      Assert.assertThat(e.getMessage(), Is.is("Inconsistent dataset / query data"));
    }
  }

  @Test
  public void testOutputMismatchWithBasis() {
    outputValues.get(0).add(new DummySInt());
    outputBasis.get(0).add(new DummySInt());
    outputBasis.get(0).add(new DummySInt());
    
    try{
      new DEASolver(DEASolver.AnalysisType.INPUT_EFFICIENCY, inputValues, outputValues, inputBasis, outputBasis);
      Assert.fail("Inconsistent data should not be accepted");
    } catch(MPCException e) {
      Assert.assertThat(e.getMessage(), Is.is("Inconsistent dataset / query data"));
    }
  }

  @Test
  public void testInconsistentInputBasis() {
    outputBasis.add(new ArrayList<SInt>());
    inputValues.get(0).add(new DummySInt());
    inputValues.get(0).add(new DummySInt());
    inputBasis.add(new ArrayList<SInt>());
    inputBasis.get(0).add(new DummySInt());
    inputBasis.get(0).add(new DummySInt());
    inputBasis.get(1).add(new DummySInt());
    inputBasis.get(1).add(new DummySInt());
    inputBasis.get(1).add(new DummySInt());
    
    try{
      new DEASolver(DEASolver.AnalysisType.INPUT_EFFICIENCY, inputValues, outputValues, inputBasis, outputBasis);
      Assert.fail("Inconsistent data should not be accepted");
    } catch(MPCException e) {
      Assert.assertThat(e.getMessage(), Is.is("Inconsistent dataset / query data"));
    }
  }
  
  @Test
  public void testInconsistentOutputBasis() {
    inputBasis.add(new ArrayList<SInt>());
    outputValues.get(0).add(new DummySInt());
    outputValues.get(0).add(new DummySInt());
    outputBasis.add(new ArrayList<SInt>());
    outputBasis.get(0).add(new DummySInt());
    outputBasis.get(0).add(new DummySInt());
    outputBasis.get(1).add(new DummySInt());
    outputBasis.get(1).add(new DummySInt());
    outputBasis.get(1).add(new DummySInt());
    
    try{
      new DEASolver(DEASolver.AnalysisType.INPUT_EFFICIENCY, inputValues, outputValues, inputBasis, outputBasis);
      Assert.fail("Inconsistent data should not be accepted");
    } catch(MPCException e) {
      Assert.assertThat(e.getMessage(), Is.is("Inconsistent dataset / query data"));
    }
  }
  
  @Test
  public void testIncosistentOutputValues() {
    outputBasis.get(0).add(new DummySInt());
    outputBasis.get(0).add(new DummySInt());
    outputValues.get(0).add(new DummySInt());
    outputValues.get(0).add(new DummySInt());
    outputValues.get(0).add(new DummySInt());
    
    try{
      new DEASolver(DEASolver.AnalysisType.INPUT_EFFICIENCY, inputValues, outputValues, inputBasis, outputBasis);
      Assert.fail("Inconsistent data should not be accepted");
    } catch(MPCException e) {
      Assert.assertThat(e.getMessage(), Is.is("Inconsistent dataset / query data"));
    }
  }
  
  
  @Test
  public void testAnalysisType() {
    Assert.assertThat(DEASolver.AnalysisType.INPUT_EFFICIENCY.toString(), Is.is("INPUT_EFFICIENCY"));
    Assert.assertThat(DEASolver.AnalysisType.valueOf("INPUT_EFFICIENCY"), Is.is(AnalysisType.INPUT_EFFICIENCY));
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
