package dk.alexandra.fresco.tools.ot.otextension;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.cointossing.FailedCoinTossingException;
import dk.alexandra.fresco.tools.commitment.FailedCommitmentException;
import dk.alexandra.fresco.tools.commitment.MaliciousCommitmentException;
import dk.alexandra.fresco.tools.ot.base.MaliciousOtException;

public class BristolOtReceiver extends BristolOtShared {
  private RotReceiver receiver;
  private List<StrictBitVector> randomMessages;
  private StrictBitVector choices;
  private int offset = -1;

  public BristolOtReceiver(RotReceiver rotReceiver, int batchSize) {
    super(rotReceiver, batchSize);
    this.receiver = rotReceiver;
  }

  public void initialize() throws FailedOtExtensionException,
      MaliciousCommitmentException, FailedCommitmentException,
      FailedCoinTossingException, MaliciousOtExtensionException {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    if (receiver.initialized == false) {
      receiver.initialize();
    }
    initialized = true;
  }

  public byte[] receive(Boolean choiceBit) throws FailedOtExtensionException,
      MaliciousOtException, NoSuchAlgorithmException,
      MaliciousCommitmentException, FailedCommitmentException,
      FailedCoinTossingException, MaliciousOtExtensionException {
    if (initialized == false) {
      initialize();
    }
    if (offset < 0 || offset >= batchSize) {
      choices = new StrictBitVector(batchSize, getRand());
      randomMessages = receiver.extend(choices);
      offset = 0;
    }
    byte[] res = doActualReceive(choiceBit);
    offset++;
    return res;
  }

  private byte[] doActualReceive(Boolean choiceBit)
      throws MaliciousOtException, FailedOtExtensionException {
    sendSwitchBit(choiceBit);
    byte[] zeroAdjustment = getNetwork().receive(getOtherId());
    byte[] oneAdjustment = getNetwork().receive(getOtherId());
    if (zeroAdjustment.length != oneAdjustment.length) {
      throw new MaliciousOtException(
          "Sender gave adjustment messages of different length.");
    }
    byte[] adjustment;
    if (choices.getBit(offset, false) == false) {
      adjustment = zeroAdjustment;
    } else {
      adjustment = oneAdjustment;
    }
    adjustMessage(adjustment);
    return adjustment;
  }

  private void sendSwitchBit(Boolean choiceBit) {
    byte[] switchBit = new byte[] { 0x00 };
    if (choiceBit ^ choices.getBit(offset, false) == true) {
      switchBit[0] = 0x01;
    }
    getNetwork().send(getOtherId(), switchBit);
  }

  private void adjustMessage(byte[] adjustment)
      throws FailedOtExtensionException {
    byte[] randomMessage = randomMessages.get(offset).toByteArray();
    if (adjustment.length < randomMessage.length) {
      ByteArrayHelper.xor(adjustment, randomMessage);
    } else {
      try {
        SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
        rand.setSeed(randomMessage);
        byte[] randomness = new byte[randomMessage.length];
        rand.nextBytes(randomness);
        ByteArrayHelper.xor(adjustment, randomness);
      } catch (NoSuchAlgorithmException e) {
        throw new FailedOtExtensionException(e.getMessage());
      }
    }
  }
}
