package dk.alexandra.fresco.tools.ot.otextension;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.Pair;
import dk.alexandra.fresco.framework.util.StrictBitVector;
import java.util.List;

/**
 * Protocol class for the party acting as the sender in an OT extension. The functionality works by
 * doing a batch of random OTs the first time the receive command is called and storing these
 * results internally. When it runs out, it automatically processes a new batch. These random OTs
 * are adjusted to work as chosen bit/message 1-out-of-2 OTs.
 */
public class BristolOtSender {
  private final RotSender sender;
  private final OtExtensionResourcePool resources;
  private final Network network;
  private final int batchSize;

  // The random messages generated in the underlying random OT functionality
  private Pair<List<StrictBitVector>, List<StrictBitVector>> randomMessages;
  // Index of the current random OT to use
  private int offset = -1;

  /**
   * Initializes the underlying Rot functionality using {@code rotSender}. It will then construct
   * OTs in batches of {@code batchSize}.
   *
   * @param rotSender The underlying random OT sender object to use
   * @param resources The resource pool for this specific instance
   * @param network The network to use
   * @param batchSize Size of the OT extension batch the protocol will construct
   */
  public BristolOtSender(RotSender rotSender, OtExtensionResourcePool resources, Network network,
      int batchSize) {
    this.sender = rotSender;
    this.resources = resources;
    this.network = network;
    this.batchSize = batchSize;
  }

  /**
   * Send the serialized message from the current 1-out-of-2 OT.
   *
   * @param messageZero The message to send for choice zero
   * @param messageOne The message to send for choice one
   */
  public void send(byte[] messageZero, byte[] messageOne) {
    // Check if there is still an unused random OT stored, if not, execute a
    // random OT extension
    if (offset < 0 || offset >= batchSize) {
      randomMessages = sender.extend(batchSize);
      offset = 0;
    }
    doActualSend(messageZero, messageOne);
    offset++;
  }

  /**
   * Adjust the random, preprocessed message, to fit the specific messages to send.
   *
   * @param messageZero The actual zero message to send
   * @param messageOne The actual one message to send
   */
  private void doActualSend(byte[] messageZero, byte[] messageOne) {
    // Find the correct preprocessed random OT messages
    StrictBitVector randomZero = randomMessages.getFirst().get(offset);
    StrictBitVector randomOne = randomMessages.getSecond().get(offset);
    int maxLength = Math.max(messageZero.length, messageOne.length);
    // Receive a bit from the receiver indicating whether the zero and one
    // messages should be switched around
    byte[] switchBit = network.receive(resources.getOtherId());
    // If false (indicated by byte 0x00), then don't switch around
    if (switchBit[0] == 0x00) {
      network.send(resources.getOtherId(),
          PseudoOtp.encrypt(messageZero, randomZero.toByteArray(), maxLength));
      network.send(resources.getOtherId(),
          PseudoOtp.encrypt(messageOne, randomOne.toByteArray(), maxLength));
    } else {
      network.send(resources.getOtherId(),
          PseudoOtp.encrypt(messageOne, randomZero.toByteArray(), maxLength));
      network.send(resources.getOtherId(),
          PseudoOtp.encrypt(messageZero, randomOne.toByteArray(), maxLength));
    }
  }
}
