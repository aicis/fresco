package dk.alexandra.fresco.tools.mascot.prg;

import dk.alexandra.fresco.framework.builder.numeric.field.FieldDefinition;
import dk.alexandra.fresco.framework.builder.numeric.field.FieldElement;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.AesCtrDrbgFactory;
import dk.alexandra.fresco.framework.util.Drng;
import dk.alexandra.fresco.framework.util.DrngImpl;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.util.Objects;

public class FieldElementPrgImpl implements FieldElementPrg {

  private final Drng drng;
  private FieldDefinition definition;

  /**
   * Creates new FieldElement prg.
   *
   * @param seed seed to the underlying DRNG.
   */
  public FieldElementPrgImpl(StrictBitVector seed, FieldDefinition definition) {
    this.definition = Objects.requireNonNull(definition);
    byte[] bytes = seed.toByteArray();
    if (bytes.length != AesCtrDrbg.SEED_LENGTH) {
      this.drng = new DrngImpl(AesCtrDrbgFactory.fromDerivedSeed(bytes));
    } else {
      this.drng = new DrngImpl(AesCtrDrbgFactory.fromRandomSeed(bytes));
    }
  }

  @Override
  public FieldElement getNext() {
    return definition.createElement(drng.nextBigInteger(definition.getModulus()));
  }
}
