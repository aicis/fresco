package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.collections.sort.KeyedCompareAndSwapProtocol;
import dk.alexandra.fresco.lib.collections.sort.OddEvenMergeProtocol;
import dk.alexandra.fresco.lib.collections.sort.OddEvenMergeProtocolRec;
import dk.alexandra.fresco.lib.field.bool.ConditionalSelect;
import dk.alexandra.fresco.lib.field.bool.generic.AndFromCopyConst;
import dk.alexandra.fresco.lib.field.bool.generic.NandFromAndAndNot;
import dk.alexandra.fresco.lib.field.bool.generic.NotFromXor;
import dk.alexandra.fresco.lib.field.bool.generic.OrFromCopyConst;
import dk.alexandra.fresco.lib.field.bool.generic.OrFromXorAnd;
import dk.alexandra.fresco.lib.field.bool.generic.XnorFromXorAndNot;
import dk.alexandra.fresco.lib.math.bool.add.BitIncrementerProtocolImpl;
import dk.alexandra.fresco.lib.math.bool.add.FullAdderProtocolImpl;
import dk.alexandra.fresco.lib.math.bool.add.OneBitFullAdderProtocolImpl;
import dk.alexandra.fresco.lib.math.bool.add.OneBitHalfAdderProtocolImpl;
import dk.alexandra.fresco.lib.math.bool.log.LogProtocol;
import dk.alexandra.fresco.lib.math.bool.mult.BinaryMultProtocol;
import java.util.List;

public class DefaultBinaryBuilderAdvanced implements BinaryBuilderAdvanced {

  private final ProtocolBuilderBinary builder;

  protected DefaultBinaryBuilderAdvanced(ProtocolBuilderBinary builder) {
    super();
    this.builder = builder;
  }

  @Override
  public Computation<SBool> or(Computation<SBool> left, Computation<SBool> right) {
    return builder.seq(new OrFromXorAnd(left, right));
  }

  @Override
  public Computation<SBool> or(Computation<SBool> left, boolean right) {
    return builder.seq(new OrFromCopyConst(left, right));
  }

  @Override
  public Computation<SBool> xnor(Computation<SBool> left, Computation<SBool> right) {
    return builder.seq(new XnorFromXorAndNot(left, right));
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
    return builder.seq(new NandFromAndAndNot(left, right));
  }

  @Override
  public Computation<SBool> nand(Computation<SBool> left, boolean right) {
    if (right) {
      return builder.binary().not(left);
    } else {
      return builder.binary().known(true);
    }
  }

  @Override
  public Computation<Pair<SBool, SBool>> oneBitFullAdder(Computation<SBool> left,
      Computation<SBool> right, Computation<SBool> carry) {
    return builder.seq(new OneBitFullAdderProtocolImpl(left, right, carry));
  }

  @Override
  public Computation<List<Computation<SBool>>> fullAdder(List<Computation<SBool>> lefts,
      List<Computation<SBool>> rights, Computation<SBool> inCarry) {
    return builder.seq(new FullAdderProtocolImpl(lefts, rights, inCarry));
  }

  public Computation<List<Computation<SBool>>> bitIncrement(List<Computation<SBool>> base,
      Computation<SBool> increment) {
    return builder.seq(new BitIncrementerProtocolImpl(base, increment));
  }

  @Override
  public Computation<SBool> and(Computation<SBool> left, boolean right) {
    return builder.seq(new AndFromCopyConst(left, right));
  }

  public Computation<SBool> not(Computation<SBool> in) {
    return builder.seq(new NotFromXor(in));
  }

  @Override
  public Computation<SBool> condSelect(Computation<SBool> condition, Computation<SBool> left,
      Computation<SBool> right) {
    return builder.seq(new ConditionalSelect(condition, left, right));
  }

  @Override
  public Computation<Pair<SBool, SBool>> oneBitHalfAdder(Computation<SBool> left,
      Computation<SBool> right) {
    return builder.seq(new OneBitHalfAdderProtocolImpl(left, right));
  }



  @Override
  public Computation<List<Computation<SBool>>> binaryMult(List<Computation<SBool>> lefts,
      List<Computation<SBool>> rights) {
    return builder.seq(new BinaryMultProtocol(lefts, rights));
  }

  @Override
  public Computation<List<Computation<SBool>>> logProtocol(List<Computation<SBool>> number) {
    return builder.seq(new LogProtocol(number));
  }


  /**
   * Advanced protocols - do not yet exist in interface
   */


  public OddEvenMergeProtocol getOddEvenMergeProtocol(List<Pair<SBool[], SBool[]>> left,
      List<Pair<SBool[], SBool[]>> right, List<Pair<SBool[], SBool[]>> sorted) {
    return new OddEvenMergeProtocolRec(left, right, sorted, this);
  }

  public Computation<List<Pair<List<Computation<SBool>>, List<Computation<SBool>>>>> getKeyedCompareAndSwapProtocol(List<Computation<SBool>> leftKey,
      List<Computation<SBool>> leftValue, List<Computation<SBool>> rightKey, List<Computation<SBool>> rightValue) {
    return builder.seq(new KeyedCompareAndSwapProtocol(leftKey, leftValue, rightKey, rightValue));
  }

}