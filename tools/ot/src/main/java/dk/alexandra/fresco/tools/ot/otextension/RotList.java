package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.Ot;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper class for a list of random seed OTs. An instance of this class (once {@code send}
 * and/or {@code receive} has been called) is meant to be used for several OT extension instances.
 * <br/>
 * Notice that an instance of this class is only meant to contain a single and static list of OTs.
 * Thus a new instance must be made if one wishes to construct a list of new and random OTs.
 */
public class RotList {
  private final int amount;
  private final List<Pair<StrictBitVector, StrictBitVector>> sendMessages;
  private final List<StrictBitVector> learnedMessages;
  private final StrictBitVector choices;

  private boolean sent = false;
  private boolean received = false;

  /**
   * Prepares for doing a list of random OTs by internally constructing a list of
   * random messages and random bit choices.
   *
   * @param rand
   *          The randomness generator to use
   * @param amount
   *          The amount of OTs to construct
   */
  public RotList(Drbg rand, int amount) {
    this.amount = amount;
    sendMessages = new ArrayList<>(amount);
    learnedMessages = new ArrayList<>(amount);
    for (int i = 0; i < amount; i++) {
      StrictBitVector seedZero = new StrictBitVector(amount, rand);
      StrictBitVector seedOne = new StrictBitVector(amount, rand);
      sendMessages.add(new Pair<>(seedZero, seedOne));
    }
    choices = new StrictBitVector(amount, rand);
  }

  /**
   * Sends the prepared random OTs using {@code ot} as the underlying OT functionality.
   *
   * @param ot
   *          The OT functionality to use for sending the random OTs
   */
  public void send(Ot ot) {
    if (sent == true) {
      throw new IllegalStateException("Seed OTs have already been sent.");
    }
    for (Pair<StrictBitVector, StrictBitVector> pair : sendMessages) {
      ot.send(pair.getFirst(), pair.getSecond());
    }
    sent = true;
  }

  /**
   * Executes the receiving parts of the list of OTs using {@code ot} as the underlying OT protocol.
   *
   * @param ot
   *          The OT functionality to use for receiving the random OTs
   */
  public void receive(Ot ot) {
    if (received == true) {
      throw new IllegalStateException("Seed OTs have already been received.");
    }
    for (int i = 0; i < amount; i++) {
      StrictBitVector message = ot.receive(choices.getBit(i, false));
      learnedMessages.add(message);
    }
    received = true;
  }

  /**
   * Retrieves the list of random messages used as the sending part of the OTs,
   * assuming the OTs have been executed.
   *
   * @return The random messages used for the sender in list of OTs
   */
  public List<Pair<StrictBitVector, StrictBitVector>> getSentMessages() {
    if (sent == false) {
      throw new IllegalStateException("Seed OTs have not been sent yet.");
    }
    return sendMessages;
  }

  /**
   * Retrieves the list of random messages used as the receiving part of the OTs,
   * assuming the OTs have been executed.
   *
   * @return The random messages used for the receiver in list of OTs
   */
  public List<StrictBitVector> getLearnedMessages() {
    if (received == false) {
      throw new IllegalStateException("Seed OTs have not been received yet.");
    }
    return learnedMessages;
  }

  /**
   * Returns the bit vector of the random choices the receiving party has used in the list of OTs.
   *
   * @return The bit vector of random choices used by the receiver in the list of OTs
   */
  public StrictBitVector getChoices() {
    if (received == false) {
      throw new IllegalStateException("Seed OTs have not been received yet.");
    }
    return choices;
  }
}
