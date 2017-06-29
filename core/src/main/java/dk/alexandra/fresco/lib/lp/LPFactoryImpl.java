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
package dk.alexandra.fresco.lib.lp;

import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.builder.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactory;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactoryImpl;
import dk.alexandra.fresco.lib.compare.MiscOIntGenerators;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactory;
import dk.alexandra.fresco.lib.compare.RandomAdditiveMaskFactoryImpl;
import dk.alexandra.fresco.lib.compare.eq.EqualityProtocol;
import dk.alexandra.fresco.lib.compare.eq.EqualityProtocolImpl;
import dk.alexandra.fresco.lib.compare.gt.GreaterThanReducerProtocolImpl;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestProtocolFactory;
import dk.alexandra.fresco.lib.compare.zerotest.ZeroTestProtocolFactoryImpl;
import dk.alexandra.fresco.lib.debug.MarkerProtocol;
import dk.alexandra.fresco.lib.debug.MarkerProtocolImpl;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.field.integer.RandomFieldElementFactory;
import dk.alexandra.fresco.lib.helper.CopyProtocolImpl;
import dk.alexandra.fresco.lib.math.integer.NumericNegateBitFactory;
import dk.alexandra.fresco.lib.math.integer.NumericNegateBitFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.exp.ExpFromOIntFactory;
import dk.alexandra.fresco.lib.math.integer.exp.PreprocessedExpPipeFactory;
import dk.alexandra.fresco.lib.math.integer.inv.InversionProtocol;
import dk.alexandra.fresco.lib.math.integer.inv.InversionProtocolImpl;
import dk.alexandra.fresco.lib.math.integer.linalg.EntrywiseProductFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.linalg.EntrywiseProductProtocol;
import dk.alexandra.fresco.lib.math.integer.linalg.EntrywiseProductProtocolImpl;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactory;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductFactoryImpl;
import dk.alexandra.fresco.lib.math.integer.linalg.InnerProductProtocol;
import java.math.BigInteger;

public class LPFactoryImpl implements LPFactory {

  private final int securityParameter;
  private final BasicNumericFactory bnf;
  private final NumericNegateBitFactory numericNegateBitFactory;
  private final RandomAdditiveMaskFactory randomAdditiveMaskFactory;
  private final RandomFieldElementFactory randFactory;
  private final InnerProductFactory innerProductFactory;
  private final ZeroTestProtocolFactory zeroTestProtocolFactory;
  private final MiscOIntGenerators misc;
  private ComparisonProtocolFactory compFactory;
  private BuilderFactoryNumeric factoryProducer;

  public LPFactoryImpl(int securityParameter, BasicNumericFactory bnf,
      ExpFromOIntFactory expFromOIntFactory,
      PreprocessedExpPipeFactory expFactory,
      RandomFieldElementFactory randFactory,
      BuilderFactoryNumeric factoryProducer) {
    this.securityParameter = securityParameter;
    this.bnf = bnf;
    this.randFactory = randFactory;
    this.numericNegateBitFactory = new NumericNegateBitFactoryImpl(bnf);
    this.innerProductFactory = new InnerProductFactoryImpl(bnf,
        new EntrywiseProductFactoryImpl(bnf));
    this.factoryProducer = factoryProducer;
    randomAdditiveMaskFactory = new RandomAdditiveMaskFactoryImpl(bnf,
        new InnerProductFactoryImpl(bnf, new EntrywiseProductFactoryImpl(bnf)));
    misc = new MiscOIntGenerators();
    this.zeroTestProtocolFactory = new ZeroTestProtocolFactoryImpl(bnf,
        expFromOIntFactory, numericNegateBitFactory, expFactory);
    this.compFactory = new ComparisonProtocolFactoryImpl(securityParameter, bnf,
        expFromOIntFactory, expFactory, this.factoryProducer);
  }

  @Override
  public InversionProtocol getInversionProtocol(SInt x, SInt result) {
    return new InversionProtocolImpl(x, result, bnf, randFactory);
  }

  @Override
  public MarkerProtocol getMarkerProtocol(String message) {
    return new MarkerProtocolImpl(message);
  }

  @Override
  public NativeProtocol<SInt, ?> getCopyProtocol(SInt in, SInt out) {
    return new CopyProtocolImpl<>(in, out);
  }

  @Override
  public EntrywiseProductProtocol getEntrywiseProductProtocol(SInt[] as, SInt[] bs,
      SInt[] results) {
    return new EntrywiseProductProtocolImpl(as, bs, results, bnf);
  }

  @Override
  public EntrywiseProductProtocol getEntrywiseProductProtocol(SInt[] as, BigInteger[] bs,
      SInt[] results) {
    return new EntrywiseProductProtocolImpl(as, bs, results, bnf);
  }

  @Override
  public GreaterThanReducerProtocolImpl getComparisonProtocol(SInt x1, SInt x2,
      SInt result, boolean longCompare) {
    int bitLength = bnf.getMaxBitLength();
    if (longCompare) {
      bitLength *= 2;
    }
    return new GreaterThanReducerProtocolImpl(bitLength,
        this.securityParameter, x1, x2, result, bnf, numericNegateBitFactory,
        randomAdditiveMaskFactory, zeroTestProtocolFactory, misc,
        innerProductFactory, factoryProducer);
  }

  @Override
  public EqualityProtocol getEqualityProtocol(int bitLength,
      int securityParam, SInt x, SInt y, SInt result) {
    return new EqualityProtocolImpl(bitLength, securityParam, x, y, result,
        bnf, zeroTestProtocolFactory);
  }

  @Override
  public InnerProductProtocol getInnerProductProtocol(SInt[] aVector,
      SInt[] bVector, SInt result) {
    return this.innerProductFactory.getInnerProductProtocol(aVector,
        bVector, result);
  }

  @Override
  public InnerProductProtocol getInnerProductProtocol(SInt[] aVector, BigInteger[] bVector,
      SInt result) {
    return this.innerProductFactory.getInnerProductProtocol(aVector,
        bVector, result);
  }


}
