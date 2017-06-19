/*******************************************************************************
 * Copyright (c) 2017 FRESCO (http://github.com/aicis/fresco).
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
package dk.alexandra.fresco.suite.dummy.arithmetic;

import java.math.BigInteger;
import java.util.Random;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePool;
import dk.alexandra.fresco.framework.value.KnownSIntProtocol;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.AddProtocol;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.CloseIntProtocol;
import dk.alexandra.fresco.lib.field.integer.MultProtocol;
import dk.alexandra.fresco.lib.field.integer.OpenIntProtocol;
import dk.alexandra.fresco.lib.field.integer.RandomFieldElementProtocol;
import dk.alexandra.fresco.lib.field.integer.SubtractProtocol;
import dk.alexandra.fresco.lib.helper.SimpleProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionFactory;
import dk.alexandra.fresco.lib.math.integer.inv.LocalInversionProtocol;

public class DummyArithmeticFactory implements BasicNumericFactory, LocalInversionFactory, ExpFromOIntFactory, PreprocessedExpPipeFactory{

  private BigInteger mod;
  private int maxBitLength;
  
  public DummyArithmeticFactory(BigInteger mod, int maxBitLength) {
    this.mod = mod;
    this.maxBitLength = maxBitLength;
  }
  
  @Override
  public SInt getSInt() {
    return new DummyArithmeticSInt();
  }

  @Override
  public SInt getSInt(int i) {
    return new DummyArithmeticSInt(i);
  }

  @Override
  public SInt getSInt(BigInteger i) {
    return new DummyArithmeticSInt(i);
  }

  @Override
  public KnownSIntProtocol getSInt(int i, SInt si) {
    return getSInt(BigInteger.valueOf(i), si);
  }

  @Override
  public KnownSIntProtocol getSInt(BigInteger i, SInt si) {
    return new KnownSIntProtocol() {

      @Override
      public Object getOutputValues() {
        return si;
      }

      @Override
      public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {
        DummyArithmeticSInt x = (DummyArithmeticSInt) si;
        x.setValue(i);
        return EvaluationStatus.IS_DONE;
      }
    };
  }

  @Override
  public OInt getOInt() {
    return new DummyArithmeticOInt();
  }

  @Override
  public OInt getOInt(BigInteger i) {
    return new DummyArithmeticOInt(i);
  }

  @Override
  public OInt getRandomOInt() {
    Random rand = new Random(42);
    return new DummyArithmeticOInt(new BigInteger(23, rand).mod(mod));
  }

  @Override
  public AddProtocol getAddProtocol(SInt a, SInt b, SInt out) {
    return new DummyArithmeticAddProtocol(a, b, out);
  }

  @Override
  public AddProtocol getAddProtocol(SInt input, OInt openInput, SInt out) {
    return new DummyArithmeticAddProtocol(input, openInput, out);
  }

  @Override
  public SubtractProtocol getSubtractProtocol(SInt a, SInt b, SInt out) {
    return new DummyArithmeticSubtractProtocol(a, b, out);
  }

  @Override
  public SubtractProtocol getSubtractProtocol(OInt a, SInt b, SInt out) {
    return new DummyArithmeticSubtractProtocol(a, b, out);
  }

  @Override
  public SubtractProtocol getSubtractProtocol(SInt a, OInt b, SInt out) {
    return new DummyArithmeticSubtractProtocol(a, b, out);
  }

  @Override
  public MultProtocol getMultProtocol(SInt a, SInt b, SInt out) {
    return new DummyArithmeticMultProtocol(a, b, out);
  }

  @Override
  public MultProtocol getMultProtocol(OInt a, SInt b, SInt c) {
    return new DummyArithmeticMultProtocol(b, a, c);
  }

  @Override
  public CloseIntProtocol getCloseProtocol(BigInteger open, SInt closed, int targetID) {
    return new DummyArithmeticCloseProtocol(targetID, getOInt(open), closed);
  }

  @Override
  public CloseIntProtocol getCloseProtocol(int source, OInt open, SInt closed) {
    return new DummyArithmeticCloseProtocol(source, open, closed);
  }

  @Override
  public OpenIntProtocol getOpenProtocol(SInt closed, OInt open) {
    return new OpenIntProtocol() {
      
      @Override
      public Object getOutputValues() {
        return open;
      }
      
      @Override
      public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {
        open.setSerializableContent(closed.getSerializableContent());
        return EvaluationStatus.IS_DONE;
      }
    };
  }

  @Override
  public OpenIntProtocol getOpenProtocol(int target, SInt closed, OInt open) {
    return new OpenIntProtocol() {
      
      @Override
      public Object getOutputValues() {
        return open;
      }
      
      @Override
      public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {
        if(resourcePool.getMyId() == target) {
          open.setSerializableContent(closed.getSerializableContent());
        }
        return EvaluationStatus.IS_DONE;
      }
    };
  }

  @Override
  public ProtocolProducer createRandomSecretSharedBitProtocol(SInt bit) {
    return new SimpleProtocolProducer() {
      
      @Override
      protected ProtocolProducer initializeProtocolProducer() {
        DummyArithmeticSInt x = (DummyArithmeticSInt)bit;
        x.setValue(BigInteger.valueOf(1));
        return new SequentialProtocolProducer();
      }
    };
  }

  @Override
  public RandomFieldElementProtocol getRandomFieldElement(SInt randomElement) {
    return new RandomFieldElementProtocol() {
      
      @Override
      public Object getOutputValues() {
        return randomElement;
      }
      
      @Override
      public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {
        DummyArithmeticSInt x = (DummyArithmeticSInt)randomElement;
        x.setValue(new BigInteger(100, new Random(42)).mod(mod));
        return EvaluationStatus.IS_DONE;
      }
    };
  }

  @Override
  public int getMaxBitLength() {
    return maxBitLength;
  }

  @Override
  public SInt getSqrtOfMaxValue() {
    BigInteger two = BigInteger.valueOf(2);
    BigInteger max = mod.subtract(BigInteger.ONE).divide(two);
    int bitlength = max.bitLength();
    BigInteger approxMaxSqrt = two.pow(bitlength / 2);
    return getSInt(approxMaxSqrt);
  }

  @Override
  public BigInteger getModulus() {
    return mod;
  }

  @Override
  public LocalInversionProtocol getLocalInversionProtocol(OInt x, OInt result) {
    return new LocalInversionProtocol() {
      
      @Override
      public Object getOutputValues() {
        return result;
      }
      
      @Override
      public EvaluationStatus evaluate(int round, ResourcePool resourcePool, SCENetwork network) {
        result.setValue(x.getValue().modInverse(mod));
        return EvaluationStatus.IS_DONE;
      }
    };
  }

  @Override
  public OInt[] getExpFromOInt(OInt value, int maxExp) {
    BigInteger[] Ms = new BigInteger[maxExp];
    Ms[0] = value.getValue();
    for(int i = 1; i < Ms.length; i++){
        Ms[i] = Ms[i-1].multiply(value.getValue()).mod(mod);
    }
    OInt[] expPipe = new OInt[Ms.length];
    for (int i = 0; i < Ms.length; i++) {
      expPipe[i] = new DummyArithmeticOInt(Ms[i]);
    }
    return expPipe;
  }

  @Override
  public SInt[] getExponentiationPipe() {    
    BigInteger value = BigInteger.valueOf(1);
    BigInteger[] Ms = new BigInteger[maxBitLength+1];
    Ms[0] = value.modInverse(mod);
    Ms[1] = value;
    for(int i = 2; i < Ms.length; i++){
        Ms[i] = Ms[i-1].multiply(value).mod(mod);
    }
    SInt[] expPipe = new SInt[Ms.length];
    for (int i = 0; i < Ms.length; i++) {
      expPipe[i] = new DummyArithmeticSInt(Ms[i]);
    }
    return expPipe;
  }

}
