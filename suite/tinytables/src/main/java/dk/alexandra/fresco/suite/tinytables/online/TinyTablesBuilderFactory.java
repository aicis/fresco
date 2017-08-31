package dk.alexandra.fresco.suite.tinytables.online;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.builder.binary.BinaryBuilder;
import dk.alexandra.fresco.framework.builder.binary.BuilderFactoryBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.datatypes.TinyTablesElement;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesSBool;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesANDProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesCloseProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesNOTProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesOpenToAllProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesXORProtocol;

public class TinyTablesBuilderFactory implements BuilderFactoryBinary {

  private int counter = 0;

  public TinyTablesBuilderFactory() {}

  private int getNextId() {
    return counter++;
  }

  @Override
  public BinaryBuilder createBinaryBuilder(ProtocolBuilderBinary builder) {
    return new BinaryBuilder() {

      @Override
      public Computation<SBool> xor(Computation<SBool> left, Computation<SBool> right) {
        SBool out = new TinyTablesSBool();
        TinyTablesXORProtocol p = new TinyTablesXORProtocol(left, right, out);
        builder.append(p);
        return p;
      }

      @Override
      public Computation<SBool> randomBit() {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public Computation<Boolean> open(Computation<SBool> toOpen, int towardsPartyId) {
        throw new RuntimeException("Not implemented yet");
      }

      @Override
      public Computation<Boolean> open(Computation<SBool> toOpen) {
        TinyTablesOpenToAllProtocol p = new TinyTablesOpenToAllProtocol(getNextId(), toOpen);
        builder.append(p);
        return p;
      }

      @Override
      public Computation<SBool> not(Computation<SBool> in) {
        SBool out = new TinyTablesSBool();
        TinyTablesNOTProtocol p = new TinyTablesNOTProtocol(in, out);
        builder.append(p);
        return p;
      }

      @Override
      public Computation<SBool> known(boolean known) {
        return () -> new TinyTablesSBool(new TinyTablesElement(known));
      }

      @Override
      public Computation<SBool> input(boolean in, int inputter) {
        TinyTablesCloseProtocol p = new TinyTablesCloseProtocol(getNextId(), inputter, in);
        builder.append(p);
        return p;
      }

      @Override
      public Computation<SBool> and(Computation<SBool> left, Computation<SBool> right) {
        SBool out = new TinyTablesSBool();
        TinyTablesANDProtocol p = new TinyTablesANDProtocol(getNextId(), left, right, out);
        builder.append(p);
        return p;
      }
    };
  }

}
