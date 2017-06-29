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
package dk.alexandra.fresco.lib.logic;

import dk.alexandra.fresco.framework.BuilderFactory;
import dk.alexandra.fresco.framework.NativeProtocol;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.OBool;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.collections.sort.KeyedCompareAndSwapProtocolGetNextProtocolImpl;
import dk.alexandra.fresco.lib.collections.sort.OddEvenMergeProtocol;
import dk.alexandra.fresco.lib.collections.sort.OddEvenMergeProtocolRec;
import dk.alexandra.fresco.lib.collections.sort.OddEvenMergeSortFactory;
import dk.alexandra.fresco.lib.compare.CompareAndSwapProtocolFactory;
import dk.alexandra.fresco.lib.compare.KeyedCompareAndSwapProtocol;
import dk.alexandra.fresco.lib.compare.bool.BinaryGreaterThanProtocol;
import dk.alexandra.fresco.lib.compare.bool.BinaryGreaterThanProtocolFactory;
import dk.alexandra.fresco.lib.compare.bool.BinaryGreaterThanProtocolImpl;
import dk.alexandra.fresco.lib.compare.bool.eq.AltBinaryEqualityProtocol;
import dk.alexandra.fresco.lib.compare.bool.eq.BinaryEqualityProtocol;
import dk.alexandra.fresco.lib.compare.bool.eq.BinaryEqualityProtocolFactory;
import dk.alexandra.fresco.lib.field.bool.AndProtocol;
import dk.alexandra.fresco.lib.field.bool.BasicLogicFactory;
import dk.alexandra.fresco.lib.field.bool.NandProtocol;
import dk.alexandra.fresco.lib.field.bool.OrProtocol;
import dk.alexandra.fresco.lib.field.bool.XnorProtocol;
import dk.alexandra.fresco.lib.field.bool.generic.AndFromCopyConstProtocol;
import dk.alexandra.fresco.lib.field.bool.generic.NandFromAndAndNotProtocolImpl;
import dk.alexandra.fresco.lib.field.bool.generic.NotFromXorProtocol;
import dk.alexandra.fresco.lib.field.bool.generic.OrFromCopyConstProtocol;
import dk.alexandra.fresco.lib.field.bool.generic.OrFromXorAndProtocol;
import dk.alexandra.fresco.lib.field.bool.generic.XnorFromXorAndNotProtocolImpl;
import dk.alexandra.fresco.lib.helper.CopyProtocolFactory;
import dk.alexandra.fresco.lib.helper.CopyProtocolImpl;
import dk.alexandra.fresco.lib.math.bool.add.AdderProtocolFactory;
import dk.alexandra.fresco.lib.math.bool.add.BitIncrementerProtocol;
import dk.alexandra.fresco.lib.math.bool.add.BitIncrementerProtocolFactory;
import dk.alexandra.fresco.lib.math.bool.add.BitIncrementerProtocolImpl;
import dk.alexandra.fresco.lib.math.bool.add.FullAdderProtocol;
import dk.alexandra.fresco.lib.math.bool.add.FullAdderProtocolImpl;
import dk.alexandra.fresco.lib.math.bool.add.OneBitFullAdderProtocol;
import dk.alexandra.fresco.lib.math.bool.add.OneBitFullAdderProtocolImpl;
import dk.alexandra.fresco.lib.math.bool.add.OneBitHalfAdderProtocol;
import dk.alexandra.fresco.lib.math.bool.add.OneBitHalfAdderProtocolImpl;
import dk.alexandra.fresco.lib.math.bool.log.LogProtocol;
import dk.alexandra.fresco.lib.math.bool.log.LogProtocolFactory;
import dk.alexandra.fresco.lib.math.bool.log.LogProtocolImpl;
import dk.alexandra.fresco.lib.math.bool.mult.BinaryMultProtocol;
import dk.alexandra.fresco.lib.math.bool.mult.BinaryMultProtocolFactory;
import dk.alexandra.fresco.lib.math.bool.mult.BinaryMultProtocolImpl;
import java.util.List;

public abstract class AbstractBinaryFactory
    implements BuilderFactory<ProtocolBuilderBinary>, BasicLogicFactory, AdderProtocolFactory,
    BinaryMultProtocolFactory,
    LogProtocolFactory,
    CopyProtocolFactory<SBool>, BinaryGreaterThanProtocolFactory, BinaryEqualityProtocolFactory,
    CompareAndSwapProtocolFactory, OddEvenMergeSortFactory, BitIncrementerProtocolFactory {

  @Override
  public ProtocolFactory getProtocolFactory() {
    return this;
  }

  @Override
  public ProtocolBuilderBinary createProtocolBuilder() {
    return ProtocolBuilderBinary.createApplicationRoot(this);
  }

  @Override
  public KeyedCompareAndSwapProtocol getKeyedCompareAndSwapProtocol(SBool[] leftKey,
      SBool[] leftValue,
      SBool[] rightKey, SBool[] rightValue) {
    return new KeyedCompareAndSwapProtocolGetNextProtocolImpl(leftKey, leftValue, rightKey,
        rightValue, this);
  }

  /**
   * simple protocols - basic functionality
   */
  @Override
  public SBool[] getSBools(int amount) {
    SBool[] res = new SBool[amount];
    for (int i = 0; i < amount; i++) {
      res[i] = this.getSBool();
    }
    return res;
  }

  @Override
  public NativeProtocol<SBool, ?> getCopyProtocol(SBool in, SBool out) {
    return new CopyProtocolImpl<>(in, out);
  }

  @Override
  public SBool[] getKnownConstantSBools(boolean[] bools) {
    int amount = bools.length;
    SBool[] res = new SBool[amount];
    for (int i = 0; i < amount; i++) {
      res[i] = this.getKnownConstantSBool(bools[i]);
    }
    return res;
  }

  public XnorProtocol getXnorProtocol(SBool left, SBool right, SBool out) {
    return new XnorFromXorAndNotProtocolImpl(left, right, out, this);
  }

  public NandProtocol getNandProtocol(SBool left, SBool right, SBool out) {
    return new NandFromAndAndNotProtocolImpl(left, right, out, this);
  }

  public OrProtocol getOrProtocol(SBool inLeft, SBool inRight, SBool out) {
    return new OrFromXorAndProtocol(this, this, this, inLeft, inRight, out);
  }

  public OrProtocol getOrProtocol(SBool inLeft, OBool inRight, SBool out) {
    return new OrFromCopyConstProtocol(this, this, inLeft, inRight, out);
  }

  @Override
  public AndProtocol getAndProtocol(SBool inLeft, OBool inRight, SBool out) {
    return new AndFromCopyConstProtocol(this, this, inLeft, inRight, out);
  }

  @Override
  public ProtocolProducer getNotProtocol(SBool in, SBool out) {
    return new NotFromXorProtocol(this, this, in, out);
  }

  /**
   * Advanced protocols - addition
   */

  @Override
  public OneBitFullAdderProtocol getOneBitFullAdderProtocol(SBool left, SBool right, SBool carry,
      SBool outS,
      SBool outCarry) {
    return new OneBitFullAdderProtocolImpl(left, right, carry, outS, outCarry, this);
  }

  @Override
  public FullAdderProtocol getFullAdderProtocol(SBool[] lefts, SBool[] rights, SBool inCarry,
      SBool[] outs,
      SBool outCarry) {
    return new FullAdderProtocolImpl(lefts, rights, inCarry, outs, outCarry, this, this);
  }

  @Override
  public BitIncrementerProtocol getBitIncrementerProtocol(SBool[] base, SBool increment,
      SBool[] outs) {
    return new BitIncrementerProtocolImpl(base, increment, outs, this, this);
  }

  @Override
  public OneBitHalfAdderProtocol getOneBitHalfAdderProtocol(SBool left, SBool right, SBool outS,
      SBool outCarry) {
    return new OneBitHalfAdderProtocolImpl(left, right, outS, outCarry, this);
  }

  /**
   * Advanced protocols - multiplication
   */

  @Override
  public BinaryMultProtocol getBinaryMultProtocol(SBool[] lefts, SBool[] rights, SBool[] outs) {
    return new BinaryMultProtocolImpl(lefts, rights, outs, this, this);
  }

  @Override
  public LogProtocol getLogProtocol(SBool[] number, SBool[] result) {
    return new LogProtocolImpl(number, result, this);
  }

  /**
   * Advanced protocols - comparisons
   */

  @Override
  public BinaryGreaterThanProtocol getBinaryComparisonProtocol(SBool[] inLeft, SBool[] inRight,
      SBool out) {
    return new BinaryGreaterThanProtocolImpl(inLeft, inRight, out, this);
    // return new BinaryComparisonprotocolNextProtocolsImpl(inLeft, inRight, out,
    // this);
    // return new GenericBinaryComparisonprotocol2(this, inLeft, inRight,
    // out);
  }

  @Override
  public BinaryEqualityProtocol getBinaryEqualityProtocol(SBool[] inLeft, SBool[] inRight,
      SBool out) {
    // return new BinaryEqualityprotocolImpl(inLeft, inRight, out, this);
    return new AltBinaryEqualityProtocol(inLeft, inRight, out, this);
  }

  /**
   * Advanced protocols - sorting
   */

  @Override
  public OddEvenMergeProtocol getOddEvenMergeProtocol(List<Pair<SBool[], SBool[]>> left,
      List<Pair<SBool[], SBool[]>> right, List<Pair<SBool[], SBool[]>> sorted) {
    return new OddEvenMergeProtocolRec(left, right, sorted, this);
  }

}
