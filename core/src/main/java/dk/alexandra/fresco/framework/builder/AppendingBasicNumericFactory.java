package dk.alexandra.fresco.framework.builder;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.value.OInt;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import java.math.BigInteger;

class AppendingBasicNumericFactory<SIntT extends SInt>
    implements BasicNumericFactory<SIntT> {

  @Override
  public SInt getSInt() {
    return factory.getSInt();
  }

  @Override
  @Deprecated
  public SInt getSInt(int i) {
    return factory.getSInt(i);
  }

  @Override
  public OInt getOInt() {
    return factory.getOInt();
  }

  @Override
  @Deprecated
  public SInt getSInt(BigInteger i) {
    return factory.getSInt(i);
  }

  @Override
  public OInt getOInt(BigInteger i) {
    return factory.getOInt(i);
  }

  @Override
  public Computation<? extends SInt> getMultProtocol(
      SInt a, SInt b,
      SInt out) {
    Computation<? extends SInt> multProtocol = factory.getMultProtocol(a, b, out);
    protocolProducer.append(multProtocol);
    return multProtocol;
  }

  @Override
  public ProtocolProducer createRandomSecretSharedBitProtocol(
      SInt bit) {
    ProtocolProducer randomSecretSharedBitProtocol = factory
        .createRandomSecretSharedBitProtocol(bit);
    protocolProducer.append(randomSecretSharedBitProtocol);
    return randomSecretSharedBitProtocol;
  }

  @Override
  public Computation<? extends SInt> getSubtractProtocol(
      SInt a, SInt b,
      SInt out) {
    Computation<? extends SInt> subtractProtocol = factory.getSubtractProtocol(a, b, out);
    protocolProducer.append(subtractProtocol);
    return subtractProtocol;
  }

  @Override
  public Computation<SInt> getSInt(
      int i, SInt si) {
    return factory.getSInt(i, si);
  }

  @Override
  public Computation<? extends SInt> getMultProtocol(
      OInt a, SInt b,
      SInt c) {
    Computation<? extends SInt> multProtocol = factory.getMultProtocol(a, b, c);
    protocolProducer.append(multProtocol);
    return multProtocol;
  }

  @Override
  public Computation<? extends SInt> getAddProtocol(
      SInt a, SInt b,
      SInt out) {
    return append(factory.getAddProtocol(a, b, out));
  }

  private <T> Computation<T> append(Computation<T> protocol) {
    protocolProducer.append(protocol);
    return protocol;
  }

  @Override
  public Computation<? extends SInt> getSubtractProtocol(
      OInt a, SInt b,
      SInt out) {
    return append(factory.getSubtractProtocol(a, b, out));
  }

  @Override
  public OInt getRandomOInt() {
    return factory.getRandomOInt();
  }

  @Override
  public Computation<? extends SInt> getSubtractProtocol(
      SInt a, OInt b,
      SInt out) {
    return append(factory.getSubtractProtocol(a, b, out));
  }

  @Override
  public Computation<? extends SInt> getAddProtocol(
      SInt input,
      OInt openInput,
      SInt out) {
    return append(factory.getAddProtocol(input, openInput, out));
  }

  @Override
  public Computation<? extends SInt> getCloseProtocol(
      BigInteger open, SInt closed, int targetID) {
    return append(factory.getCloseProtocol(open, closed, targetID));
  }

  @Override
  public Computation<SInt> getSInt(
      BigInteger i, SInt si) {
    return append(factory.getSInt(i, si));
  }

  @Override
  public Computation<SIntT> getRandomFieldElement(SIntT randomElement) {
    return append(factory.getRandomFieldElement(randomElement));
  }

  @Override
  public int getMaxBitLength() {
    return factory.getMaxBitLength();
  }

  @Override
  public Computation<? extends SInt> getCloseProtocol(
      int source, OInt open,
      SInt closed) {
    return append(factory.getCloseProtocol(source, open, closed));
  }

  @Override
  public SInt getSqrtOfMaxValue() {
    return factory.getSqrtOfMaxValue();
  }

  @Override
  public Computation<? extends OInt> getOpenProtocol(
      SInt closed, OInt open) {
    return append(factory.getOpenProtocol(closed, open));
  }

  @Override
  public BigInteger getModulus() {
    return factory.getModulus();
  }

  @Override
  public Computation<? extends OInt> getOpenProtocol(
      int target, SInt closed, OInt open) {
    return append(factory.getOpenProtocol(target, closed, open));
  }

  private final BasicNumericFactory<SIntT> factory;
  private final ProtocolBuilder protocolProducer;

  AppendingBasicNumericFactory(
      BasicNumericFactory<SIntT> factory,
      ProtocolBuilder protocolProducer) {
    this.factory = factory;
    this.protocolProducer = protocolProducer;
  }
}
