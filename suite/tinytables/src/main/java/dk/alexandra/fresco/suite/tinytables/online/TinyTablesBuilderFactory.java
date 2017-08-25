package dk.alexandra.fresco.suite.tinytables.online;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolFactory;
import dk.alexandra.fresco.framework.builder.binary.BinaryBuilder;
import dk.alexandra.fresco.framework.builder.binary.BuilderFactoryBinary;
import dk.alexandra.fresco.framework.builder.binary.ProtocolBuilderBinary;
import dk.alexandra.fresco.framework.network.SCENetwork;
import dk.alexandra.fresco.framework.sce.resources.ResourcePoolImpl;
import dk.alexandra.fresco.framework.value.SBool;
import dk.alexandra.fresco.suite.tinytables.online.datatypes.TinyTablesSBool;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesANDProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesCloseProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesNOTProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesOpenToAllProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesProtocol;
import dk.alexandra.fresco.suite.tinytables.online.protocols.TinyTablesXORProtocol;

public class TinyTablesBuilderFactory implements BuilderFactoryBinary {

  private TinyTablesFactory factory;
  private int counter = 0;

  public TinyTablesBuilderFactory(TinyTablesFactory factory) {
    this.factory = factory;
  }

  private int getNextId() {
    return counter++;
  }

  @Deprecated
  @Override
  public ProtocolFactory getProtocolFactory() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BinaryBuilder createBinaryBuilder(ProtocolBuilderBinary builder) {
    return new BinaryBuilder() {

      @Override
      public Computation<SBool> xor(Computation<SBool> left, boolean right) {
        throw new RuntimeException("Not implemented Yet");
      }

      @Override
      public Computation<SBool> xor(Computation<SBool> left, Computation<SBool> right) {
        SBool out = factory.getSBool();
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
        SBool out = factory.getSBool();
        TinyTablesNOTProtocol p = new TinyTablesNOTProtocol(in, out);
        builder.append(p);
        return p;
      }

      @Override
      public Computation<SBool> known(boolean known) {
        return () -> factory.getKnownConstantSBool(known);
      }

      @Override
      public Computation<SBool> input(boolean in, int inputter) {
        TinyTablesCloseProtocol p = new TinyTablesCloseProtocol(getNextId(), inputter, in);
        builder.append(p);
        return p;
      }

      @Override
      public Computation<SBool> copy(Computation<SBool> src) {
        TinyTablesSBool out = (TinyTablesSBool) factory.getSBool();
        TinyTablesProtocol<SBool> p = new TinyTablesProtocol<SBool>() {

          @Override
          public dk.alexandra.fresco.framework.NativeProtocol.EvaluationStatus evaluate(int round,
              ResourcePoolImpl resourcePool, SCENetwork network) {
            out.setValue(((TinyTablesSBool) src.out()).getValue());
            return EvaluationStatus.IS_DONE;
          }

          @Override
          public SBool out() {
            return out;
          }
        };

        builder.append(p);
        return p;
      }

      @Override
      public Computation<SBool> and(Computation<SBool> left, boolean right) {
        throw new RuntimeException("Not implemented yet");
      }

      @Override
      public Computation<SBool> and(Computation<SBool> left, Computation<SBool> right) {
        SBool out = factory.getSBool();
        TinyTablesANDProtocol p = new TinyTablesANDProtocol(getNextId(), left, right, out);
        builder.append(p);
        return p;
      }
    };
  }

}
