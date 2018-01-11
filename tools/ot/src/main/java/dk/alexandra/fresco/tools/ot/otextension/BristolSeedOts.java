package dk.alexandra.fresco.tools.ot.otextension;

import java.util.ArrayList;
import java.util.List;

import dk.alexandra.fresco.framework.util.Drbg;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.ot.base.Ot;

public class BristolSeedOts {
  private final int amount;
  private final Ot ot;

  private boolean sent = false;
  private boolean received = false;

  private final List<Pair<StrictBitVector, StrictBitVector>> sendMessages;
  private final List<StrictBitVector> learnedMessages;
  private final StrictBitVector choices;

  public BristolSeedOts(Drbg rand, int amount, Ot ot) {
    this.amount = amount;
    this.ot = ot;
    sendMessages = new ArrayList<>(amount);
    learnedMessages = new ArrayList<>(amount);
    for (int i = 0; i < amount; i++) {
      StrictBitVector seedZero = new StrictBitVector(amount, rand);
      StrictBitVector seedOne = new StrictBitVector(amount, rand);
      sendMessages.add(new Pair<>(seedZero, seedOne));
    }
    choices = new StrictBitVector(amount, rand);
  }

  public void send() {
    if (sent == true) {
      throw new IllegalStateException("Seed OTs have already been sent.");
    }
    for (Pair<StrictBitVector, StrictBitVector> pair : sendMessages) {
      ot.send(pair.getFirst(), pair.getSecond());
    }
    sent = true;
  }

  public void receive() {
    if (received == true) {
      throw new IllegalStateException("Seed OTs have already been received.");
    }
    for (int i = 0; i < amount; i++) {
      StrictBitVector message = ot.receive(choices.getBit(i, false));
      learnedMessages.add(message);
    }
    received = true;
  }

  public List<Pair<StrictBitVector, StrictBitVector>> getSentMessages() {
    if (sent == false) {
      throw new IllegalStateException("Seed OTs have not been sent yet.");
    }
    return sendMessages;
  }

  public List<StrictBitVector> getLearnedMessages() {
    if (received == false) {
      throw new IllegalStateException("Seed OTs have not been received yet.");
    }
    return learnedMessages;
  }

  public StrictBitVector getChoices() {
    if (received == false) {
      throw new IllegalStateException("Seed OTs have not been received yet.");
    }
    return choices;
  }
}
