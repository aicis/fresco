package dk.alexandra.fresco.suite.tinytables.prepro;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.binary.Binary;
import dk.alexandra.fresco.framework.builder.binary.BuilderFactoryBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.prepro.datatypes.TinyTablesPreproSBool;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproANDProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproCloseProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproNOTProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproOpenToAllProtocol;
import dk.alexandra.fresco.suite.tinytables.prepro.protocols.TinyTablesPreproXORProtocol;

public class TinyTablesPreproBuilderFactory implements BuilderFactoryBinary {

  private int counter = 0;

  public TinyTablesPreproBuilderFactory() {}

  private int getNextId() {
    return counter++;
  }

  @Override
  public Binary createBinary(ProtocolBuilderBinary builder) {
    return new Binary() {

      @Override
      public DRes<SBool> xor(DRes<SBool> left, DRes<SBool> right) {
        SBool out = new TinyTablesPreproSBool();
        TinyTablesPreproXORProtocol p = new TinyTablesPreproXORProtocol(left, right, out);
        builder.append(p);
        return p;
      }

      @Override
      public DRes<SBool> randomBit() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public DRes<Boolean> open(DRes<SBool> toOpen, int towardsPartyId) {
        throw new RuntimeException("Not implemented yet");
      }

      @Override
      public DRes<Boolean> open(DRes<SBool> toOpen) {
        TinyTablesPreproOpenToAllProtocol p =
            new TinyTablesPreproOpenToAllProtocol(getNextId(), toOpen);
        builder.append(p);
        // Always return false - preprocessing cannot compute output.
        return () -> false;
      }

      @Override
      public DRes<SBool> not(DRes<SBool> in) {
        SBool out = new TinyTablesPreproSBool();
        TinyTablesPreproNOTProtocol p = new TinyTablesPreproNOTProtocol(in, out);
        builder.append(p);
        return p;
      }

      @Override
      public DRes<SBool> known(boolean known) {
        // Ignore the value and use trivial mask
        return () -> new TinyTablesPreproSBool(new TinyTablesElement(false));
      }

      @Override
      public DRes<SBool> input(boolean in, int inputter) {
        TinyTablesPreproCloseProtocol p = new TinyTablesPreproCloseProtocol(getNextId(), inputter);
        builder.append(p);
        return p;
      }

      @Override
      public DRes<SBool> and(DRes<SBool> left, DRes<SBool> right) {
        SBool out = new TinyTablesPreproSBool();
        TinyTablesPreproANDProtocol p =
            new TinyTablesPreproANDProtocol(getNextId(), left, right, out);
        builder.append(p);
        return p;
      }
    };
  }

}
