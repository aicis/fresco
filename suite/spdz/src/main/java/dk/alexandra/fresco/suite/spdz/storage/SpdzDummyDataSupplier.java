package dk.alexandra.fresco.suite.spdz.storage;

import dk.alexandra.fresco.framework.builder.numeric.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.FieldElement;
import dk.alexandra.fresco.framework.util.ArithmeticDummyDataSupplier;
import dk.alexandra.fresco.framework.util.MultiplicationTripleShares;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzInputMask;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzSInt;
import dk.alexandra.fresco.suite.spdz.datatypes.SpdzTriple;
import java.math.BigInteger;
import java.util.List;

public class SpdzDummyDataSupplier implements SpdzDataSupplier {

  private final int myId;
  private final ArithmeticDummyDataSupplier supplier;
  private final FieldDefinition fieldDefinition;
  private final BigInteger secretSharedKey;
  private final int expPipeLength;

  public SpdzDummyDataSupplier(int myId, int noOfPlayers, FieldDefinition fieldDefinition,
      BigInteger secretSharedKey) {
    this(myId, noOfPlayers, fieldDefinition, secretSharedKey, 200);
  }

  public SpdzDummyDataSupplier(int myId, int noOfPlayers, FieldDefinition fieldDefinition,
      BigInteger secretSharedKey, int expPipeLength) {
    this.myId = myId;
    this.fieldDefinition = fieldDefinition;
    this.secretSharedKey = secretSharedKey;
    this.expPipeLength = expPipeLength;
    this.supplier = new ArithmeticDummyDataSupplier(myId, noOfPlayers,
        fieldDefinition.getModulus().getBigInteger());
  }

  @Override
  public SpdzTriple getNextTriple() {
    MultiplicationTripleShares rawTriple = supplier.getMultiplicationTripleShares();
    return new SpdzTriple(
        toSpdzSInt(rawTriple.getLeft()),
        toSpdzSInt(rawTriple.getRight()),
        toSpdzSInt(rawTriple.getProduct()));
  }

  @Override
  public SpdzSInt[] getNextExpPipe() {
    List<Pair<BigInteger, BigInteger>> rawExpPipe = supplier.getExpPipe(expPipeLength);
    return rawExpPipe.stream()
        .map(this::toSpdzSInt)
        .toArray(SpdzSInt[]::new);
  }

  @Override
  public SpdzInputMask getNextInputMask(int towardPlayerId) {
    Pair<BigInteger, BigInteger> raw = supplier.getRandomElementShare();
    if (myId == towardPlayerId) {
      return new SpdzInputMask(toSpdzSInt(raw), getBigIntegerI(raw.getFirst()));
    } else {
      return new SpdzInputMask(toSpdzSInt(raw), null);
    }
  }

  @Override
  public SpdzSInt getNextBit() {
    return toSpdzSInt(supplier.getRandomBitShare());
  }

  @Override
  public BigInteger getModulus() {
    return fieldDefinition.getModulus().getBigInteger();
  }

  @Override
  public FieldDefinition getFieldDefinition() {
    return fieldDefinition;
  }

  @Override
  public FieldElement getSecretSharedKey() {
    return getBigIntegerI(secretSharedKey);
  }

  @Override
  public SpdzSInt getNextRandomFieldElement() {
    return toSpdzSInt(supplier.getRandomElementShare());
  }

  private SpdzSInt toSpdzSInt(Pair<BigInteger, BigInteger> raw) {
    return new SpdzSInt(
        getBigIntegerI(raw.getSecond()),
        getBigIntegerI(raw.getFirst().multiply(secretSharedKey)
            .mod(fieldDefinition.getModulus().getBigInteger()))
    );
  }

  private FieldElement getBigIntegerI(BigInteger value) {
    return fieldDefinition.createElement(value);
  }
}
