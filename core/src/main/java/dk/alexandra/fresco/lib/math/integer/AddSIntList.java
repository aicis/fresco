package dk.alexandra.fresco.lib.math.integer;

import dk.alexandra.fresco.framework.Computation;
import dk.alexandra.fresco.framework.ProtocolProducer;
import dk.alexandra.fresco.framework.builder.ProtocolBuilder;
import dk.alexandra.fresco.framework.value.SInt;
import dk.alexandra.fresco.lib.field.integer.BasicNumericFactory;
import dk.alexandra.fresco.lib.helper.AbstractRoundBasedProtocol;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Protocol producer for summing a list of SInts
 *
 * @param <SIntT> the type of SInts to add - and later output
 */
public class AddSIntList<SIntT extends SInt> extends AbstractRoundBasedProtocol
    implements Computation<SInt> {

  private final BasicNumericFactory<SIntT> factory;
  private SInt result;

  private List<SInt> currentInputList;
  private SInt current;

  /**
   * Creates a new AddSIntList.
   *
   * @param factory the construction of
   * @param input the input to sum
   */
  public AddSIntList(BasicNumericFactory<SIntT> factory, SIntT... input) {
    this.currentInputList = Arrays.asList(input);
    this.factory = factory;
    this.current = factory.getSInt(0);
    this.result = factory.getSInt();
  }

  @Override
  public ProtocolProducer nextProtocolProducer() {
    if (currentInputList.isEmpty()) {
      result.setSerializableContent(current.getSerializableContent());
      return null;
    }
    List<SInt> outputs = new LinkedList<>();
    ProtocolBuilder<SIntT> parallel = ProtocolBuilder.createParallel(factory);
    BasicNumericFactory<SIntT> appendingFactory = parallel.createAppendingBasicNumericFactory();
    SInt left = null;
    for (SInt input : currentInputList) {
      if (left == null) {
        left = input;
      } else {
        outputs.add(appendingFactory.add(left, input).out());
        left = null;
      }
    }
    if (left != null) {
      current = appendingFactory.add(left, current).out();
    }
    currentInputList = outputs;
    return parallel.build();
  }

  @Override
  public SInt out() {
    return result;
  }
}
