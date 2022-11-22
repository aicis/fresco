package dk.alexandra.fresco.suite.crt;

import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.numeric.BuilderFactoryNumeric;
import dk.alexandra.fresco.framework.builder.numeric.Numeric;
import dk.alexandra.fresco.framework.builder.numeric.NumericResourcePool;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericContext;
import dk.alexandra.fresco.suite.crt.datatypes.CRTSInt;
import dk.alexandra.fresco.suite.crt.datatypes.resource.CRTResourcePool;
import dk.alexandra.fresco.suite.crt.protocols.framework.ProtocolBuilderNumericWrapper;

import java.math.BigInteger;

public class CRTBuilderFactory<ResourcePoolA extends NumericResourcePool, ResourcePoolB extends NumericResourcePool>
    implements BuilderFactoryNumeric {

  private final CRTNumericContext<ResourcePoolA, ResourcePoolB> context;
  private final BuilderFactoryNumeric left;
  private final BuilderFactoryNumeric right;
  private final BigInteger p, q;
  private final ResourcePoolA resourcePoolLeft;
  private final ResourcePoolB resourcePoolRight;

  public CRTBuilderFactory(CRTResourcePool<ResourcePoolA, ResourcePoolB> resourcePool,
                           BuilderFactoryNumeric left,
                           BuilderFactoryNumeric right) {

    if (resourcePool.getSubResourcePools().getFirst().getMyId() != resourcePool.getSubResourcePools().getSecond().getMyId()
        || resourcePool.getSubResourcePools().getFirst().getNoOfParties() != resourcePool.getSubResourcePools().getSecond().getNoOfParties()) {
      throw new IllegalArgumentException(
          "The protocol suites used must be configured with the same ID and number of players");
    }

    this.left = left;
    this.resourcePoolLeft = resourcePool.getSubResourcePools().getFirst();
    this.right = right;
    this.resourcePoolRight = resourcePool.getSubResourcePools().getSecond();
    this.p = resourcePoolLeft.getModulus();
    this.q = resourcePoolRight.getModulus();
    this.context = new CRTNumericContext<>(
            p.bitLength() + q.bitLength() - 40, //TODO
            resourcePoolLeft.getMyId(), resourcePoolLeft.getNoOfParties(), left, right, p, q, resourcePool);
  }

  @Override
  public BasicNumericContext getBasicNumericContext() {
    return context;
  }


  @Override
  public Numeric createNumeric(ProtocolBuilderNumeric builder) {
    return new Numeric() {

      @Override
      public DRes<SInt> add(DRes<SInt> a, DRes<SInt> b) {

        return builder.par(par -> {

          CRTSInt aOut = (CRTSInt) a.out();
          DRes<SInt> aLeft = aOut.getLeft();
          DRes<SInt> aRight = aOut.getRight();

          CRTSInt bOut = (CRTSInt) b.out();
          DRes<SInt> bLeft = bOut.getLeft();
          DRes<SInt> bRight = bOut.getRight();

          Numeric l = context.leftNumeric(par);
          Numeric r = context.rightNumeric(par);

          return new CRTSInt(
              l.add(aLeft, bLeft),
                  r.add(aRight, bRight));
        });
      }

      @Override
      public DRes<SInt> add(BigInteger a, DRes<SInt> b) {
        return builder.seq(seq -> {
          Numeric numeric = createNumeric(seq);
          DRes<SInt> aSecret = numeric.known(a);
          return numeric.add(aSecret, b);
        });
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, DRes<SInt> b) {
        return builder.par(par -> {

          CRTSInt aOut = (CRTSInt) a.out();
          DRes<SInt> aLeft = aOut.getLeft();
          DRes<SInt> aRight = aOut.getRight();

          CRTSInt bOut = (CRTSInt) b.out();
          DRes<SInt> bLeft = bOut.getLeft();
          DRes<SInt> bRight = bOut.getRight();

          Numeric l = context.leftNumeric(par);
          Numeric r = context.rightNumeric(par);

          return new CRTSInt(
                  l.sub(aLeft, bLeft),
                  r.sub(aRight, bRight));
        });
      }

      @Override
      public DRes<SInt> sub(BigInteger a, DRes<SInt> b) {
        return builder.seq(seq -> {
          Numeric numeric = createNumeric(seq);
          DRes<SInt> aSecret = numeric.known(a);
          return numeric.sub(aSecret, b);
        });
      }

      @Override
      public DRes<SInt> sub(DRes<SInt> a, BigInteger b) {
        return builder.seq(seq -> {
          Numeric numeric = createNumeric(seq);
          DRes<SInt> bSecret = numeric.known(b);
          return numeric.sub(a, bSecret);
        });
      }

      @Override
      public DRes<SInt> mult(DRes<SInt> a, DRes<SInt> b) {
        return builder.par(par -> {
          CRTSInt aOut = (CRTSInt) a.out();
          DRes<SInt> aLeft = aOut.getLeft();
          DRes<SInt> aRight = aOut.getRight();

          CRTSInt bOut = (CRTSInt) b.out();
          DRes<SInt> bLeft = bOut.getLeft();
          DRes<SInt> bRight = bOut.getRight();

          Numeric l = context.leftNumeric(par);
          Numeric r = context.rightNumeric(par);

          return new CRTSInt(
                  l.mult(aLeft, bLeft),
                  r.mult(aRight, bRight));
        });
      }

      @Override
      public DRes<SInt> mult(BigInteger a, DRes<SInt> b) {
        return builder.par(par -> {
          Pair<BigInteger, BigInteger> aRNS = Util.mapToCRT(a, p, q);

          CRTSInt bOut = (CRTSInt) b.out();
          DRes<SInt> bLeft = bOut.getLeft();
          DRes<SInt> bRight = bOut.getRight();

          Numeric l = context.leftNumeric(par);
          Numeric r = context.rightNumeric(par);

          return new CRTSInt(
                  l.mult(aRNS.getFirst(), bLeft),
                  r.mult(aRNS.getSecond(), bRight));
        });
      }

      @Override
      public DRes<SInt> randomBit() {
        return builder.seq(seq -> seq.numeric().known(0)); // TODO
      }

      @Override
      public DRes<SInt> randomElement() {
        return builder.par(par -> {
          Numeric l = context.leftNumeric(par);
          Numeric r = context.rightNumeric(par);
          return new CRTSInt(l.randomElement(),
                  r.randomElement());
        });
      }

      @Override
      public DRes<SInt> known(BigInteger value) {
        Pair<BigInteger, BigInteger> crt = Util.mapToCRT(value, p, q);

        return builder.par(par -> {
          Numeric l = context.leftNumeric(par);
          Numeric r = context.rightNumeric(par);
          return new CRTSInt(l.known(crt.getFirst()), r.known(crt.getSecond()));
        });
      }

      @Override
      public DRes<SInt> input(BigInteger value, int inputParty) {
         Pair<BigInteger, BigInteger> crt;
        if (value != null) {
          crt = Util.mapToCRT(value, p, q);
        } else {
          crt = new Pair<>(null, null);
        }

        return builder.par(par -> {
          Numeric l = context.leftNumeric(par);
          Numeric r = context.rightNumeric(par);

          return new CRTSInt(l.input(crt.getFirst(), inputParty),
                  r.input(crt.getSecond(), inputParty));
        });
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare) {
        return builder.par(par -> {
          CRTSInt crtsInt = (CRTSInt) secretShare.out();

          Numeric l = context.leftNumeric(par);
          Numeric r = context.rightNumeric(par);

          return Pair.lazy(l.open(crtsInt.getLeft()), r.open(crtsInt.getRight()));
        }).seq((seq, opened) -> DRes.of(Util.mapToBigInteger(opened.getFirst().out(), opened.getSecond().out(), p, q)));
      }

      @Override
      public DRes<BigInteger> open(DRes<SInt> secretShare, int outputParty) {
        return builder.par(par -> {
          CRTSInt crtsInt = (CRTSInt) secretShare.out();

          Numeric l = left.createNumeric(new ProtocolBuilderNumericWrapper<>(par, left, resourcePoolLeft));
          Numeric r = right.createNumeric(new ProtocolBuilderNumericWrapper<>(par, right, resourcePoolRight));

          return Pair.lazy(l.open(crtsInt.getLeft(), outputParty), r.open(crtsInt.getRight(), outputParty));
        }).seq((seq, opened) -> DRes.of(opened.getFirst() == null ? null : Util.mapToBigInteger(opened.getFirst().out(), opened.getSecond().out(), p, q)));

      }
    };
  }

}
