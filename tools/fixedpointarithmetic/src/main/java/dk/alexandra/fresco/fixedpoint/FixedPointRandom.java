package dk.alexandra.fresco.fixedpoint;

import ch.qos.logback.core.net.SyslogOutputStream;
import dk.alexandra.fresco.framework.DRes;
import dk.alexandra.fresco.framework.builder.Computation;
import dk.alexandra.fresco.framework.builder.numeric.ProtocolBuilderNumeric;
import dk.alexandra.fresco.framework.builder.numeric.AdvancedNumeric.RandomAdditiveMask;
import dk.alexandra.fresco.framework.value.SInt;
import java.math.BigInteger;

/**
 * Construct a random SFixed with a value between 0 and 1.  
 *
 */
public class FixedPointRandom implements Computation<SFixedSIntWrapper, ProtocolBuilderNumeric> {

  private final int scale;
  private final int scaleSize;
  private final int precision;

  public FixedPointRandom(int precision) {
    this.precision = precision;
    this.scale = (int)Math.pow(10, precision);
    this.scaleSize = (int)Math.ceil((Math.log(scale)/(Math.log(2))));
  }

  @Override
  public DRes<SFixedSIntWrapper> buildComputation(ProtocolBuilderNumeric builder) {
    
    System.out.println("scale: "+scale+" has size: "+scaleSize);

    DRes<RandomAdditiveMask> random1 = builder.advancedNumeric().additiveMask(scaleSize);
    System.out.println("random: "+random1);
    DRes<SInt> tenPowered = builder.numeric().known(BigInteger.TEN.pow(precision));
    
    builder.build();
    DRes<SInt> comp1 = builder.comparison().compareLEQ(random1.out().r, tenPowered);
    System.out.println("comp:" +comp1);
    DRes<BigInteger> openComp1 = builder.numeric().open(comp1);

    
   IterationState state1 = new IterationState(openComp1.out(), random1.out().r);
   System.out.println("first state: "+state1);

    
    return builder.seq(seq -> {
      System.out.println("first");
      DRes<RandomAdditiveMask> random = builder.advancedNumeric().additiveMask(scaleSize);
      System.out.println("random: "+random);
      DRes<SInt> comp = builder.comparison().compareLEQ(random.out().r, builder.numeric().known(BigInteger.TEN.pow(precision)));
      System.out.println("comp:" +comp);
      DRes<BigInteger> openComp = builder.numeric().open(comp);

      
     IterationState state = new IterationState(openComp.out(), random.out().r);
     System.out.println("first state: "+state);
     return state;
    }).whileLoop(
        
        (state) -> BigInteger.ONE.equals(state.openComp), 
        (seq, state) -> {
          System.out.println("trying second");
          DRes<RandomAdditiveMask> random = builder.advancedNumeric().additiveMask(scaleSize);
          DRes<SInt> comp = builder.comparison().compareLEQ(random.out().r, builder.numeric().known(BigInteger.TEN.pow(precision)));
          DRes<BigInteger> openComp = builder.numeric().open(comp);
          IterationState is = new IterationState(openComp.out(), random.out().r);
          return is;
        }
    ).seq((seq,state) -> {
      return ()->new SFixedSIntWrapper(state.value);
      }
    );
    
    /*
    DRes<RandomAdditiveMask> random = builder.advancedNumeric().additiveMask(scaleSize);
    DRes<SInt> comp = builder.comparison().compareLEQ(random.out().r, builder.numeric().known(BigInteger.TEN.pow(precision)));
    DRes<BigInteger> openComp = builder.numeric().open(comp);
    while (BigInteger.ZERO.equals(openComp.out())) {
      random = builder.advancedNumeric().additiveMask(scaleSize);
      comp = builder.comparison().compareLEQ(random.out().r, builder.numeric().known(BigInteger.TEN.pow(precision)));
      openComp = builder.numeric().open(comp);
    }

    return new SFixedSIntWrapper(()->random.out().r);
*/
  }

  private static final class IterationState implements DRes<IterationState> {

    private final BigInteger openComp;
    private final SInt value;

    private IterationState(BigInteger openComp, SInt value) {
      this.openComp = openComp;
      this.value = value;
    }

    @Override
    public IterationState out() {
      return this;
    }
  }
}