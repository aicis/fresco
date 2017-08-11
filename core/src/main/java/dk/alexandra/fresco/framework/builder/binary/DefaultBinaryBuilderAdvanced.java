package dk.alexandra.fresco.framework.builder.binary;

import java.util.List;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.collections.sort.OddEvenMergeProtocol;
import dk.alexandra.fresco.lib.collections.sort.OddEvenMergeProtocolRec;
import dk.alexandra.fresco.lib.compare.KeyedCompareAndSwapProtocol;
import dk.alexandra.fresco.lib.field.bool.generic.AndFromCopyConst;
import dk.alexandra.fresco.lib.field.bool.generic.NandFromAndAndNot;
import dk.alexandra.fresco.lib.field.bool.generic.NotFromXor;
import dk.alexandra.fresco.lib.field.bool.generic.OrFromCopyConst;
import dk.alexandra.fresco.lib.field.bool.generic.OrFromXorAnd;
import dk.alexandra.fresco.lib.field.bool.generic.XnorFromXorAndNot;
import dk.alexandra.fresco.lib.math.bool.add.BitIncrementerProtocol;
import dk.alexandra.fresco.lib.math.bool.add.FullAdderProtocolImpl;
import dk.alexandra.fresco.lib.math.bool.add.OneBitFullAdderProtocolImpl;
import dk.alexandra.fresco.lib.math.bool.add.OneBitHalfAdderProtocolImpl;

public class DefaultBinaryBuilderAdvanced implements BinaryBuilderAdvanced {

  private final ProtocolBuilderBinary builder;

  public DefaultBinaryBuilderAdvanced(ProtocolBuilderBinary builder) {
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
    if(right) {
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
    if(right) {
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
  public Computation<SBool> greaterThan(Computation<List<SBool>> left, Computation<List<SBool>> right) {

    return null; //return new BinaryGreaterThanProtocolImpl(inLeft, inRight, out, this);
  }

  @Override
  public Computation<SBool> equals(Computation<List<SBool>> left, Computation<List<SBool>> right) {
    return null; //return new AltBinaryEqualityProtocol(inLeft, inRight, out, this);
  }

  @Override
  public Computation<Pair<SBool, SBool>> oneBitHalfAdder(Computation<SBool> left, Computation<SBool> right) {
    return builder.createSequentialSub(new OneBitHalfAdderProtocolImpl(left, right));
  }

  @Override
  public Computation<Pair<SBool, SBool>> oneBitFullAdder(Computation<SBool> left, Computation<SBool> right,
      Computation<SBool> carry) {
    return builder.createSequentialSub(new OneBitFullAdderProtocolImpl(left, right, carry));
  }

  @Override
  public Computation<List<Computation<SBool>>> fullAdder(List<Computation<SBool>> lefts, List<Computation<SBool>> rights,
      Computation<SBool> inCarry) {
    return builder.createSequentialSub(new FullAdderProtocolImpl(lefts, rights, inCarry));
  }

  @Override
  public Computation<SBool[]> binaryMult(SBool[] lefts, SBool[] rights) {
    return null;////return new BinaryMultProtocolImpl(lefts, rights, outs, this, this);
  }

  @Override
  public Computation<SBool[]> logProtocol(SBool[] number) {
    return null;//return new LogProtocolImpl(number, result, this);
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
    return null;//return new KeyedCompareAndSwapProtocolGetNextProtocolImpl(leftKey, leftValue, rightKey,
        //rightValue, this);
  }
  }
