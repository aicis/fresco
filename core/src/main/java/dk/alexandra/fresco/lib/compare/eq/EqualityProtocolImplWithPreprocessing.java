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
package dk.alexandra.fresco.lib.compare.eq;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolCollection;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.MultByConstantFactory;
import dk.alexandra.fresco.lib.field.integer.generic.AddProtocolFactory;
import dk.alexandra.fresco.lib.helper.ParallelProtocolProducer;
import dk.alexandra.fresco.lib.helper.sequential.SequentialProtocolProducer;
import dk.alexandra.fresco.lib.math.integer.HammingDistanceFactory;
import dk.alexandra.fresco.lib.math.integer.NumericBitFactory;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactory;

/**
 * @author ttoft
 */
public class EqualityProtocolImplWithPreprocessing implements EqualityProtocol {

  // parameters
  private final int bitLength, securityParam;

  // variables for input/output
  private final SInt x, y, result;

  // Factories
  private final BasicNumericFactory<SInt> factory;
  private final MultByConstantFactory mbcFactory;
  //	private final NumericBitProvider bitProvider;
  private final PreprocessedExpPipeFactory expFactory;
  //	private final AddProvider addProvider;
  private final InnerProductFactory innerProdFactory;
  private final ExpFromOIntFactory expFromOIntFactory;
  private final HammingDistanceFactory hammingFactory;
  private final RandomAdditiveMaskFactory randomAddMaskFactory;

  private final MiscOIntGenerators miscOIntGenerator;

//	private final OInt[] twoPows;

  private ProtocolProducer pp;
  private boolean done;

  //	public EqualityImplWithPreprocessing(int bitLength, int securityParam,
//			SInt x, SInt y, SInt result,
//BasicNumericProvider provider, MultByConstantProvider mbcProvider, NumericBitProvider bitProvider,
//ExponentiationProvider expProvider, AddProvider addProvider,
//InnerProductProvider innerProdProvider, MiscOIntGenerators miscOIntGenerator,
//ExpFromOIntProvider expFromOIntProvider, HammingDistanceProvider hammingProvider,
//RandomAdditiveMaskProvider randomAddMaskProvider) {
  public EqualityProtocolImplWithPreprocessing(int bitLength, int securityParam,
      SInt x, SInt y, SInt result,
      BasicNumericFactory<SInt> factory, MultByConstantFactory mbcFactory,
      NumericBitFactory bitProvider,
      PreprocessedExpPipeFactory expFactory, AddProtocolFactory addFactory,
      InnerProductFactory innerProdFactory, MiscOIntGenerators miscOIntGenerator,
      ExpFromOIntFactory expFromOIntFactory) {
    this.bitLength = bitLength;
    this.securityParam = securityParam;
    this.x = x;
    this.y = y;
    this.result = result;
    this.factory = factory;
    this.mbcFactory = mbcFactory;
//		this.bitProvider = bitProvider;
    this.expFactory = expFactory;
//		this.addProvider = addProvider;
    this.innerProdFactory = innerProdFactory;
    this.expFromOIntFactory = expFromOIntFactory;
    this.hammingFactory = null; // hammingProvider;
    this.randomAddMaskFactory = null; //randomAddMaskProvider;

    // TODO: should we generate it here, or have a global one? Probably have a global one...
    this.miscOIntGenerator = miscOIntGenerator;

    this.pp = null;
    this.done = false;

//		this.twoPows = miscOIntGenerator.getTwoPowers(securityParam+ bitLength);		
  }


  @Override
  public void getNextProtocols(ProtocolCollection protocolCollection) {
    if (pp == null) {
      ProtocolProducer problemReducerPP, EqSolverGP;
      SInt reducedProblem = factory.getSInt();

      problemReducerPP = reduceProblemSize(reducedProblem);

      EqSolverGP = reducedEqSolverGP(reducedProblem);

      this.pp = new SequentialProtocolProducer(problemReducerPP, EqSolverGP);
    }
    if (pp.hasNextProtocols()) {
      pp.getNextProtocols(protocolCollection);
    } else {
      pp = null;
      done = true;
    }
  }

  @Override
  public boolean hasNextProtocols() {
    return !done;
  }


  private SInt[] loadRandomMultiplicativeMask() {
    // R[0] = r^{-1}
    // R[i] = R^i
    return expFactory.getExponentiationPipe();
  }

  private ProtocolProducer reduceProblemSize(SInt reducedProblem) {
    // load random r and bits of r mod 2^length
    SInt[] bits = new SInt[bitLength];
    for (int i = 0; i < bitLength; i++) {
      bits[i] = factory.getSInt();
    }
    SInt r = factory.getSInt();
    ProtocolBuilder<SInt> protocolBuilder = ProtocolBuilder.createSequential(factory);

    ProtocolProducer randLoader = randomAddMaskFactory
        .getRandomAdditiveMaskProtocol(securityParam, bits, r);
    protocolBuilder.append(randLoader);

    // mask and reveal difference
    OInt masked_O = factory.getOInt();

    BasicNumericFactory<SInt> factory = protocolBuilder.createAppendingBasicNumericFactory();
    SInt resultSub = factory.sub(x, y).out();
    SInt resultAdd = factory.add(resultSub, r).out();
    factory.getOpenProtocol(resultAdd, masked_O);

    // Compute Hamming distance
    protocolBuilder.append(hammingFactory
        .getHammingdistanceProtocol(bits, masked_O, reducedProblem));

    return protocolBuilder.build();
  }


  /**
   * @param reducedProblem equalToZero-input where the value is at most bitLength
   * @return SInt stating if the input was zero (1) or non-zero (0)
   */
  private ProtocolProducer reducedEqSolverGP(SInt reducedProblem) {
    // load random R and relevant powers
    SInt[] R = loadRandomMultiplicativeMask();

    // mask (multiplicatively) and reveal
    OInt masked_O = factory.getOInt();

    Computation<? extends SInt> mult = factory.mult(reducedProblem, R[0]);
    Computation<? extends OInt> open = factory.getOpenProtocol(mult.out(), masked_O);

    // compute powers and evaluate polynomial
    OInt[] maskedPowers = expFromOIntFactory.getExpFromOInt(masked_O, bitLength);

    Computation[] unmaskGPs = new NativeProtocol[bitLength];
    SInt[] powers = new SInt[bitLength];
    for (int i = 0; i < bitLength; i++) {
      powers[i] = factory.getSInt();
      unmaskGPs[i] = mbcFactory.getMultProtocol(maskedPowers[i], R[i], powers[i]);
    }

    OInt[] polynomialCoefficients = miscOIntGenerator.getPoly(bitLength, factory.getModulus());
    ProtocolProducer polynomialGP = innerProdFactory
        .getInnerProductProtocol(powers, polynomialCoefficients, this.result);

    SequentialProtocolProducer sequentialProtocolProducer =
        new SequentialProtocolProducer(mult, open);
    sequentialProtocolProducer.append(new ParallelProtocolProducer(unmaskGPs));
    sequentialProtocolProducer.append(polynomialGP);
    return sequentialProtocolProducer;
  }
}
