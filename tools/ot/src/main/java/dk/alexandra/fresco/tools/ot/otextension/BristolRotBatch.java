package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.RotBatch;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Class implementing a batched random OT protocol, allowing the sending and receiving of any type
 * of objects and of any lengths. Use the underlying random OT protocol to construct random messages
 * in a batch. Then use each of these messages as a seed to a PRG and extend them to a sufficient
 * amount of bits.
 */
public class BristolRotBatch implements RotBatch {
  private final RotFactory rot;
  private final int comSecParam;
  private final int statSecParam;

  private RotSender sender;
  private RotReceiver receiver;

  /**
   * Constructs a new random batch OT protocol and constructs the internal sender and receiver
   * objects.
   *
   * @param randomOtExtension An instance of the underlying random OT extension
   * @param comSecParam The computational security parameter
   * @param statSecParam The statistical security parameter
   */
  public BristolRotBatch(RotFactory randomOtExtension, int comSecParam, int statSecParam) {
    this.rot = randomOtExtension;
    this.comSecParam = comSecParam;
    this.statSecParam = statSecParam;
  }

  @Override
  public List<Pair<StrictBitVector, StrictBitVector>> send(int numMessages, int sizeOfEachMessage) {
    if (this.sender == null) {
      this.sender = rot.createSender();
    }
    int amountToPreprocess = computeExtensionSize(numMessages, comSecParam, statSecParam);
    Pair<List<StrictBitVector>, List<StrictBitVector>> messages = sender.extend(amountToPreprocess);
    List<StrictBitVector> zeroMessages = messages.getFirst().parallelStream().limit(numMessages)
        .map(m -> LengthAdjustment.adjust(m.toByteArray(), sizeOfEachMessage / Byte.SIZE))
        .map(StrictBitVector::new)
        .collect(Collectors.toList());
    List<StrictBitVector> oneMessages = messages.getSecond().parallelStream().limit(numMessages)
        .map(m -> LengthAdjustment.adjust(m.toByteArray(), sizeOfEachMessage / Byte.SIZE))
        .map(StrictBitVector::new)
        .collect(Collectors.toList());
    return IntStream.range(0, numMessages).parallel()
        .mapToObj(i -> new Pair<>(zeroMessages.get(i), oneMessages.get(i)))
        .collect(Collectors.toList());
  }

  @Override
  public List<StrictBitVector> receive(StrictBitVector choiceBits, int sizeOfEachMessage) {
    if (this.receiver == null) {
      this.receiver = rot.createReceiver();
    }
    int amountToPreprocess = computeExtensionSize(choiceBits.getSize(), comSecParam, statSecParam);
    byte[] extraByteChoices = Arrays.copyOf(choiceBits.toByteArray(),
        amountToPreprocess / Byte.SIZE);
    List<StrictBitVector> messages = receiver.extend(new StrictBitVector(extraByteChoices));
    return messages.parallelStream().limit(choiceBits.getSize())
        .map(m -> LengthAdjustment.adjust(m.toByteArray(), sizeOfEachMessage / Byte.SIZE))
        .map(StrictBitVector::new)
        .collect(Collectors.toList());
  }

  /**
   * Compute the minimal amount of OTs we have to preprocess in order to be sure to get "minSiz",
   * <i>usable</i> OTs. This is not trivial since kbitLength+lambdaParam amount of OTs must be
   * sacrificed in the underlying process.
   *
   * @param minSize The amount of usable OTs we wish to have in the end
   * @param kbitLength The computational security parameter
   * @param lambdaParam The statistical security parameter
   * @return The amount of OTs that must be extended such that the transposition algorithm in
   *         correlated OT with errors work.
   */
  public static int computeExtensionSize(int minSize, int kbitLength, int lambdaParam) {
    // Increase the amount of OTs if needed, to ensure that the result is of the
    // from 8*2^x for x > 1. I.e. s.t. that "minSize" >= 16
    minSize = Math.max(minSize, 16);
    // Compute the number which will be passed on to the transposition algorithm
    int newNum = minSize + kbitLength + lambdaParam;
    // Check if "newNum" is a two power, this is done by checking if all bits,
    // besides the msb, is 0 and only msb is 1
    if ((newNum & (newNum - 1)) != 0) {
      // Compute the 2 exponent needed for the total amount of OTs (some of
      // which must be sacrificed)
      int exponent = (int) Math.ceil(Math.log(newNum) / Math.log(2));
      // Finally compute the amount of usable OTs to get from the call to the
      // underlying ROT functionality
      return (1 << exponent) - kbitLength - lambdaParam;
    }
    return newNum - kbitLength - lambdaParam;
  }
}
