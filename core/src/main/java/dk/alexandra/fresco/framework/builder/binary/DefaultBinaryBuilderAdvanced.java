package dk.alexandra.fresco.framework.builder.binary;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.lib.math.bool.add.FullAdderProtocolImpl;
import dk.alexandra.fresco.lib.math.bool.add.OneBitFullAdderProtocolImpl;
import dk.alexandra.fresco.lib.math.bool.add.OneBitHalfAdderProtocolImpl;
import java.util.List;

public class DefaultBinaryBuilderAdvanced implements BinaryBuilderAdvanced {

  private final ProtocolBuilderBinary builder;

  public DefaultBinaryBuilderAdvanced(ProtocolBuilderBinary builder) {
    super();
    this.builder = builder;
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

  @Override
  public Computation<SBool> or(Computation<SBool> left, Computation<SBool> right) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Computation<SBool> or(Computation<SBool> left, boolean right) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Computation<SBool> xnor(Computation<SBool> left, Computation<SBool> right) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Computation<SBool> xnor(Computation<SBool> left, boolean right) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Computation<SBool> nand(Computation<SBool> left, Computation<SBool> right) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Computation<SBool> nand(Computation<SBool> left, boolean right) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Computation<SBool> condSelect(Computation<SBool> condition, Computation<SBool> left,
      Computation<SBool> right) {
    // TODO Auto-generated method stub
    return null;
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
