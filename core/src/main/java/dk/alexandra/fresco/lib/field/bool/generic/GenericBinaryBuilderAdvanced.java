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
package dk.alexandra.fresco.lib.field.bool.generic;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.binary.BinaryBuilderAdvanced;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.collections.sort.OddEvenMergeProtocol;
import dk.alexandra.fresco.lib.collections.sort.OddEvenMergeProtocolRec;
import dk.alexandra.fresco.lib.compare.KeyedCompareAndSwapProtocol;
import dk.alexandra.fresco.lib.math.bool.add.BitIncrementerProtocol;
import dk.alexandra.fresco.lib.math.bool.add.FullAdderProtocolImpl;
import dk.alexandra.fresco.lib.math.bool.add.OneBitFullAdderProtocolImpl;
import dk.alexandra.fresco.lib.math.bool.add.OneBitHalfAdderProtocolImpl;
import java.util.List;

public class GenericBinaryBuilderAdvanced implements BinaryBuilderAdvanced {

  private ProtocolBuilderBinary builder;

  public GenericBinaryBuilderAdvanced(ProtocolBuilderBinary builder) {
    super();
    this.builder = builder;
  }

  @Override
  public Computation<SBool> or(Computation<SBool> left, Computation<SBool> right) {
    return builder.createSequentialSub(new OrFromXorAnd(left, right));
  }

  @Override
  public Computation<SBool> or(Computation<SBool> left, boolean right) {
    return builder.createSequentialSub(new OrFromCopyConst(left, right));
  }

  @Override
  public Computation<SBool> xnor(Computation<SBool> left, Computation<SBool> right) {
    return builder.createSequentialSub(new XnorFromXorAndNot(left, right));
  }

  @Override
  public Computation<SBool> xnor(Computation<SBool> left, boolean right) {
    if (right) {
      return builder.binary().copy(left);
    } else {
      return builder.binary().not(left);
    }
  }

  @Override
  public Computation<SBool> nand(Computation<SBool> left, Computation<SBool> right) {
    return builder.createSequentialSub(new NandFromAndAndNot(left, right));
  }

  @Override
  public Computation<SBool> nand(Computation<SBool> left, boolean right) {
    if (right) {
      return builder.binary().not(left);
    } else {
      return builder.binary().known(true);
    }
  }


  public Computation<SBool> and(Computation<SBool> left, boolean right) {
    return builder.createSequentialSub(new AndFromCopyConst(left, right));
  }

  public Computation<SBool> not(Computation<SBool> in) {
    return builder.createSequentialSub(new NotFromXor(in));
  }

  @Override
  public Computation<SBool> condSelect(Computation<SBool> condition, Computation<SBool> left,
      Computation<SBool> right) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Computation<Pair<SBool, SBool>> oneBitHalfAdder(Computation<SBool> left,
      Computation<SBool> right) {
    return builder.createSequentialSub(new OneBitHalfAdderProtocolImpl(left, right));
  }

  @Override
  public Computation<Pair<SBool, SBool>> oneBitFullAdder(Computation<SBool> left,
      Computation<SBool> right, Computation<SBool> carry) {
    return builder.createSequentialSub(new OneBitFullAdderProtocolImpl(left, right, carry));
  }

  @Override
  public Computation<List<Computation<SBool>>> fullAdder(List<Computation<SBool>> lefts,
      List<Computation<SBool>> rights, Computation<SBool> inCarry) {
    return builder.createSequentialSub(new FullAdderProtocolImpl(lefts, rights, inCarry));
  }


  /**
   * Advanced protocols - do not yet exist in interface
   */


  public OddEvenMergeProtocol getOddEvenMergeProtocol(List<Pair<SBool[], SBool[]>> left,
      List<Pair<SBool[], SBool[]>> right, List<Pair<SBool[], SBool[]>> sorted) {
    return new OddEvenMergeProtocolRec(left, right, sorted, this);
  }

  public BitIncrementerProtocol getBitIncrementerProtocol(SBool[] base, SBool increment,
      SBool[] outs) {
    return null;// return new BitIncrementerProtocolImpl(base, increment, outs, this, this);
  }

  public KeyedCompareAndSwapProtocol getKeyedCompareAndSwapProtocol(SBool[] leftKey,
      SBool[] leftValue, SBool[] rightKey, SBool[] rightValue) {
    return null;// return new KeyedCompareAndSwapProtocolGetNextProtocolImpl(leftKey, leftValue,
                // rightKey,
    // rightValue, this);
  }

  @Override
  public List<Computation<SBool>> binaryMult(List<Computation<SBool>> lefts,
      List<Computation<SBool>> rights) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Computation<SBool>> logProtocol(List<Computation<SBool>> number) {
    // TODO Auto-generated method stub
    return null;
  }


}
