package dk.alexandra.fresco.tools.ot.otextension;

import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import dk.alexandra.fresco.framework.network.Network;
import dk.alexandra.fresco.framework.util.ByteArrayHelper;
import dk.alexandra.fresco.tools.cointossing.FailedCoinTossingException;
import dk.alexandra.fresco.tools.commitment.FailedCommitmentException;
import dk.alexandra.fresco.tools.commitment.MaliciousCommitmentException;
import dk.alexandra.fresco.tools.ot.base.FailedOtException;
import dk.alexandra.fresco.tools.ot.base.MaliciousOtException;
import dk.alexandra.fresco.tools.ot.base.Ot;

public class BristolOt<T extends Serializable> implements Ot<T>
{

  protected BristolOtSender sender;
  protected BristolOtReceiver receiver;

  public BristolOt(Rot rot, int batchSize) {
    this.sender = new BristolOtSender(rot.getSender(), batchSize);
    this.receiver = new BristolOtReceiver(rot.getReceiver(), batchSize);
  }

  public BristolOt(int myId, int otherId, int kbitLength,
      int lambdaSecurityParam, Random rand, Network network, int batchSize) {
    Rot rot = new Rot(myId, otherId, kbitLength, lambdaSecurityParam, rand,
        network);
    RotSender sender = rot.getSender();
    RotReceiver receiver = rot.getReceiver();
    this.sender = new BristolOtSender(sender, batchSize);
    this.receiver = new BristolOtReceiver(receiver, batchSize);
  }

  @Override
  public void send(T messageZero, T messageOne)
      throws MaliciousOtException, FailedOtException {
    try {
      sender.send(ByteArrayHelper.serialize(messageZero),
          ByteArrayHelper.serialize(messageOne));
    } catch (MaliciousOtExtensionException | MaliciousCommitmentException  e) {
      throw new MaliciousOtException(e.getMessage());
    } catch (FailedOtExtensionException | FailedCommitmentException
        | FailedCoinTossingException | IOException e) {
      throw new FailedOtException(e.getMessage());
    }
  }

  @Override
  public T receive(Boolean choiceBit) throws MaliciousOtException, FailedOtException {
    try {
      return (T) ByteArrayHelper.deserialize(receiver.receive(choiceBit));
    } catch (MaliciousOtException
        | MaliciousCommitmentException | MaliciousOtExtensionException e) {
      throw new MaliciousOtException(e.getMessage());
    } catch (ClassNotFoundException | IOException | NoSuchAlgorithmException
        | FailedOtExtensionException | FailedCommitmentException
        | FailedCoinTossingException e) {
      throw new FailedOtException(e.getMessage());
    }
  }

}
