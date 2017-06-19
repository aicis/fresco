package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.compare.ComparisonProtocolFactory;
import dk.alexandra.fresco.lib.compare.eq.EqualityProtocol;

/**
 * Appending comparison factory
 */
class AppendingComparisonProtocolFactory implements ComparisonProtocolFactory {

  private final ComparisonProtocolFactory comparisonProtocolFactory;
  private final ProtocolBuilder protocolBuilder;

  AppendingComparisonProtocolFactory(
      ComparisonProtocolFactory comparisonProtocolFactory,
      ProtocolBuilder protocolBuilder) {
    this.comparisonProtocolFactory = comparisonProtocolFactory;
    this.protocolBuilder = protocolBuilder;
  }

  @Override
  public ProtocolProducer getGreaterThanProtocol(SInt x1,
      SInt x2, SInt result, boolean longCompare) {
    ProtocolProducer greaterThanProtocol = comparisonProtocolFactory
        .getGreaterThanProtocol(x1, x2, result, longCompare);
    protocolBuilder.append(greaterThanProtocol);
    return greaterThanProtocol;
  }

  @Override
  public EqualityProtocol getEqualityProtocol(int bitLength,
      SInt x, SInt y, SInt result) {
    EqualityProtocol equalityProtocol = comparisonProtocolFactory
        .getEqualityProtocol(bitLength, x, y, result);
    protocolBuilder.append(equalityProtocol);
    return equalityProtocol;
  }
}
