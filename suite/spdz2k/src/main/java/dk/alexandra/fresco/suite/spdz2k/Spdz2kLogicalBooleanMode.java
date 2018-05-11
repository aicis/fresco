package dk.alexandra.fresco.suite.spdz2k;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.DefaultLogical;
import dk.alexandra.fresco.framework.builder.numeric.Logical;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUInt;
import dk.alexandra.fresco.suite.spdz2k.datatypes.CompUIntFactory;
import dk.alexandra.fresco.suite.spdz2k.datatypes.Spdz2kSIntBoolean;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kAndKnownProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kAndProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kOrProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kOutputToAll;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kXorKnownProtocol;
import dk.alexandra.fresco.suite.spdz2k.protocols.natives.Spdz2kXorProtocol;

/**
 * Logical operators for Spdz2k on boolean shares. <p>NOTE: requires that inputs have previously
 * been converted to boolean shares!</p>
 */
public class Spdz2kLogicalBooleanMode<PlainT extends CompUInt<?, ?, PlainT>> extends
    DefaultLogical implements Logical {

  private final CompUIntFactory<PlainT> factory;

  protected Spdz2kLogicalBooleanMode(
      ProtocolBuilderNumeric builder, CompUIntFactory<PlainT> factory) {
    super(builder);
    this.factory = factory;
  }

  @Override
  public DRes<SInt> and(DRes<SInt> bitA, DRes<SInt> bitB) {
    return builder.append(new Spdz2kAndProtocol<>(bitA, bitB));
  }

  @Override
  public DRes<SInt> or(DRes<SInt> bitA, DRes<SInt> bitB) {
    return builder.append(new Spdz2kOrProtocol<>(bitA, bitB));
  }

  @Override
  public DRes<SInt> xor(DRes<SInt> bitA, DRes<SInt> bitB) {
    return builder.append(new Spdz2kXorProtocol<>(bitA, bitB));
  }

  @Override
  public DRes<SInt> halfOr(DRes<SInt> bitA, DRes<SInt> bitB) {
    return xor(bitA, bitB);
  }

  @Override
  public DRes<SInt> andKnown(DRes<OInt> knownBit, DRes<SInt> secretBit) {
    return builder.append(new Spdz2kAndKnownProtocol<>(knownBit, secretBit));
  }

  @Override
  public DRes<SInt> xorKnown(DRes<OInt> knownBit, DRes<SInt> secretBit) {
    return builder.append(new Spdz2kXorKnownProtocol<>(knownBit, secretBit));
  }

  @Override
  public DRes<SInt> not(DRes<SInt> secretBit) {
    return xorKnown(builder.getOIntFactory().one(), secretBit);
  }

  @Override
  public DRes<OInt> openAsBit(DRes<SInt> secretBit) {
    // quite heavy machinery...
    return builder.seq(seq -> {
      Spdz2kSIntBoolean<PlainT> bit = factory.toSpdz2kSIntBoolean(secretBit);
      return seq.append(new Spdz2kOutputToAll<>(bit.asArithmetic()));
    }).seq((seq, opened) -> {
      PlainT openBit = factory.fromOInt(opened);
      // TODO clean up
      return openBit.testBit(factory.getLowBitLength() - 1) ? factory.one() : factory.zero();
    });
  }

}
