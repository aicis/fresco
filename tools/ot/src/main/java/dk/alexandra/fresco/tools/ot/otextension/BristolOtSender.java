package dk.alexandra.fresco.tools.ot.otextension;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import dk.alexandra.fresco.tools.cointossing.FailedCoinTossingException;
import dk.alexandra.fresco.tools.commitment.FailedCommitmentException;
import dk.alexandra.fresco.tools.commitment.MaliciousCommitmentException;

public class BristolOtSender extends BristolOtShared {
  private RotSender sender;

  private Pair<List<StrictBitVector>, List<StrictBitVector>> randomMessages;
  private int offset = -1;

  public BristolOtSender(RotSender rotSender, int batchSize) {
    super(rotSender, batchSize);
    this.sender = rotSender;
  }

  public void initialize()
      throws MaliciousCommitmentException, FailedCommitmentException,
      FailedCoinTossingException, FailedOtExtensionException,
      MaliciousOtExtensionException {
    if (initialized) {
      throw new IllegalStateException("Already initialized");
    }
    if (sender.initialized == false) {
      sender.initialize();
    }
    initialized = true;
  }

  public void send(byte[] messageZero, byte[] messageOne)
      throws MaliciousOtExtensionException, FailedOtExtensionException,
      MaliciousCommitmentException, FailedCommitmentException,
      FailedCoinTossingException {
    if (messageZero.length != messageOne.length) {
      throw new IllegalArgumentException("Messages must have equal length");
    }
    if (initialized == false) {
      initialize();
    }
    if (offset < 0 || offset >= batchSize) {
      randomMessages = sender.extend(batchSize);
      offset = 0;
    }
    doActualSend(messageZero, messageOne);
    offset++;
  }

  private void doActualSend(byte[] messageZero, byte[] messageOne)
      throws FailedOtExtensionException {
    StrictBitVector randomZero = randomMessages.getFirst().get(offset);
    StrictBitVector randomOne = randomMessages.getSecond().get(offset);
    byte[] switchBit = getNetwork().receive(getOtherId());
    if (switchBit[0] == 0x00) {
      sendAdjustedMessage(messageZero, randomZero.toByteArray());
      sendAdjustedMessage(messageOne, randomOne.toByteArray());
    } else {
      sendAdjustedMessage(messageOne, randomZero.toByteArray());
      sendAdjustedMessage(messageZero, randomOne.toByteArray());
    }
  }

  private void sendAdjustedMessage(byte[] realMessage,
      byte[] randomMessage) throws FailedOtExtensionException {
    byte[] toSend;
    if (realMessage.length < randomMessage.length) {
      toSend = randomMessage;
    } else {
      try {
        SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
        rand.setSeed(randomMessage);
        toSend = new byte[randomMessage.length];
        rand.nextBytes(toSend);
      } catch (NoSuchAlgorithmException e) {
        throw new FailedOtExtensionException(e.getMessage());
      }
    }
    ByteArrayHelper.xor(toSend, realMessage);
    getNetwork().send(getOtherId(), toSend);
  }
}
