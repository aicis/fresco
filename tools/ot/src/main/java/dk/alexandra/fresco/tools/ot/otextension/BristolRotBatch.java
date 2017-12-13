package dk.alexandra.fresco.tools.ot.otextension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.AesCtrDrbg;
import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.RotBatch;

/**
 * Class implementing a batched random OT protocol, allowing the sending and
 * receiving of any type of objects and of any lengths. Use the underlying
 * random OT protocol to construct "numMessages" in a batch. Then use each of
 * these messages as a seed to a PRG and extend them to "sizeOfEachMessage"
 * bits.
 * 
 * @author jot2re
 *
 * @param <T>
 *          The objects to send/receive
 */
public class BristolRotBatch implements RotBatch<StrictBitVector> {
  protected RotSender sender;
  protected RotReceiver receiver;

  /**
   * Constructs a new random batch OT protocol and constructs the internal sender and
   * receiver objects.
   * 
   * @param myId
   *          The unique ID of the calling party
   * @param otherId
   *          The unique ID of the other party (not the calling party)
   *          participating in the protocol
   * @param kbitLength
   *          The computational security parameter
   * @param lambdaSecurityParam
   *          The statistical security parameter
   * @param rand
   *          Object used for randomness generation
   * @param network
   *          The network instance
   */
  public BristolRotBatch(int myId, int otherId, int kbitLength,
      int lambdaSecurityParam, Drbg rand, Network network) {
    Rot rot = new Rot(myId, otherId, kbitLength, lambdaSecurityParam, rand,
        network);
    this.sender = rot.getSender();
    this.receiver = rot.getReceiver();
  }

  public void initSender() {
    sender.initialize();
  }

  public void initReceiver() {
    receiver.initialize();
  }

  @Override
  public List<Pair<StrictBitVector, StrictBitVector>> send(int numMessages,
      int sizeOfEachMessage) {
    // Initialize the underlying functionalities if needed
    if (sender.initialized == false) {
      sender.initialize();
    }
    List<Pair<StrictBitVector, StrictBitVector>> res = new ArrayList<>(
        numMessages);
    int amountToPreprocess = computeExtensionSize(numMessages,
        sender.getKbitLength(), sender.getLambdaSecurityParam());
    Pair<List<StrictBitVector>, List<StrictBitVector>> messages = sender
        .extend(amountToPreprocess);
    List<StrictBitVector> rawZeroMessages = messages.getFirst();
    List<StrictBitVector> rawOneMessages = messages.getSecond();
    for (int i = 0; i < numMessages; i++) {
      StrictBitVector zeroMessage = computeRandomMessage(rawZeroMessages.get(i),
          sizeOfEachMessage);
      StrictBitVector oneMessage = computeRandomMessage(rawOneMessages.get(i),
          sizeOfEachMessage);
      Pair<StrictBitVector, StrictBitVector> currentPair = new Pair<>(
          zeroMessage, oneMessage);
      res.add(currentPair);
    }
    return res;
  }

  @Override
  public List<StrictBitVector> receive(StrictBitVector choiceBits,
      int sizeOfEachMessage) {
    // Initialize the underlying functionalities if needed
    if (receiver.initialized == false) {
      receiver.initialize();
    }
    List<StrictBitVector> res = new ArrayList<>(choiceBits.getSize());
    // Find how many OTs we need to preprocess
    int amountToPreprocess = computeExtensionSize(choiceBits.getSize(),
        receiver.getKbitLength(), receiver.getLambdaSecurityParam());
    // Construct a new choice-bit vector of the original choices, padded with
    // 0 choices if needed
    byte[] extraByteChoices = Arrays.copyOf(choiceBits.toByteArray(),
        amountToPreprocess / 8);
    StrictBitVector extraChoices = new StrictBitVector(extraByteChoices,
        amountToPreprocess);
    List<StrictBitVector> messages = receiver.extend(extraChoices);
    for (int i = 0; i < choiceBits.getSize(); i++) {
      StrictBitVector newMessage = computeRandomMessage(messages.get(i),
          sizeOfEachMessage);
      res.add(newMessage);
    }
    return res;
  }

  /**
   * Compute the minimal amount of OTs we have to preprocess in order to be sure
   * to get "minSiz", <i>usable</i> OTs. This is not trivial since
   * kbitLength+lambdaParam amount of OTs must be sacrificed in the underlying
   * process.
   * 
   * @param minSize
   *          The amount of usable OTs we wish to have in the end
   * @param kbitLength
   *          The computational security parameter
   * @param lambdaParam
   *          The statistical security parameter
   * @return The amount of OTs that must be extended such that the transposition
   *         algorithm in correlated OT with errors work.
   */
  public static int computeExtensionSize(int minSize, int kbitLength,
      int lambdaParam) {
    // Increase the amount of OTs if needed, to ensure that the result is of the
    // from 8*2^x for x > 1. I.e. s.t. that "minSize" >= 16
    int newNum = Math.max(minSize, 16);
    // Compute the number which will be passed on to the transposition algorithm
    newNum = minSize + kbitLength + lambdaParam;
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

  /**
   * Use "seed" as a seed to a PRG and construct a new messages of
   * "sizeOfMessage" bits using this PRG.
   * 
   * @param seed
   *          The seed for the PRG
   * @param sizeOfMessage
   *          Size in bits of the message to construct
   * @return A random messages generated using a PRG
   */
  private StrictBitVector computeRandomMessage(StrictBitVector seed,
      int sizeOfMessage) {
    Drbg rand = new AesCtrDrbg(seed.toByteArray());
    return new StrictBitVector(sizeOfMessage, rand);
  }
}
